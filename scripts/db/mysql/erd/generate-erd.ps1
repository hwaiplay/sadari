[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$ddlPath = Join-Path $PSScriptRoot '..\01-create.sql'
$repoRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..\..'))
$outputDirectory = Join-Path $repoRoot 'docs\architecture\database-erd'
$dbmlPath = Join-Path $outputDirectory 'sadari-database.dbml'
$dotPath = Join-Path $outputDirectory 'sadari-database.dot'
$svgPath = Join-Path $outputDirectory 'sadari-database.svg'
$rendererPath = Join-Path $PSScriptRoot 'render-erd.mjs'

$domains = @(
    [pscustomobject]@{ Key = 'common'; Label = '공통 및 파일'; Color = '#64748B' }
    [pscustomobject]@{ Key = 'administration'; Label = '관리자 및 운영 콘텐츠'; Color = '#2563EB' }
    [pscustomobject]@{ Key = 'user'; Label = '사용자 및 보안'; Color = '#0F766E' }
    [pscustomobject]@{ Key = 'reading'; Label = '도서 및 독서'; Color = '#7C3AED' }
    [pscustomobject]@{ Key = 'engagement'; Label = '소셜 및 알림'; Color = '#DB2777' }
    [pscustomobject]@{ Key = 'reading_club'; Label = '독서 모임'; Color = '#D97706' }
    [pscustomobject]@{ Key = 'support'; Label = '신고 및 고객문의'; Color = '#DC2626' }
    [pscustomobject]@{ Key = 'operations'; Label = '스케줄러 및 상태 이벤트'; Color = '#475569' }
)

function ConvertTo-DbmlText {
    param([AllowEmptyString()][string]$Value)

    # DBML 단일 따옴표 문자열 이스케이프
    return $Value.Replace('\', '\\').Replace("'", "\'").Replace("`r", '').Replace("`n", ' ')
}

function ConvertTo-HtmlText {
    param([AllowEmptyString()][string]$Value)

    # Graphviz HTML 레이블용 엔터티 변환
    return [System.Net.WebUtility]::HtmlEncode($Value)
}

function Get-ColumnNames {
    param([string]$Definition)

    $names = [System.Collections.Generic.List[string]]::new()

    # 백틱으로 구분된 컬럼명 수집
    foreach ($match in [regex]::Matches($Definition, '`(?<name>[^`]+)`')) {
        $names.Add($match.Groups['name'].Value)
    }

    # 컬럼명 배열 반환
    return $names.ToArray()
}

function Get-DomainKey {
    param([string]$TableName)

    # 독서 모임 전용 테이블 분류
    if ($TableName -match '^(TB|TM|TH)_CL') {
        return 'reading_club'
    }

    # 신고 및 고객문의 테이블 분류
    if ($TableName -match '^(TH_CM|CT_INQ)') {
        return 'support'
    }

    # 관리자와 운영 콘텐츠 테이블 분류
    if ($TableName -match '^(CT_NOTICE|CT_WLCMPG|CT_SVINFO|CT_POPUPX|TB_AUTHMN|TM_ADMENU|TM_ADMINX|TM_AUTHXM|TM_URMENU)$') {
        return 'administration'
    }

    # 사용자와 계정 보안 테이블 분류
    if ($TableName -match '^(TB_NKSEQX|TH_USSPND|TB_FOLLOW|TB_USBLOC|TB_LOGHIS|TH_USWTHD|TM_USERXM|TM_USSETX|TB_USVIEW|TB_USINTR)$') {
        return 'user'
    }

    # 도서와 독서 데이터 테이블 분류
    if ($TableName -match '^(TB_RDATDX|TM_BKINFO|TM_GOALXM|TM_REPORT|TM_RDTMRX)$') {
        return 'reading'
    }

    # 소셜 반응과 알림 테이블 분류
    if ($TableName -match '^(TM_ALICON|TB_ALTEMP|TB_ALIMXX|TB_PSHSUB|TB_LIKEXX|TB_REPLXX)$') {
        return 'engagement'
    }

    # 스케줄러와 상태 이벤트 테이블 분류
    if ($TableName -match '^(TL_|TB_EVTBOX)') {
        return 'operations'
    }

    # 공통 기반 테이블 기본 분류
    return 'common'
}

function Get-Domain {
    param([string]$DomainKey)

    # 영역 키와 일치하는 표시 정보 조회
    foreach ($domain in $domains) {
        if ($domain.Key -eq $DomainKey) {
            return $domain
        }
    }

    throw "알 수 없는 ERD 영역 키: $DomainKey"
}

function Get-DbmlDefault {
    param([AllowNull()][string]$DefaultValue)

    # 기본값 미지정과 명시적 Null 기본값 제외
    if ([string]::IsNullOrWhiteSpace($DefaultValue) -or $DefaultValue -eq 'NULL') {
        return $null
    }

    # 문자열과 숫자 기본값 원문 유지
    if ($DefaultValue -match "^'.*'$" -or $DefaultValue -match '^-?\d+(\.\d+)?$') {
        return $DefaultValue
    }

    # 함수형 기본값 DBML 표현 변환
    return "``$DefaultValue``"
}

function Get-ColumnFlags {
    param(
        [pscustomobject]$Table,
        [pscustomobject]$Column
    )

    $flags = [System.Collections.Generic.List[string]]::new()

    # 단일 컬럼 기본키 표시
    if ($Table.PrimaryKey.Count -eq 1 -and $Table.PrimaryKey[0] -eq $Column.Name) {
        $flags.Add('PK')
    }

    # 외래키 참여 컬럼 표시
    $foreignKeyColumns = @($Table.ForeignKeys | ForEach-Object { $_.Columns })

    if ($foreignKeyColumns -contains $Column.Name) {
        $flags.Add('FK')
    }

    # 자동 증가 컬럼 표시
    if ($Column.IsIncrement) {
        $flags.Add('AI')
    }

    # 필수 컬럼 표시
    if ($Column.IsNotNull) {
        $flags.Add('NN')
    }

    # 표시용 플래그 반환
    return ($flags -join ', ')
}

function Get-TableModel {
    param([System.Text.RegularExpressions.Match]$TableMatch)

    $tableName = $TableMatch.Groups['name'].Value
    $tableComment = $TableMatch.Groups['comment'].Value.Replace("''", "'")
    $body = $TableMatch.Groups['body'].Value
    $columns = [System.Collections.Generic.List[object]]::new()
    $primaryKey = [System.Collections.Generic.List[string]]::new()
    $indexes = [System.Collections.Generic.List[object]]::new()
    $foreignKeys = [System.Collections.Generic.List[object]]::new()
    $checks = [System.Collections.Generic.List[string]]::new()

    # 테이블 본문의 컬럼과 제약조건 분석
    foreach ($line in ($body -split "`r?`n")) {
        $trimmed = $line.Trim().TrimEnd(',')

        # 빈 줄 제외
        if ([string]::IsNullOrWhiteSpace($trimmed)) {
            continue
        }

        $columnMatch = [regex]::Match($trimmed, '^`(?<name>[^`]+)`\s+(?<definition>.+)$')

        # 컬럼 정의 수집
        if ($columnMatch.Success) {
            $definition = $columnMatch.Groups['definition'].Value
            $typeMatch = [regex]::Match($definition, '^(?<type>[A-Za-z]+(?:\([^)]*\))?)')
            $commentMatch = [regex]::Match($definition, "\bCOMMENT\s+'(?<comment>(?:''|[^'])*)'")
            $defaultMatch = [regex]::Match($definition, "\bDEFAULT\s+(?<default>'(?:''|[^'])*'|\([^)]*\)|[^\s,]+)")
            $columnComment = ''
            $defaultValue = $null

            # DDL 컬럼 코멘트 복원
            if ($commentMatch.Success) {
                $columnComment = $commentMatch.Groups['comment'].Value.Replace("''", "'")
            }

            # DDL 기본값 복원
            if ($defaultMatch.Success) {
                $defaultValue = $defaultMatch.Groups['default'].Value
            }

            $columns.Add([pscustomobject]@{
                Name = $columnMatch.Groups['name'].Value
                Type = $typeMatch.Groups['type'].Value.ToLowerInvariant()
                Comment = $columnComment
                Default = $defaultValue
                IsNotNull = $definition -match '\bNOT NULL\b'
                IsIncrement = $definition -match '\bAUTO_INCREMENT\b'
                IsGenerated = $definition -match '\bGENERATED ALWAYS\b'
            })
            continue
        }

        $primaryKeyMatch = [regex]::Match($trimmed, '^PRIMARY KEY\s*\((?<columns>.+)\)$')

        # 기본키 컬럼 수집
        if ($primaryKeyMatch.Success) {
            foreach ($columnName in (Get-ColumnNames $primaryKeyMatch.Groups['columns'].Value)) {
                $primaryKey.Add($columnName)
            }
            continue
        }

        $foreignKeyMatch = [regex]::Match(
            $trimmed,
            '^CONSTRAINT\s+`(?<name>[^`]+)`\s+FOREIGN KEY\s*\((?<columns>[^)]+)\)\s+REFERENCES\s+`(?<target>[^`]+)`\s*\((?<targetColumns>[^)]+)\)(?<actions>.*)$'
        )

        # 외래키 관계 수집
        if ($foreignKeyMatch.Success) {
            $actions = $foreignKeyMatch.Groups['actions'].Value
            $deleteAction = $null

            # 삭제 동작 수집
            if ($actions -match 'ON DELETE\s+(?<action>CASCADE|SET NULL|RESTRICT|NO ACTION)') {
                $deleteAction = $Matches['action'].ToLowerInvariant()
            }

            $foreignKeys.Add([pscustomobject]@{
                Name = $foreignKeyMatch.Groups['name'].Value
                Columns = @(Get-ColumnNames $foreignKeyMatch.Groups['columns'].Value)
                TargetTable = $foreignKeyMatch.Groups['target'].Value
                TargetColumns = @(Get-ColumnNames $foreignKeyMatch.Groups['targetColumns'].Value)
                DeleteAction = $deleteAction
            })
            continue
        }

        $indexMatch = [regex]::Match($trimmed, '^(?<unique>UNIQUE\s+)?KEY\s+`(?<name>[^`]+)`\s*\((?<columns>.+)\)$')

        # 일반 및 고유 인덱스 수집
        if ($indexMatch.Success) {
            $indexes.Add([pscustomobject]@{
                Name = $indexMatch.Groups['name'].Value
                Columns = @(Get-ColumnNames $indexMatch.Groups['columns'].Value)
                IsUnique = $indexMatch.Groups['unique'].Success
            })
            continue
        }

        $checkMatch = [regex]::Match($trimmed, '^CONSTRAINT\s+`(?<name>[^`]+)`\s+CHECK\s*\((?<expression>.+)\)$')

        # 검사 제약조건 수집
        if ($checkMatch.Success) {
            $checks.Add("$($checkMatch.Groups['name'].Value): $($checkMatch.Groups['expression'].Value)")
        }
    }

    # 테이블 구조 모델 반환
    return [pscustomobject]@{
        Name = $tableName
        Comment = $tableComment
        DomainKey = Get-DomainKey $tableName
        Columns = $columns.ToArray()
        PrimaryKey = $primaryKey.ToArray()
        Indexes = $indexes.ToArray()
        ForeignKeys = $foreignKeys.ToArray()
        Checks = $checks.ToArray()
    }
}

function Get-DbmlContent {
    param(
        [object[]]$Tables,
        [string]$DdlHash
    )

    $lines = [System.Collections.Generic.List[string]]::new()
    $lines.Add('// 자동 생성 파일, 직접 수정 금지')
    $lines.Add('// 생성 명령: scripts/db/mysql/erd/generate-erd.ps1')
    $lines.Add('')
    $lines.Add('Project sadari_database {')
    $lines.Add("  database_type: 'MySQL'")
    $lines.Add("  Note: 'scripts/db/mysql/01-create.sql 기준, SHA-256 $DdlHash'")
    $lines.Add('}')

    # 테이블별 DBML 정의 생성
    foreach ($table in $Tables) {
        $domain = Get-Domain $table.DomainKey
        $tableNotes = [System.Collections.Generic.List[string]]::new()
        $tableNotes.Add($table.Comment)

        # 검사 제약조건을 테이블 설명에 포함
        foreach ($check in $table.Checks) {
            $tableNotes.Add("CHECK $check")
        }

        $lines.Add('')
        $lines.Add("Table $($table.Name) [headercolor: $($domain.Color)] {")

        # 전체 컬럼과 속성 생성
        foreach ($column in $table.Columns) {
            $attributes = [System.Collections.Generic.List[string]]::new()

            # 단일 컬럼 기본키 속성 추가
            if ($table.PrimaryKey.Count -eq 1 -and $table.PrimaryKey[0] -eq $column.Name) {
                $attributes.Add('pk')
            }

            # 필수 컬럼 속성 추가
            if ($column.IsNotNull) {
                $attributes.Add('not null')
            }

            # 자동 증가 속성 추가
            if ($column.IsIncrement) {
                $attributes.Add('increment')
            }

            $dbmlDefault = Get-DbmlDefault $column.Default

            # 기본값 속성 추가
            if ($null -ne $dbmlDefault) {
                $attributes.Add("default: $dbmlDefault")
            }

            $columnNotes = [System.Collections.Generic.List[string]]::new()

            # 컬럼 코멘트 추가
            if (-not [string]::IsNullOrWhiteSpace($column.Comment)) {
                $columnNotes.Add($column.Comment)
            }

            # 생성 컬럼 정보 추가
            if ($column.IsGenerated) {
                $columnNotes.Add('생성 컬럼')
            }

            # 컬럼 설명 속성 추가
            if ($columnNotes.Count -gt 0) {
                $attributes.Add("note: '$(ConvertTo-DbmlText ($columnNotes -join '; '))'")
            }

            $attributeText = ''

            # DBML 컬럼 속성 목록 조립
            if ($attributes.Count -gt 0) {
                $attributeText = " [$($attributes -join ', ')]"
            }

            $lines.Add("  $($column.Name) $($column.Type)$attributeText")
        }

        $hasCompositePrimaryKey = $table.PrimaryKey.Count -gt 1
        $hasIndexes = $table.Indexes.Count -gt 0

        # 복합 기본키와 인덱스 영역 생성
        if ($hasCompositePrimaryKey -or $hasIndexes) {
            $lines.Add('')
            $lines.Add('  indexes {')

            # 복합 기본키 인덱스 추가
            if ($hasCompositePrimaryKey) {
                $lines.Add("    ($($table.PrimaryKey -join ', ')) [pk]")
            }

            # 일반 및 고유 인덱스 추가
            foreach ($index in $table.Indexes) {
                $indexAttributes = [System.Collections.Generic.List[string]]::new()

                # 고유 인덱스 속성 추가
                if ($index.IsUnique) {
                    $indexAttributes.Add('unique')
                }

                $indexAttributes.Add("name: '$($index.Name)'")
                $lines.Add("    ($($index.Columns -join ', ')) [$($indexAttributes -join ', ')]")
            }

            $lines.Add('  }')
        }

        $lines.Add("  Note: '$(ConvertTo-DbmlText ($tableNotes -join '; '))'")
        $lines.Add('}')
    }

    # 외래키 관계 생성
    foreach ($table in $Tables) {
        foreach ($foreignKey in $table.ForeignKeys) {
            $sourceColumns = if ($foreignKey.Columns.Count -eq 1) { $foreignKey.Columns[0] } else { "($($foreignKey.Columns -join ', '))" }
            $targetColumns = if ($foreignKey.TargetColumns.Count -eq 1) { $foreignKey.TargetColumns[0] } else { "($($foreignKey.TargetColumns -join ', '))" }
            $relationOptions = [System.Collections.Generic.List[string]]::new()

            # 삭제 동작 옵션 추가
            if ($null -ne $foreignKey.DeleteAction) {
                $relationOptions.Add("delete: $($foreignKey.DeleteAction)")
            }

            $optionText = ''

            # 외래키 옵션 목록 조립
            if ($relationOptions.Count -gt 0) {
                $optionText = " [$($relationOptions -join ', ')]"
            }

            $lines.Add('')
            $lines.Add("Ref $($foreignKey.Name): $($table.Name).$sourceColumns > $($foreignKey.TargetTable).$targetColumns$optionText")
        }
    }

    # 영역별 테이블 그룹 생성
    foreach ($domain in $domains) {
        $domainTables = @($Tables | Where-Object { $_.DomainKey -eq $domain.Key })

        # 빈 영역 제외
        if ($domainTables.Count -eq 0) {
            continue
        }

        $groupName = ($domain.Key -split '_') | ForEach-Object { (Get-Culture).TextInfo.ToTitleCase($_) }
        $lines.Add('')
        $lines.Add("TableGroup $($groupName -join '') {")

        # 영역 소속 테이블 추가
        foreach ($table in $domainTables) {
            $lines.Add("  $($table.Name)")
        }

        $lines.Add('}')
    }

    # 최종 DBML 문자열 반환
    return (($lines -join "`n") + "`n")
}

function Get-DotContent {
    param(
        [object[]]$Tables,
        [string]$DdlHash
    )

    $lines = [System.Collections.Generic.List[string]]::new()
    $lines.Add('digraph sadari_database {')
    $lines.Add('  graph [rankdir=TB, bgcolor="#F8FAFC", pad="0.35", nodesep="0.55", ranksep="1.2", splines=polyline, overlap=false, compound=true, newrank=true, fontname="Malgun Gothic", label="Sadari Database ERD", labelloc=t, fontsize=28];')
    $lines.Add('  node [shape=plain, fontname="Malgun Gothic"];')
    $lines.Add('  edge [color="#94A3B8", fontcolor="#475569", fontname="Malgun Gothic", fontsize=8, penwidth=0.9, arrowsize=0.7, dir=both, arrowtail=crow, arrowhead=tee];')
    $lines.Add("  // DDL SHA-256 $DdlHash")

    # 영역 클러스터와 테이블 노드 생성
    foreach ($domain in $domains) {
        $domainTables = @($Tables | Where-Object { $_.DomainKey -eq $domain.Key })

        # 빈 영역 제외
        if ($domainTables.Count -eq 0) {
            continue
        }

        $lines.Add('')
        $lines.Add("  subgraph cluster_$($domain.Key) {")
        $lines.Add(('    label="{0}";' -f (ConvertTo-HtmlText $domain.Label)))
        $lines.Add(('    color="{0}";' -f $domain.Color))
        $lines.Add('    penwidth=2;')
        $lines.Add('    style="rounded,dashed";')
        $lines.Add('    margin=24;')

        # 테이블별 전체 컬럼 노드 생성
        foreach ($table in $domainTables) {
            $lines.Add(('    "{0}" [label=<' -f $table.Name))
            $lines.Add('      <TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="5" COLOR="#CBD5E1" BGCOLOR="#FFFFFF">')
            $lines.Add(('        <TR><TD BGCOLOR="{0}" ALIGN="LEFT"><FONT COLOR="#FFFFFF" POINT-SIZE="14"><B>{1}</B></FONT></TD></TR>' -f $domain.Color, $table.Name))
            $lines.Add(('        <TR><TD BGCOLOR="#F1F5F9" ALIGN="LEFT"><FONT COLOR="#334155" POINT-SIZE="10"><I>{0}</I></FONT></TD></TR>' -f (ConvertTo-HtmlText $table.Comment)))

            # 전체 컬럼과 코멘트 표시
            foreach ($column in $table.Columns) {
                $flags = Get-ColumnFlags $table $column
                $flagPrefix = ''

                # 키와 필수 여부 플래그 표시
                if (-not [string]::IsNullOrWhiteSpace($flags)) {
                    $flagPrefix = '<FONT COLOR="{0}"><B>[{1}]</B></FONT> ' -f $domain.Color, (ConvertTo-HtmlText $flags)
                }

                $details = [System.Collections.Generic.List[string]]::new()

                # 컬럼 코멘트 표시
                if (-not [string]::IsNullOrWhiteSpace($column.Comment)) {
                    $details.Add($column.Comment)
                }

                # 기본값 표시
                if ($null -ne $column.Default -and $column.Default -ne 'NULL') {
                    $details.Add("DEFAULT $($column.Default)")
                }

                # 생성 컬럼 표시
                if ($column.IsGenerated) {
                    $details.Add('생성 컬럼')
                }

                $detailText = ConvertTo-HtmlText ($details -join ' · ')
                $lines.Add(('        <TR><TD PORT="{0}" ALIGN="LEFT">{1}<B>{0}</B> <FONT COLOR="#64748B">{2}</FONT><BR/><FONT COLOR="#475569" POINT-SIZE="9">{3}</FONT></TD></TR>' -f $column.Name, $flagPrefix, $column.Type, $detailText))
            }

            $lines.Add('      </TABLE>')
            $lines.Add('    >];')
        }

        $lines.Add('  }')
    }

    # 외래키 관계선 생성
    foreach ($table in $Tables) {
        foreach ($foreignKey in $table.ForeignKeys) {
            $sourcePort = $foreignKey.Columns[0]
            $targetPort = $foreignKey.TargetColumns[0]
            $deleteLabel = ''

            # 삭제 동작 레이블 추가
            if ($null -ne $foreignKey.DeleteAction) {
                $deleteLabel = " / $($foreignKey.DeleteAction)"
            }

            $edgeLabel = ConvertTo-HtmlText "$($foreignKey.Name)$deleteLabel"
            $lines.Add(('  "{0}":"{1}" -> "{2}":"{3}" [label="{4}"];' -f $table.Name, $sourcePort, $foreignKey.TargetTable, $targetPort, $edgeLabel))
        }
    }

    $lines.Add('}')

    # 최종 Graphviz 문자열 반환
    return (($lines -join "`n") + "`n")
}

# DDL과 출력 경로 유효성 확인
if (-not (Test-Path -LiteralPath $ddlPath -PathType Leaf)) {
    throw "DDL 파일을 찾을 수 없음: $ddlPath"
}

if (-not $outputDirectory.StartsWith($repoRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "ERD 출력 경로가 저장소 범위를 벗어남: $outputDirectory"
}

# DDL 원문과 해시 로드
$sql = Get-Content -Raw -Encoding UTF8 -LiteralPath $ddlPath
$ddlHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $ddlPath).Hash.ToLowerInvariant()
$tablePattern = '(?ms)CREATE TABLE `(?<name>[^`]+)`\s*\((?<body>.*?)\)\s+ENGINE=.*?COMMENT=''(?<comment>(?:''''|[^''])*)'';'
$tableMatches = [regex]::Matches($sql, $tablePattern)

# 스키마 테이블 미검출 차단
if ($tableMatches.Count -eq 0) {
    throw 'DDL에서 CREATE TABLE 정의를 찾지 못함'
}

$tables = [System.Collections.Generic.List[object]]::new()

# 전체 테이블 구조 모델 생성
foreach ($tableMatch in $tableMatches) {
    $tables.Add((Get-TableModel $tableMatch))
}

# 외래키 대상 테이블 존재 여부 검증
foreach ($table in $tables) {
    foreach ($foreignKey in $table.ForeignKeys) {
        $tableNames = @($tables | ForEach-Object { $_.Name })

        if ($tableNames -notcontains $foreignKey.TargetTable) {
            throw "외래키 대상 테이블 누락: $($foreignKey.Name) -> $($foreignKey.TargetTable)"
        }
    }
}

# ERD 출력 디렉터리 준비
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null

# DBML과 Graphviz 원본 생성
$dbmlContent = Get-DbmlContent $tables.ToArray() $ddlHash
$dotContent = Get-DotContent $tables.ToArray() $ddlHash
Set-Content -Encoding UTF8 -NoNewline -LiteralPath $dbmlPath -Value $dbmlContent
Set-Content -Encoding UTF8 -NoNewline -LiteralPath $dotPath -Value $dotContent

# 렌더링 의존성 설치 여부 확인
$vizModule = Join-Path $PSScriptRoot 'node_modules\@viz-js\viz'

if (-not (Test-Path -LiteralPath $vizModule -PathType Container)) {
    throw "SVG 렌더링 의존성 누락: $PSScriptRoot 위치에서 npm ci 실행 필요"
}

# Graphviz SVG 렌더링 실행
& node $rendererPath $dotPath $svgPath

if ($LASTEXITCODE -ne 0) {
    throw "ERD SVG 렌더링 실패, 종료 코드: $LASTEXITCODE"
}

$foreignKeyCount = (@($tables | ForEach-Object { $_.ForeignKeys }) | Measure-Object).Count
Write-Output "ERD 생성 완료: 테이블 $($tables.Count)개, 외래키 ${foreignKeyCount}개"
