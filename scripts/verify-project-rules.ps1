[CmdletBinding()]
param(
    [switch]$Full
)

$ErrorActionPreference = "Stop"

# 스크립트 위치를 기준으로 저장소 루트를 고정하여 다른 디렉터리의 파일을 검사하지 않게 한다
$scriptDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$repositoryRoot = [System.IO.Path]::GetFullPath((Join-Path $scriptDirectory ".."))
# Git 설정 값에서는 Windows 역슬래시가 이스케이프로 해석되지 않도록 슬래시 경로를 사용한다
$gitSafeRepositoryRoot = $repositoryRoot.Replace('\', '/')

# 여러 검증 실패를 한 번에 확인할 수 있도록 결과를 누적한다
$failures = [System.Collections.Generic.List[string]]::new()

<#
.SYNOPSIS
프로젝트 규칙 검증 실패 내용을 누적한다

.PARAMETER Message
누적할 실패 내용
#>
function Add-RuleFailure {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Message
    )

    # 전체 검증이 끝난 뒤 한 번에 보고할 실패 내용을 저장한다
    $failures.Add($Message)
}

<#
.SYNOPSIS
저장소 소유자 차이와 관계없이 현재 저장소에 한정하여 Git 명령을 실행한다

.PARAMETER Arguments
Git에 전달할 인자 목록

.OUTPUTS
Git 명령의 표준 출력 목록
#>
function Invoke-RepositoryGit {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    # 현재 저장소만 안전 경로로 지정하여 사용자 전역 Git 설정을 변경하지 않는다
    $output = & git -c "safe.directory=$gitSafeRepositoryRoot" -c "core.safecrlf=false" -C $repositoryRoot @Arguments 2>&1

    # Git 명령이 실패하면 불완전한 검사 결과가 성공으로 처리되지 않게 중단한다
    if ($LASTEXITCODE -ne 0) {
        # 실패한 Git 인자와 출력만 포함하여 원인을 확인할 수 있게 예외를 생성한다
        throw "Git 명령에 실패했습니다. arguments=$($Arguments -join ' '), output=$($output -join [Environment]::NewLine)"
    }

    # 호출자가 변경 파일과 검사 결과를 사용할 수 있도록 표준 출력을 반환한다
    return @($output)
}

Write-Host "[규칙 검사] 저장소: $repositoryRoot"

# 규칙 로딩 계약에 필요한 파일 목록을 정의한다
$requiredRuleFiles = @(
    "AGENTS.md"
    ".aiassistant/rules/coreRules.md"
    ".aiassistant/rules/javaRules.md"
    ".aiassistant/rules/sqlRules.md"
    ".aiassistant/rules/scriptRules.md"
    ".aiassistant/rules/viewRules.md"
    ".aiassistant/rules/reportRules.md"
    ".aiassistant/rules/deploymentRules.md"
)

# 규칙 문서 자체가 변경되면 해당 영역 규칙을 활성 목록에 표시한다
$ruleDocumentByPath = @{
    ".aiassistant/rules/coreRules.md"       = "coreRules.md"
    ".aiassistant/rules/javaRules.md"       = "javaRules.md"
    ".aiassistant/rules/sqlRules.md"        = "sqlRules.md"
    ".aiassistant/rules/scriptRules.md"     = "scriptRules.md"
    ".aiassistant/rules/viewRules.md"       = "viewRules.md"
    ".aiassistant/rules/reportRules.md"     = "reportRules.md"
    ".aiassistant/rules/deploymentRules.md" = "deploymentRules.md"
}

# 필수 규칙 파일이 모두 존재해야 선택 로딩이 누락 없이 동작한다
foreach ($relativePath in $requiredRuleFiles) {
    # 저장소 루트 아래의 명시된 규칙 파일만 검사한다
    $absolutePath = Join-Path $repositoryRoot $relativePath

    # 필수 파일이 없으면 이후 작업에서 규칙을 선택할 수 없으므로 실패로 기록한다
    if (-not (Test-Path -LiteralPath $absolutePath -PathType Leaf)) {
        # 누락된 저장소 상대 경로를 실패 목록에 추가한다
        Add-RuleFailure "필수 규칙 파일이 없습니다: $relativePath"
    }

}

# Core는 상시 적용되고 영역 규칙은 선택 적용되는지 Front Matter를 검사한다
$coreContent = Get-Content -Raw -LiteralPath (Join-Path $repositoryRoot ".aiassistant/rules/coreRules.md")
# 프로젝트 진입 문서가 Core와 단일 검증 명령을 안내하는지 확인한다
$agentsContent = Get-Content -Raw -LiteralPath (Join-Path $repositoryRoot "AGENTS.md")

# 진입 문서가 Core를 참조하지 않으면 선택 로딩의 안전 기준이 누락되므로 실패로 기록한다
if ($agentsContent -notmatch [regex]::Escape(".aiassistant/rules/coreRules.md")) {
    # 누락된 Core 참조를 실패 목록에 추가한다
    Add-RuleFailure "AGENTS.md가 coreRules.md를 참조하지 않습니다."
}

# 진입 문서가 단일 검증 명령을 참조하지 않으면 완료 검사가 작업자마다 달라지므로 실패로 기록한다
if ($agentsContent -notmatch [regex]::Escape("scripts/verify-project-rules.ps1")) {
    # 누락된 검증기 참조를 실패 목록에 추가한다
    Add-RuleFailure "AGENTS.md가 verify-project-rules.ps1을 참조하지 않습니다."
}

# Core 적용 범위가 바뀌면 모든 요청의 안전 규칙이 누락되므로 실패로 기록한다
if ($coreContent -notmatch "(?m)^apply:\s*always\s*$") {
    # 잘못된 Core 적용 범위를 실패 목록에 추가한다
    Add-RuleFailure "coreRules.md의 apply 값은 always여야 합니다."
}

# 영역 규칙의 선택 적용 상태를 확인할 파일 목록을 정의한다
$scopedRuleFiles = $requiredRuleFiles | Where-Object { $_ -like ".aiassistant/rules/*Rules.md" -and $_ -notlike "*coreRules.md" }

# 모든 영역 규칙이 상시 컨텍스트를 다시 비대하게 만들지 않도록 검사한다
foreach ($relativePath in $scopedRuleFiles) {
    # 영역 규칙의 Front Matter를 확인한다
    $ruleContent = Get-Content -Raw -LiteralPath (Join-Path $repositoryRoot $relativePath)

    # 선택 적용 표시가 없으면 규칙 라우팅 계약이 깨지므로 실패로 기록한다
    if ($ruleContent -notmatch "(?m)^apply:\s*scoped\s*$") {
        # 잘못된 영역 규칙 적용 범위를 실패 목록에 추가한다
        Add-RuleFailure "$relativePath 파일의 apply 값은 scoped여야 합니다."
    }

}

# 각 규칙 문서의 표에서 안정적인 규칙 ID를 수집한다
$ruleIds = [System.Collections.Generic.List[string]]::new()

# 규칙 ID 색인이 빠졌거나 중복되지 않았는지 전수 검사한다
foreach ($relativePath in $requiredRuleFiles | Where-Object { $_ -like ".aiassistant/rules/*.md" }) {
    # 규칙 문서의 표에 선언된 ID만 수집하여 본문 참조와 구분한다
    $ruleContent = Get-Content -Raw -LiteralPath (Join-Path $repositoryRoot $relativePath)
    # 현재 규칙 문서의 안정적인 ID 선언을 추출한다
    $matches = [regex]::Matches($ruleContent, '(?m)^\| `([A-Z]+-[A-Z]+-\d{3})` \|')

    # 규칙 ID 색인이 없으면 완료 보고에서 규칙을 안정적으로 참조할 수 없으므로 실패로 기록한다
    if ($matches.Count -eq 0) {
        # 색인이 없는 규칙 문서를 실패 목록에 추가한다
        Add-RuleFailure "$relativePath 파일에 Rule ID Index가 없습니다."
    }

    # 문서에서 찾은 모든 규칙 ID를 전체 중복 검사 목록에 추가한다
    foreach ($match in $matches) {
        # 정규식의 첫 번째 그룹에 있는 규칙 ID를 저장한다
        $ruleIds.Add($match.Groups[1].Value)
    }

}

# 같은 규칙 ID가 여러 의미로 사용되지 않도록 중복을 찾는다
$duplicateRuleIds = $ruleIds | Group-Object | Where-Object { $_.Count -gt 1 }

# 중복 ID는 완료 보고와 자동 검사 대상을 모호하게 하므로 실패로 기록한다
foreach ($duplicateRuleId in $duplicateRuleIds) {
    # 중복 횟수와 ID를 실패 목록에 추가한다
    Add-RuleFailure "중복 규칙 ID가 있습니다: $($duplicateRuleId.Name), count=$($duplicateRuleId.Count)"
}

# 추적 파일과 새 파일을 합쳐 이번 작업의 변경 범위를 계산한다
$trackedChanges = Invoke-RepositoryGit @("diff", "--name-only", "--diff-filter=ACMR", "HEAD", "--")
# 아직 Git이 추적하지 않는 새 파일도 활성 규칙 판정에 포함한다
$untrackedChanges = Invoke-RepositoryGit @("ls-files", "--others", "--exclude-standard")
# 빈 줄과 중복 경로를 제거하여 실제 변경 파일 목록을 만든다
$changedFiles = @($trackedChanges + $untrackedChanges) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Sort-Object -Unique

# 변경 파일 확장자와 역할에 따라 추가로 읽어야 하는 영역 규칙을 계산한다
$activeRuleDocuments = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
# 모든 작업에 Core 규칙을 적용한다
$null = $activeRuleDocuments.Add("coreRules.md")

# 각 변경 파일이 요구하는 영역 규칙을 라우팅 표와 같은 기준으로 판정한다
foreach ($relativePath in $changedFiles) {
    # Windows와 Git 경로 구분자 차이를 없애 규칙 판정을 일관되게 한다
    $normalizedPath = $relativePath.Replace('\', '/')
    # 파일 확장자를 소문자로 통일하여 대소문자 차이로 규칙이 빠지지 않게 한다
    $extension = [System.IO.Path]::GetExtension($normalizedPath).ToLowerInvariant()

    # 변경된 규칙 문서는 자신의 영역을 활성 목록에 직접 추가한다
    if ($ruleDocumentByPath.ContainsKey($normalizedPath)) {
        # 규칙 문서 경로에 대응하는 활성 문서를 추가한다
        $null = $activeRuleDocuments.Add($ruleDocumentByPath[$normalizedPath])
    }

    # Java 소스에는 Java 영역 규칙을 적용한다
    if ($extension -eq ".java") {
        # Java 규칙 문서를 활성 목록에 추가한다
        $null = $activeRuleDocuments.Add("javaRules.md")
    }

    # SQL 파일과 DB 스크립트 및 MyBatis XML에는 SQL 영역 규칙을 적용한다
    if ($extension -eq ".sql" -or $normalizedPath.StartsWith("scripts/db/") -or ($extension -eq ".xml" -and $normalizedPath -match "(?i)mapper")) {
        # SQL 규칙 문서를 활성 목록에 추가한다
        $null = $activeRuleDocuments.Add("sqlRules.md")
    }

    # TypeScript, JavaScript 및 셸 스크립트에는 Script 영역 규칙을 적용한다
    if ($extension -in @(".ts", ".tsx", ".js", ".jsx", ".ps1", ".psm1", ".sh")) {
        # Script 규칙 문서를 활성 목록에 추가한다
        $null = $activeRuleDocuments.Add("scriptRules.md")
    }

    # React 화면과 스타일 파일에는 View 규칙을 적용한다
    if ($extension -in @(".tsx", ".jsx", ".css", ".scss")) {
        # View 규칙 문서를 활성 목록에 추가한다
        $null = $activeRuleDocuments.Add("viewRules.md")
    }

    # docs 아래 보고성 Markdown에는 Report 규칙을 적용한다
    if ($extension -eq ".md" -and $normalizedPath.StartsWith("docs/")) {
        # Report 규칙 문서를 활성 목록에 추가한다
        $null = $activeRuleDocuments.Add("reportRules.md")
    }

    # 배포 및 환경설정 파일에는 Deployment 규칙을 적용한다
    if ($extension -in @(".yml", ".yaml") -or $normalizedPath -match "(?i)(^|/)(Dockerfile|docker-compose[^/]*)$") {
        # Deployment 규칙 문서를 활성 목록에 추가한다
        $null = $activeRuleDocuments.Add("deploymentRules.md")
    }

}

Write-Host "[규칙 검사] 변경 파일: $($changedFiles.Count)개"
Write-Host "[규칙 검사] 활성 문서: $((@($activeRuleDocuments) | Sort-Object) -join ', ')"

# Git 공백 오류는 실패 출력까지 수집할 수 있도록 별도로 실행한다
$previousErrorActionPreference = $ErrorActionPreference
# Native stderr가 PowerShell 예외로 승격되어 검사 결과가 사라지지 않게 일시적으로 계속 진행한다
$ErrorActionPreference = "Continue"
# 현재 저장소의 공백 오류와 충돌 표식을 검사한다
$diffCheckOutput = & git -c "safe.directory=$gitSafeRepositoryRoot" -c "core.safecrlf=false" -C $repositoryRoot diff --check HEAD -- 2>&1
# Git 종료 코드를 이후 복원과 관계없이 보존한다
$diffCheckExitCode = $LASTEXITCODE
# 나머지 검증은 다시 엄격한 오류 처리 정책을 사용한다
$ErrorActionPreference = $previousErrorActionPreference

# Git이 공백 오류를 찾으면 구체적인 위치와 함께 실패로 기록한다
if ($diffCheckExitCode -ne 0) {
    # Git의 구체적인 위치 정보를 실패 목록에 추가한다
    Add-RuleFailure "git diff --check 실패: $($diffCheckOutput -join [Environment]::NewLine)"
}

# 변경된 SQL과 MyBatis 파일에서 SELECT 전체 컬럼 조회를 검사한다
foreach ($relativePath in $changedFiles | Where-Object { $_ -match '(?i)\.(sql|xml)$' }) {
    # 삭제되거나 저장소 밖인 경로를 검사하지 않도록 실제 파일 경계를 확인한다
    $absolutePath = [System.IO.Path]::GetFullPath((Join-Path $repositoryRoot $relativePath))

    # 변경 파일이 저장소 루트 안의 일반 파일일 때만 내용을 검사한다
    if ($absolutePath.StartsWith($repositoryRoot, [System.StringComparison]::OrdinalIgnoreCase) -and (Test-Path -LiteralPath $absolutePath -PathType Leaf)) {
        # 줄바꿈을 포함한 SELECT 전체 컬럼 사용을 찾는다
        $sqlContent = Get-Content -Raw -LiteralPath $absolutePath

        # 필요한 컬럼을 명시하지 않은 조회는 계약과 성능을 불명확하게 하므로 실패로 기록한다
        if ($sqlContent -match '(?is)\bSELECT\s+\*') {
            # 위반 파일을 저장소 상대 경로로 보고한다
            Add-RuleFailure "SELECT * 사용을 확인해 주세요: $relativePath"
        }

    }

}

# 변경된 Java 스케줄러가 환경별 cron을 코드에 고정하지 않았는지 검사한다
foreach ($relativePath in $changedFiles | Where-Object { $_ -match '(?i)\.java$' }) {
    # 저장소 루트 아래의 실제 Java 파일만 검사한다
    $absolutePath = [System.IO.Path]::GetFullPath((Join-Path $repositoryRoot $relativePath))

    # 존재하는 변경 Java 파일에만 스케줄러 검사를 적용한다
    if ($absolutePath.StartsWith($repositoryRoot, [System.StringComparison]::OrdinalIgnoreCase) -and (Test-Path -LiteralPath $absolutePath -PathType Leaf)) {
        # 어노테이션의 cron 값이 Placeholder인지 확인한다
        $javaContent = Get-Content -Raw -LiteralPath $absolutePath

        # 문자열로 고정한 cron은 프로필별 설정 동기화를 막으므로 실패로 기록한다
        if ($javaContent -match '@Scheduled\s*\(\s*cron\s*=\s*"(?!\$\{)') {
            # 위반 파일을 저장소 상대 경로로 보고한다
            Add-RuleFailure "@Scheduled cron을 YML Placeholder로 변경해야 합니다: $relativePath"
        }

    }

}

# 변경된 Java 파일에서 프로젝트 제한보다 긴 메서드명을 검사한다
foreach ($relativePath in $changedFiles | Where-Object { $_ -match '(?i)\.java$' }) {
    # 존재하는 저장소 내부 Java 파일만 명명 검사를 수행한다
    $absolutePath = [System.IO.Path]::GetFullPath((Join-Path $repositoryRoot $relativePath))

    # 삭제된 파일과 저장소 밖 경로는 검사하지 않는다
    if ($absolutePath.StartsWith($repositoryRoot, [System.StringComparison]::OrdinalIgnoreCase) -and (Test-Path -LiteralPath $absolutePath -PathType Leaf)) {
        # 선언문 형태만 찾도록 제어문과 호출문을 제외한 메서드 패턴을 사용한다
        $javaContent = Get-Content -Raw -LiteralPath $absolutePath
        # Java 선언부에서 반환 타입 다음의 메서드명을 추출한다
        $methodMatches = [regex]::Matches($javaContent, '(?m)^\s*(?!(?:if|for|while|switch|catch|return|throw|new)\b)(?:(?:public|protected|private|static|final|synchronized|abstract|native|default)\s+)*[A-Za-z_$][\w$<>\[\], ?.@]*\s+([A-Za-z_$][\w$]*)\s*\([^;{}]*\)\s*(?:throws\s+[^;{]+)?[;{]')

        # 추출한 메서드명을 프로젝트 25자 제한과 대조한다
        foreach ($methodMatch in $methodMatches) {
            # 정규식 첫 번째 그룹에서 실제 메서드명을 가져온다
            $methodName = $methodMatch.Groups[1].Value

            # 외부 계약 여부는 자동 판정할 수 없으므로 긴 이름을 검토 실패로 표시한다
            if ($methodName.Length -gt 25) {
                # 위반 파일과 메서드명 및 길이를 실패 목록에 추가한다
                Add-RuleFailure "25자를 초과한 Java 메서드명입니다: $relativePath, $methodName, length=$($methodName.Length)"
            }

        }

    }

}

# 변경된 TypeScript와 JavaScript 파일에서 이름 있는 함수의 길이를 검사한다
foreach ($relativePath in $changedFiles | Where-Object { $_ -match '(?i)\.(ts|tsx|js|jsx)$' }) {
    # 존재하는 저장소 내부 Script 파일만 명명 검사를 수행한다
    $absolutePath = [System.IO.Path]::GetFullPath((Join-Path $repositoryRoot $relativePath))

    # 삭제된 파일과 저장소 밖 경로는 검사하지 않는다
    if ($absolutePath.StartsWith($repositoryRoot, [System.StringComparison]::OrdinalIgnoreCase) -and (Test-Path -LiteralPath $absolutePath -PathType Leaf)) {
        # 함수 선언문과 함수 값을 받는 변수 선언에서 이름을 추출한다
        $scriptContent = Get-Content -Raw -LiteralPath $absolutePath
        # 일반 함수 선언의 이름을 찾는다
        $functionMatches = [regex]::Matches($scriptContent, '\bfunction\s+([A-Za-z_$][\w$]*)\s*\(')
        # 화살표 함수와 함수 표현식을 담는 변수 이름을 찾는다
        $variableFunctionMatches = [regex]::Matches($scriptContent, '\b(?:const|let)\s+([A-Za-z_$][\w$]*)\s*(?::[^=\r\n]+)?=\s*(?:async\s*)?(?:<[^>]+>\s*)?\(')

        # 서로 다른 선언 패턴에서 찾은 함수명을 하나의 목록으로 합친다
        $functionNames = @($functionMatches + $variableFunctionMatches) | ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique

        # 추출한 함수명을 프로젝트 25자 제한과 대조한다
        foreach ($functionName in $functionNames) {
            # 외부 라이브러리 계약 여부는 자동 판정할 수 없으므로 긴 이름을 검토 실패로 표시한다
            if ($functionName.Length -gt 25) {
                # 위반 파일과 함수명 및 길이를 실패 목록에 추가한다
                Add-RuleFailure "25자를 초과한 Script 함수명입니다: $relativePath, $functionName, length=$($functionName.Length)"
            }

        }

    }

}

# 변경된 MyBatis XML의 쿼리 ID가 Java Mapper 계약에 존재하는지 검사한다
$changedMapperFiles = $changedFiles | Where-Object { $_ -match '(?i)mapper.*\.xml$|\.xml$' }

# Mapper XML이 변경된 경우에만 Java Mapper 전체 선언을 한 번 읽는다
if (@($changedMapperFiles).Count -gt 0) {
    # Mapper 계약 검색에 사용할 Java 소스 목록을 조회한다
    $javaSourceFiles = Get-ChildItem -LiteralPath (Join-Path $repositoryRoot "src/main/java") -Recurse -File -Filter "*.java"
    # 쿼리 ID가 메서드 호출과 우연히 일치하는 범위를 줄이도록 Java 선언 텍스트를 결합한다
    $javaSourceContent = ($javaSourceFiles | ForEach-Object { Get-Content -Raw -LiteralPath $_.FullName }) -join [Environment]::NewLine

    # 변경된 각 XML의 CRUD 쿼리 ID를 Java 선언과 대조한다
    foreach ($relativePath in $changedMapperFiles) {
        # 존재하는 저장소 내부 XML 파일만 계약 검사를 수행한다
        $absolutePath = [System.IO.Path]::GetFullPath((Join-Path $repositoryRoot $relativePath))

        # 삭제된 파일과 저장소 밖 경로는 검사하지 않는다
        if ($absolutePath.StartsWith($repositoryRoot, [System.StringComparison]::OrdinalIgnoreCase) -and (Test-Path -LiteralPath $absolutePath -PathType Leaf)) {
            # MyBatis CRUD 태그에 선언된 ID를 추출한다
            $mapperContent = Get-Content -Raw -LiteralPath $absolutePath
            # select, insert, update 및 delete 태그의 ID를 찾는다
            $mapperIdMatches = [regex]::Matches($mapperContent, '<(?:select|insert|update|delete)\b[^>]*\bid\s*=\s*"([A-Za-z_$][\w$]*)"')

            # 각 쿼리 ID가 Java 메서드 선언 형태로 존재하는지 확인한다
            foreach ($mapperIdMatch in $mapperIdMatches) {
                # 정규식 첫 번째 그룹에서 Mapper ID를 가져온다
                $mapperId = $mapperIdMatch.Groups[1].Value

                # Java 계층에 같은 이름의 호출 계약이 없으면 Mapper 계약 불일치로 기록한다
                if ($javaSourceContent -notmatch ("\b" + [regex]::Escape($mapperId) + "\s*\(")) {
                    # 위반 파일과 Mapper ID를 실패 목록에 추가한다
                    Add-RuleFailure "Java Mapper 계약을 찾을 수 없습니다: $relativePath, id=$mapperId"
                }

            }

        }

    }

}

# 백엔드와 프론트엔드의 기본 언어 및 영어 메시지 키 집합을 검사한다
$messageFilePairs = @(
    @("src/main/resources/messages.properties", "src/main/resources/messages_en.properties")
    @("src/main/frontend/src/app/messages/messages.properties", "src/main/frontend/src/app/messages/messages_en.properties")
)

# 각 메시지 파일 쌍의 중복 키와 언어별 누락 키를 검사한다
foreach ($messageFilePair in $messageFilePairs) {
    # 기본 언어와 영어 파일의 절대 경로를 계산한다
    $defaultMessagePath = Join-Path $repositoryRoot $messageFilePair[0]
    # 영어 메시지 파일 경로를 계산한다
    $englishMessagePath = Join-Path $repositoryRoot $messageFilePair[1]

    # 두 파일이 모두 있을 때만 키 집합을 비교한다
    if ((Test-Path -LiteralPath $defaultMessagePath -PathType Leaf) -and (Test-Path -LiteralPath $englishMessagePath -PathType Leaf)) {
        # Properties 형식의 기본 언어 키를 추출한다
        $defaultKeyMatches = [regex]::Matches((Get-Content -Raw -LiteralPath $defaultMessagePath), '(?m)^\s*([A-Za-z0-9_.-]+)\s*[=:]')
        # Properties 형식의 영어 키를 추출한다
        $englishKeyMatches = [regex]::Matches((Get-Content -Raw -LiteralPath $englishMessagePath), '(?m)^\s*([A-Za-z0-9_.-]+)\s*[=:]')
        # 기본 언어 키 목록을 만든다
        $defaultKeys = @($defaultKeyMatches | ForEach-Object { $_.Groups[1].Value })
        # 영어 키 목록을 만든다
        $englishKeys = @($englishKeyMatches | ForEach-Object { $_.Groups[1].Value })

        # 기본 언어 파일의 중복 키를 찾는다
        $duplicateDefaultKeys = $defaultKeys | Group-Object | Where-Object { $_.Count -gt 1 }
        # 영어 파일의 중복 키를 찾는다
        $duplicateEnglishKeys = $englishKeys | Group-Object | Where-Object { $_.Count -gt 1 }

        # 기본 언어 중복 키는 마지막 값으로 조용히 덮어써질 수 있으므로 실패로 기록한다
        foreach ($duplicateDefaultKey in $duplicateDefaultKeys) {
            # 중복 파일과 키를 실패 목록에 추가한다
            Add-RuleFailure "중복 메시지 키가 있습니다: $($messageFilePair[0]), key=$($duplicateDefaultKey.Name)"
        }

        # 영어 중복 키는 마지막 값으로 조용히 덮어써질 수 있으므로 실패로 기록한다
        foreach ($duplicateEnglishKey in $duplicateEnglishKeys) {
            # 중복 파일과 키를 실패 목록에 추가한다
            Add-RuleFailure "중복 메시지 키가 있습니다: $($messageFilePair[1]), key=$($duplicateEnglishKey.Name)"
        }

        # 언어별 키 집합의 차이를 양방향으로 찾는다
        $messageKeyDifferences = Compare-Object -ReferenceObject ($defaultKeys | Sort-Object -Unique) -DifferenceObject ($englishKeys | Sort-Object -Unique)

        # 어느 한 언어에만 있는 키는 사용자 문구 계약 불일치로 기록한다
        foreach ($messageKeyDifference in $messageKeyDifferences) {
            # 차이가 난 키와 존재하는 언어 방향을 실패 목록에 추가한다
            Add-RuleFailure "언어별 메시지 키가 일치하지 않습니다: pair=$($messageFilePair -join ', '), key=$($messageKeyDifference.InputObject), side=$($messageKeyDifference.SideIndicator)"
        }

    }

}

# 변경된 application YML의 환경변수가 배포 문서에 기록되어 있는지 검사한다
$changedApplicationYmlFiles = $changedFiles | Where-Object { $_ -match '(?i)(^|/)application(?:-[^/]*)?\.ya?ml$' }

# 환경설정이 변경된 경우 운영 문서와 이름을 대조한다
if (@($changedApplicationYmlFiles).Count -gt 0) {
    # 배포 문서가 없으면 설정 동기화를 검증할 수 없으므로 실패로 기록한다
    $deploymentDocumentPath = Join-Path $repositoryRoot "docs/github-actions-deployment.md"

    # 배포 문서 존재 여부에 따라 상세 환경변수 검사를 분기한다
    if (-not (Test-Path -LiteralPath $deploymentDocumentPath -PathType Leaf)) {
        # 누락된 배포 문서를 실패 목록에 추가한다
        Add-RuleFailure "application YML 변경에 필요한 배포 문서가 없습니다: docs/github-actions-deployment.md"
    }
    else {
        # 환경변수 이름 검색에 사용할 배포 문서 내용을 읽는다
        $deploymentDocumentContent = Get-Content -Raw -LiteralPath $deploymentDocumentPath

        # 변경된 각 YML의 환경변수 Placeholder를 배포 문서와 대조한다
        foreach ($relativePath in $changedApplicationYmlFiles) {
            # 존재하는 저장소 내부 YML 파일만 검사한다
            $absolutePath = [System.IO.Path]::GetFullPath((Join-Path $repositoryRoot $relativePath))

            # 삭제된 파일과 저장소 밖 경로는 검사하지 않는다
            if ($absolutePath.StartsWith($repositoryRoot, [System.StringComparison]::OrdinalIgnoreCase) -and (Test-Path -LiteralPath $absolutePath -PathType Leaf)) {
                # YML에서 대문자 환경변수 Placeholder 이름을 추출한다
                $environmentMatches = [regex]::Matches((Get-Content -Raw -LiteralPath $absolutePath), '\$\{([A-Z][A-Z0-9_]*)(?::[^}]*)?\}')
                # 같은 환경변수를 한 번만 검사한다
                $environmentNames = @($environmentMatches | ForEach-Object { $_.Groups[1].Value }) | Sort-Object -Unique

                # 각 환경변수가 운영 문서에 이름 그대로 기록되어 있는지 확인한다
                foreach ($environmentName in $environmentNames) {
                    # 운영 문서에 없는 환경변수는 배포 누락 위험으로 기록한다
                    if ($deploymentDocumentContent -notmatch ("\b" + [regex]::Escape($environmentName) + "\b")) {
                        # 위반 YML과 환경변수 이름을 실패 목록에 추가한다
                        Add-RuleFailure "배포 문서에 환경변수가 없습니다: $relativePath, $environmentName"
                    }

                }

            }

        }

    }

}

# 전체 검사가 요청되면 백엔드와 프론트엔드의 기존 품질 명령을 이어서 실행한다
if ($Full) {
    Write-Host "[전체 검사] Gradle 테스트를 실행합니다."
    # 프로젝트 Wrapper를 사용하여 동일한 Gradle 버전으로 테스트한다
    & (Join-Path $repositoryRoot "gradlew.bat") test

    # 백엔드 테스트가 실패하면 전체 검증 실패로 기록한다
    if ($LASTEXITCODE -ne 0) {
        # Gradle 테스트 실패를 최종 결과에 포함한다
        Add-RuleFailure "gradlew.bat test에 실패했습니다."
    }

    # 프론트엔드 명령의 실행 위치를 고정한다
    $frontendDirectory = Join-Path $repositoryRoot "src/main/frontend"
    # 원래 실행 위치를 항상 복원하도록 프론트엔드 검사를 보호한다
    Push-Location -LiteralPath $frontendDirectory

    try {
        Write-Host "[전체 검사] Frontend lint를 실행합니다."
        # 프로젝트 ESLint 설정으로 정적 오류와 경고를 검사한다
        & npm.cmd run lint

        # lint가 실패하면 전체 검증 실패로 기록한다
        if ($LASTEXITCODE -ne 0) {
            # lint 실패를 최종 결과에 포함한다
            Add-RuleFailure "npm run lint에 실패했습니다."
        }

        Write-Host "[전체 검사] TypeScript 검사를 실행합니다."
        # 출력 파일을 만들지 않고 TypeScript 계약을 검사한다
        & npx.cmd tsc --noEmit

        # TypeScript 검사가 실패하면 전체 검증 실패로 기록한다
        if ($LASTEXITCODE -ne 0) {
            # TypeScript 실패를 최종 결과에 포함한다
            Add-RuleFailure "npx tsc --noEmit에 실패했습니다."
        }

        Write-Host "[전체 검사] Frontend build를 실행합니다."
        # 실제 번들 생성 경로까지 확인하여 화면 변경의 빌드 가능성을 검증한다
        & npm.cmd run build

        # 프론트엔드 빌드가 실패하면 전체 검증 실패로 기록한다
        if ($LASTEXITCODE -ne 0) {
            # 빌드 실패를 최종 결과에 포함한다
            Add-RuleFailure "npm run build에 실패했습니다."
        }

    }
    finally {
        # 이후 명령이 프론트엔드 디렉터리에서 실행되지 않도록 원래 위치를 복원한다
        Pop-Location
    }

}

# 하나 이상의 실패가 있으면 모든 내용을 출력하고 실패 종료 코드를 반환한다
if ($failures.Count -gt 0) {
    Write-Host "[규칙 검사] 실패 $($failures.Count)건" -ForegroundColor Red

    # 사용자가 한 번에 수정할 수 있도록 누적 실패를 모두 표시한다
    foreach ($failure in $failures) {
        Write-Host "- $failure" -ForegroundColor Red
    }

    # 자동화에서 실패를 감지할 수 있도록 비정상 종료한다
    exit 1
}

Write-Host "[규칙 검사] 통과" -ForegroundColor Green
exit 0
