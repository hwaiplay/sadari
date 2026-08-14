package org.our.sadari.readingClub.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.book.mapper.BookMapper;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.exception.CustomException;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.service.BadWordDetectionService;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.readingClub.dto.ReadingClubDto;
import org.our.sadari.readingClub.mapper.ReadingClubMapper;
import org.our.sadari.report.dto.ReportDto;
import org.our.sadari.report.mapper.ReportMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : ReadingClubServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-05
 * description    : 독서 모임 생성, 탐색, 가입, 초대와 승인 업무를 처리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-05        SeungHyeon.Kang    최초 생성
 * 2026-08-14        SeungHyeon.Kang    활성 모임원 프로필 접근 정책 적용
 * 2026-08-14        Hanwon.Jang        회원 초대 알림 발송 연동
 * 2026-08-14        Hanwon.Jang        모임 수정과 물리 삭제 처리 추가
 * 2026-08-14        Hanwon.Jang        회차와 활성 멤버별 읽는 중 독후감 일괄 등록 추가
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingClubServiceImpl implements ReadingClubService {

    // 공개 모임 공개 범위 코드
    private static final String CLUB_PUBLIC = "PUBLIC";
    // 비공개 모임 공개 범위 코드
    private static final String CLUB_PRIVATE = "PRIVATE";
    // 승인 없이 즉시 가입하는 가입 방식 코드
    private static final String JOIN_OPEN = "OPEN";
    // 모임장 승인이 필요한 가입 방식 코드
    private static final String JOIN_APPROVAL = "APPROVAL";
    // 맞팔로우 초대만 허용하는 가입 방식 코드
    private static final String JOIN_INVITE = "INVITE";
    // 운영 중인 모임 상태 코드
    private static final String CLUB_ACTIVE = "ACTIVE";
    // 현재 모임에 참여 중인 활성 모임원 상태 코드
    private static final String MEMBER_ACTIVE = "ACTIVE";
    // 승인된 가입 신청 상태 코드
    private static final String APPLICATION_APPROVED = "APPROVED";
    // 거절된 가입 신청 상태 코드
    private static final String APPLICATION_REJECTED = "REJECTED";

    // 독서 모임 데이터베이스 접근 Mapper
    private final ReadingClubMapper readingClubMapper;
    // 사용자 입력 비속어 검사 서비스
    private final BadWordDetectionService badWordDetectionService;
    // 사용자 알림과 푸시 발송 서비스
    private final AlimService alimService;
    // 도서 마스터 데이터 접근 Mapper
    private final BookMapper bookMapper;
    // 멤버별 독후감 데이터 접근 Mapper
    private final ReportMapper reportMapper;
    // 독후감 기본 책갈피 색상 공통코드 조회 도구
    private final CodeUtil codeUtil;

    /**
     * {@inheritDoc}
     *
     * @author Hanwon.Jang
     * @param userNumb 등록을 요청한 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @param request 선택 도서와 목표 독서 기간
     * @return 생성된 회차 번호
     */
    @Override
    @Transactional
    public ResultData setReading(Long userNumb, Long clubNumb, ReadingClubDto.ReadingCreateReqDto request) {

        // 모임과 등록 요청의 필수 참조값이 없으면 저장을 시작하지 않는다
        if (StringUtil.hasEmpty(userNumb, clubNumb, request) || !isValidReadingRequest(request)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 같은 모임의 회차 번호 계산과 동시 등록을 직렬화하기 위해 모임 행을 잠근다
        ReadingClubDto.ClubViewDto club = readingClubMapper.getClubForUpdate(clubNumb);
        // 활성 계정인 현재 모임장만 독서를 등록할 수 있다
        if (StringUtil.isEmpty(club) || !CLUB_ACTIVE.equals(club.getClubStat())
                || readingClubMapper.getActiveOwnerCnt(clubNumb, userNumb) == 0) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 네트워크 재시도로 같은 요청 키가 전달되면 기존 회차를 다시 반환한다
        Long existingRondNumb = readingClubMapper.getReadingRoundByIdempotency(clubNumb, request.getIdemKeyx());
        if (!StringUtil.isEmpty(existingRondNumb)) {
            // 이미 생성된 회차 번호를 성공 결과로 반환한다
            return ResultData.success(Map.of("rondNumb", existingRondNumb));
        }

        // 예정 또는 진행 중인 독서가 있으면 중복 회차 생성을 차단한다
        if (readingClubMapper.getOngoingRoundCnt(clubNumb) > 0) {
            // "저장할 수 없어요. 입력 내용을 확인해주세요."
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
        }

        // 계정과 멤버 관계가 모두 활성인 사용자만 이번 회차에 자동 참여시킨다
        List<Long> memberUserNumbList = readingClubMapper.getActiveMemberUserNumbList(clubNumb);
        // 모임장이 포함된 활성 멤버 목록이 없으면 불완전한 회차를 만들지 않는다
        if (StringUtil.isEmpty(memberUserNumbList) || memberUserNumbList.isEmpty()
                || !memberUserNumbList.contains(userNumb)) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 자동 생성 독후감에 사용할 활성 책갈피 색상의 첫 값을 조회한다
        String reportColor = codeUtil.getFirstCode(Constant.CODE_BOOK_COLR);
        if (StringUtil.isEmpty(reportColor)) {
            // 설정 누락은 부분 저장 없이 서버 오류로 롤백한다
            throw new CustomException(ResultEnum.COMMON_SAVE_REJECTED, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // ISBN 기준으로 등록된 도서가 없을 때만 도서 마스터를 생성한다
        if (bookMapper.dupBook(request) == 0) {
            // 신규 도서 마스터를 저장한다
            int savedBookCnt = bookMapper.setBook(request);
            if (savedBookCnt != 1 || StringUtil.isEmpty(request.getBookNumb())) {
                // 도서 마스터 생성 실패는 전체 등록을 롤백한다
                throw new CustomException(ResultEnum.COMMON_SAVE_REJECTED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            // 기존 ISBN의 도서 번호를 이번 회차에 연결한다
            request.setBookNumb(bookMapper.getBookNumbByIsbn(request.getBookIsbn()));
        }

        // 기존 도서 조회 결과가 없으면 외래키가 없는 회차 생성을 차단한다
        if (StringUtil.isEmpty(request.getBookNumb())) {
            // 도서 연결 실패는 전체 등록을 롤백한다
            throw new CustomException(ResultEnum.COMMON_SAVE_REJECTED, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 잠긴 모임 범위에서 다음 회차 번호를 계산한다
        request.setRondNumb(readingClubMapper.getNextReadingRoundNumb(clubNumb));
        if (StringUtil.isEmpty(request.getRondNumb())
                || readingClubMapper.setReadingRound(clubNumb, userNumb, request) != 1) {
            // 회차 생성 실패는 도서와 멤버 독후감까지 모두 롤백한다
            throw new CustomException(ResultEnum.COMMON_SAVE_REJECTED, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        long partNumb = 1L;
        // 활성 멤버마다 같은 도서와 목표 기간의 읽는 중 독후감을 만든다
        for (Long memberUserNumb : memberUserNumbList) {
            // 현재 멤버의 자동 생성 독후감 값을 구성한다
            ReportDto report = toReadingReport(memberUserNumb, request, reportColor);
            if (reportMapper.setReport(report) != 1 || StringUtil.isEmpty(report.getReptNumb())) {
                // 멤버 한 명의 독후감 생성 실패도 전체 등록을 롤백한다
                throw new CustomException(ResultEnum.COMMON_SAVE_REJECTED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 생성된 독후감을 이번 모임 독서 참여 정보와 연결한다
            int savedParticipantCnt = readingClubMapper.setReadingParticipant(
                    clubNumb, request.getRondNumb(), partNumb, memberUserNumb, report.getReptNumb());
            if (savedParticipantCnt != 1) {
                // 참여 연결 실패는 회차와 모든 멤버 독후감을 롤백한다
                throw new CustomException(ResultEnum.COMMON_SAVE_REJECTED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            partNumb++;
        }

        // 생성된 회차 번호를 등록 완료 결과로 반환한다
        return ResultData.success(Map.of("rondNumb", request.getRondNumb()));
    }

    /**
     * {@inheritDoc}
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 내 모임 목록 조회 결과
     */
    @Override
    public ResultData getMyClubList(Long userNumb) {
        // 인증 사용자가 없으면 모임 관계를 조회하지 않는다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요. 다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 로그인 사용자가 활성 회원으로 참여 중인 모임을 조회한다
        List<ReadingClubDto.ClubViewDto> clubs = readingClubMapper.getMyClubList(userNumb);
        // 조회한 각 모임에 카테고리 표시 정보를 결합한다
        List<ReadingClubDto.ClubViewDto> result = fillClubRelations(clubs, false);
        // 카테고리 표시 정보가 포함된 내 모임 목록을 반환한다
        return ResultData.success(result);
    }

    /**
     * {@inheritDoc}
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param keyword 모임명과 소개 검색어
     * @return 공개 모임 목록 조회 결과
     */
    @Override
    public ResultData getFindClubList(Long userNumb, String keyword) {
        // 모임 찾기는 관심분야를 하나 이상 선택한 사용자만 이용한다
        if (StringUtil.isEmpty(userNumb) || readingClubMapper.getUserInterestCnt(userNumb) == 0) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 검색어 양끝 공백과 길이를 보정한다
        String normalizedKeyword = StringUtil.normalizePlainText(keyword, 100);
        // 관심분야 우선순위와 검색어를 적용한 공개 모임을 조회한다
        List<ReadingClubDto.ClubViewDto> clubs = readingClubMapper.getFindClubList(userNumb, normalizedKeyword);
        // 조회한 각 공개 모임에 카테고리 표시 정보를 결합한다
        List<ReadingClubDto.ClubViewDto> result = fillClubRelations(clubs, false);
        // 카테고리 표시 정보가 포함된 공개 모임 목록을 반환한다
        return ResultData.success(result);
    }

    /**
     * {@inheritDoc}
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param clubNumb 모임 번호
     * @return 모임 상세 조회 결과
     */
    @Override
    public ResultData getClubDtl(Long userNumb, Long clubNumb) {
        // 상세 조회에 필요한 두 식별값을 검증한다
        if (StringUtil.hasEmpty(userNumb, clubNumb)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 로그인 사용자 관점의 모임 상세를 조회한다
        ReadingClubDto.ClubViewDto club = readingClubMapper.getClubDtl(clubNumb, userNumb);
        // 존재하지 않는 모임은 데이터 없음으로 반환한다
        if (StringUtil.isEmpty(club)) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 비공개 모임은 활성 회원 또는 유효 초대 대상만 상세를 볼 수 있다
        if (CLUB_PRIVATE.equals(club.getClubVisb()) && StringUtil.isEmpty(club.getMembStat())) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 카테고리와 승인형 질문을 결합한다
        fillClubRelation(club, true);
        // 완성된 상세 정보를 반환한다
        return ResultData.success(club);
    }

    /**
     * {@inheritDoc}
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회를 요청한 사용자 번호
     * @param clubNumb 조회할 모임 번호
     * @return 모임원 프로필 목록 조회 결과
     */
    @Override
    public ResultData getClubMemberList(Long userNumb, Long clubNumb) {

        // 모임원 관계 조회에 필요한 식별값을 검증한다
        if (StringUtil.hasEmpty(userNumb, clubNumb)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 요청 사용자의 현재 모임원 관계를 조회한다
        ReadingClubDto.MemberDto member = readingClubMapper.getClubMember(clubNumb, userNumb);
        // 활성 모임원만 다른 모임원의 프로필을 조회할 수 있다
        if (StringUtil.isEmpty(member) || !MEMBER_ACTIVE.equals(member.getMembStat())) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 활성 계정인 활성 모임원과 프로필 이미지 경로를 조회한다
        List<ReadingClubDto.MemberProfileDto> members = readingClubMapper.getClubMemberList(clubNumb);
        // 접근 가능한 모임원 프로필 목록을 반환한다
        return ResultData.success(members);
    }

    /**
     * {@inheritDoc}
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param request 모임 생성 입력값
     * @return 생성된 모임 상세 조회 결과
     */
    @Override
    @Transactional
    public ResultData setClub(Long userNumb, ReadingClubDto.ClubCreateReqDto request) {
        // 인증 사용자와 생성 요청을 검증한다
        if (StringUtil.hasEmpty(userNumb, request) || !isValidClubRequest(request)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 사용자 입력 문구의 비속어를 검사한다
        if (hasBadWord(request)) {
            // "욕설이나 비속어는 사용할 수 없어요. 감지된 단어: 모임 정보"
            return ResultData.fail(ResultEnum.COMMON_BAD_WORD_INCLUDED, "모임 정보");
        }

        // 모임 마스터를 생성한다
        readingClubMapper.setClub(userNumb, request);
        // 선택 순서를 모임 내 정렬 순서로 저장한다
        for (int index = 0; index < request.getCategoryList().size(); index++) {
            // 카테고리 한 건을 저장한다
            readingClubMapper.setClubCategory(request.getClubNumb(), request.getCategoryList().get(index), index + 1);
        }

        // 개설자를 활성 모임장 회원으로 등록한다
        readingClubMapper.setOwnerMember(request.getClubNumb(), userNumb);
        // 승인형 모임은 생성 시점의 가입 질문을 함께 저장한다
        if (JOIN_APPROVAL.equals(request.getJoinType())) {
            // 모임당 질문 한 행을 저장한다
            readingClubMapper.setClubQuestion(userNumb, toQuestion(request.getClubNumb(), request.getQuestionList()));
        }

        // 생성된 모임 상세를 반환한다
        return getClubDtl(userNumb, request.getClubNumb());
    }

    /**
     * {@inheritDoc}
     *
     * @author Hanwon.Jang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 수정할 모임 번호
     * @param request 수정할 모임 정보
     * @return 수정된 모임 상세 조회 결과
     */
    @Override
    @Transactional
    public ResultData uptClub(Long userNumb, Long clubNumb, ReadingClubDto.ClubCreateReqDto request) {
        // 모임 수정에 필요한 사용자와 대상 및 요청 본문을 먼저 검증한다
        if (StringUtil.hasEmpty(userNumb, clubNumb, request)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 권한과 운영 제약을 같은 트랜잭션에서 판단하도록 모임 마스터 행을 잠근다
        ReadingClubDto.ClubViewDto club = readingClubMapper.getClubForUpdate(clubNumb);
        // 현재 운영 중인 모임의 모임장만 모임 정보를 수정할 수 있다
        if (!isOwner(club, userNumb) || !CLUB_ACTIVE.equals(club.getClubStat())) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 수정 입력을 정규화하고 허용된 공개 범위와 가입 방식 조합인지 검증한다
        if (!isValidClubRequest(request)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 모임명과 소개 및 승인 질문의 비속어를 저장 전에 검사한다
        if (hasBadWord(request)) {
            // "욕설이나 비속어는 사용할 수 없어요. 감지된 단어: 모임 정보"
            return ResultData.fail(ResultEnum.COMMON_BAD_WORD_INCLUDED, "모임 정보");
        }

        // 현재 활성 회원과 유효한 예약 초대보다 작은 정원으로 줄일 수 없다
        if (readingClubMapper.getOccupiedSeatCnt(clubNumb) > request.getMaxxMemb()) {
            // "수정에 실패했어요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        // 예정 또는 진행 중 회차가 있으면 기존 콘텐츠 공개 범위가 달라지지 않게 한다
        if (!club.getClubVisb().equals(request.getClubVisb())
                && readingClubMapper.getOngoingRoundCnt(clubNumb) > 0) {
            // "수정에 실패했어요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        // 처리 대기 신청이 있으면 신청 당시 가입 정책이 달라지지 않게 한다
        if (!club.getJoinType().equals(request.getJoinType())
                && readingClubMapper.getPendingApplicationCnt(clubNumb) > 0) {
            // "수정에 실패했어요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        // 권한 조건을 SQL에도 적용해 검증 이후의 소유권 또는 운영 상태 변경을 방어한다
        if (readingClubMapper.uptClub(userNumb, clubNumb, request) == 0) {
            // "수정에 실패했어요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        // 활성 계정의 수정 권한이 확정된 뒤 만료된 초대 예약석을 정리한다
        readingClubMapper.delExpiredInvitation(clubNumb);

        // 기존 카테고리를 지운 뒤 요청 순서대로 유효한 관계를 다시 구성한다
        readingClubMapper.delClubCategory(clubNumb);
        // 선택한 모든 카테고리를 순서값과 함께 저장한다
        for (int index = 0; index < request.getCategoryList().size(); index++) {
            // 카테고리 한 건을 새 노출 순서로 저장한다
            readingClubMapper.setClubCategory(clubNumb, request.getCategoryList().get(index), index + 1);
        }

        // 승인형 가입 방식은 이후 신청에 사용할 현재 질문을 등록하거나 수정한다
        if (JOIN_APPROVAL.equals(request.getJoinType())) {
            // 요청 질문 목록을 고정 컬럼 DTO로 변환한다
            ReadingClubDto.QuestionDto question = toQuestion(clubNumb, request.getQuestionList());
            // 기존 질문 행이 없으면 수정 대신 신규 질문 행을 등록한다
            if (readingClubMapper.uptClubQuestion(userNumb, question) == 0) {
                // 승인형으로 새로 전환된 모임의 질문 행을 등록한다
                readingClubMapper.setClubQuestion(userNumb, question);
            }
        }

        // 수정된 카테고리와 질문을 포함한 모임 상세를 반환한다
        return getClubDtl(userNumb, clubNumb);
    }

    /**
     * {@inheritDoc}
     *
     * @author Hanwon.Jang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 삭제할 모임 번호
     * @return 모임 물리 삭제 결과
     */
    @Override
    @Transactional
    public ResultData delClub(Long userNumb, Long clubNumb) {
        // 모임 삭제에 필요한 사용자 번호와 대상 모임 번호를 검증한다
        if (StringUtil.hasEmpty(userNumb, clubNumb)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 삭제 권한과 운영 상태를 한 트랜잭션에서 확정하도록 모임 마스터 행을 잠근다
        ReadingClubDto.ClubViewDto club = readingClubMapper.getClubForUpdate(clubNumb);
        // 현재 운영 중인 모임의 모임장만 복구 불가능한 삭제를 실행할 수 있다
        if (!isOwner(club, userNumb) || !CLUB_ACTIVE.equals(club.getClubStat())) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 소유권과 운영 상태를 SQL에서도 다시 확인하며 모임 종속 데이터는 외래키로 함께 삭제한다
        if (readingClubMapper.delClub(userNumb, clubNumb) == 0) {
            // "삭제에 실패했어요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_DELETE_REJECTED);
        }

        // 개인 독후감과 공용 도서를 제외한 모임 및 종속 데이터 삭제 성공을 반환한다
        return ResultData.success();
    }

    /**
     * {@inheritDoc}
     *
     * @author SeungHyeon.Kang
     * @param userNumb 가입 사용자 번호
     * @param clubNumb 모임 번호
     * @param request 가입 질문 답변
     * @return 가입 또는 신청 처리 결과
     */
    @Override
    @Transactional
    public ResultData setJoin(Long userNumb, Long clubNumb, ReadingClubDto.JoinReqDto request) {
        // 가입 대상과 사용자 식별값을 검증한다
        if (StringUtil.hasEmpty(userNumb, clubNumb, request)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 정원과 중복 관계를 같은 트랜잭션에서 판단하도록 모임 행을 잠근다
        ReadingClubDto.ClubViewDto club = readingClubMapper.getClubForUpdate(clubNumb);
        // 공개 운영 중인 모임만 직접 가입할 수 있다
        if (StringUtil.isEmpty(club) || !CLUB_ACTIVE.equals(club.getClubStat())
                || !CLUB_PUBLIC.equals(club.getClubVisb())) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 이미 회원 또는 초대 관계가 있으면 중복 가입을 막는다
        if (!StringUtil.isEmpty(readingClubMapper.getClubMember(clubNumb, userNumb))) {
            // "저장에 실패했어요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
        }

        // 즉시 가입은 현재 좌석을 검사하고 활성 회원을 바로 생성한다
        if (JOIN_OPEN.equals(club.getJoinType())) {
            // 만료 초대를 지워 좌석 집계를 최신화한다
            readingClubMapper.delExpiredInvitation(clubNumb);
            // 정원이 가득 찬 모임에는 새 회원을 추가하지 않는다
            if (readingClubMapper.getOccupiedSeatCnt(clubNumb) >= club.getMaxxMemb()) {
                // "저장에 실패했어요. 다시 시도해주세요."
                return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
            }

            // 활성 일반 회원을 등록한다
            readingClubMapper.setActiveMember(clubNumb, userNumb);
            // 가입 완료 후 상세를 반환한다
            return getClubDtl(userNumb, clubNumb);
        }

        // 승인 가입 이외 방식은 공개 페이지 직접 가입을 허용하지 않는다
        if (!JOIN_APPROVAL.equals(club.getJoinType())) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 동일 모임의 처리 중 신청은 한 건만 허용한다
        if (!StringUtil.isEmpty(readingClubMapper.getPendingApplication(clubNumb, userNumb))) {
            // "저장에 실패했어요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
        }

        // 현재 질문과 답변 개수 및 내용을 검증한다
        ReadingClubDto.QuestionDto question = readingClubMapper.getClubQuestion(clubNumb);
        List<String> questions = toQuestionList(question);
        List<String> answers = normalizeTextList(request.getAnswerList(), 2000);
        // 모든 질문에 순서대로 장문 답변을 입력해야 한다
        if (questions.isEmpty() || questions.size() != answers.size() || hasEmptyText(answers)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 답변 내 비속어 포함 여부를 검사한다
        for (String answer : answers) {
            // 한 답변이라도 비속어가 있으면 전체 신청을 거절한다
            if (badWordDetectionService.findBadWord(answer).isPresent()) {
                // "욕설이나 비속어는 사용할 수 없어요. 감지된 단어: 가입 답변"
                return ResultData.fail(ResultEnum.COMMON_BAD_WORD_INCLUDED, "가입 답변");
            }
        }

        // 신청 당시 질문과 답변을 한 행에 복사한다
        readingClubMapper.setJoinApplication(toApplication(clubNumb, userNumb, questions, answers));
        // 신청 완료 상세를 반환한다
        return getClubDtl(userNumb, clubNumb);
    }

    /**
     * {@inheritDoc}
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @return 맞팔로우 초대 후보 목록 조회 결과
     */
    @Override
    public ResultData getInviteCandidateList(Long userNumb, Long clubNumb) {
        // 모임장 권한을 검증한다
        ReadingClubDto.ClubViewDto club = readingClubMapper.getClubDtl(clubNumb, userNumb);
        // 현재 모임장만 맞팔 초대 후보를 볼 수 있다
        if (!isOwner(club, userNumb)) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 아직 관계가 없는 활성 맞팔로우 사용자를 조회한다
        List<ReadingClubDto.InviteCandidateDto> candidates = readingClubMapper.getInviteCandidateList(clubNumb
                                                                                                      , userNumb);
        // 모임장이 선택할 수 있는 맞팔로우 초대 후보 목록을 반환한다
        return ResultData.success(candidates);
    }

    /**
     * {@inheritDoc}
     *
     * @author Hanwon.Jang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @return 보낸 초대 목록 조회 결과
     */
    @Override
    public ResultData getSentInvitationList(Long userNumb, Long clubNumb) {
        // 현재 모임장만 활성 회원에게 발송한 초대 목록을 조회할 수 있다
        if (!isOwner(readingClubMapper.getClubDtl(clubNumb, userNumb), userNumb)) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 비활성화 또는 삭제 대기 회원을 제외한 유효한 보낸 초대 목록을 반환한다
        return ResultData.success(readingClubMapper.getSentInvitationList(clubNumb, userNumb));
    }

    /**
     * {@inheritDoc}
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @param request 초대 대상 목록
     * @return 초대 저장 결과
     */
    @Override
    @Transactional
    public ResultData setInvitation(Long userNumb, Long clubNumb, ReadingClubDto.InviteReqDto request) {
        // 인증 사용자와 모임 및 요청 본문이 있어야 초대 대상을 확인할 수 있다
        if (StringUtil.hasEmpty(userNumb, clubNumb, request)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 요청 본문에서 초대 대상 사용자 번호 목록을 가져온다
        List<Long> targetUserNumbList = request.getUserNumbList();
        // 초대 대상 목록이 없으면 중복 검사와 좌석 계산을 진행하지 않는다
        if (StringUtil.isEmpty(targetUserNumbList)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 초대 대상 사용자 번호의 중복을 검사할 집합을 생성한다
        Set<Long> targetUserNumbSet = new HashSet<>(targetUserNumbList);
        // 같은 사용자가 중복 선택되면 전체 초대를 저장하지 않는다
        if (targetUserNumbSet.size() != targetUserNumbList.size()) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 좌석 예약을 직렬화하도록 모임 행을 잠근다
        ReadingClubDto.ClubViewDto club = readingClubMapper.getClubForUpdate(clubNumb);
        // 현재 운영 중인 모임의 모임장만 초대할 수 있다
        if (!isOwner(club, userNumb) || !CLUB_ACTIVE.equals(club.getClubStat())) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 만료된 예약석을 먼저 제거한다
        readingClubMapper.delExpiredInvitation(clubNumb);
        // 선택한 전체 대상의 좌석을 한 번에 확보할 수 있어야 한다
        if (readingClubMapper.getOccupiedSeatCnt(clubNumb) + targetUserNumbList.size() > club.getMaxxMemb()) {
            // "저장에 실패했어요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
        }

        // 모든 대상의 맞팔·기존 관계를 먼저 검증한다
        for (Long targetUserNumb : targetUserNumbList) {
            // 맞팔이 아니거나 이미 모임 관계가 있으면 일괄 초대를 중단한다
            if (StringUtil.isEmpty(targetUserNumb)
                    || readingClubMapper.getMutualFollowCnt(userNumb, targetUserNumb) == 0
                    || !StringUtil.isEmpty(readingClubMapper.getClubMember(clubNumb, targetUserNumb))) {
                // "저장에 실패했어요. 다시 시도해주세요."
                return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
            }
        }

        // 검증된 대상마다 3일 유효한 예약석을 생성한다
        for (Long targetUserNumb : targetUserNumbList) {
            // 초대 대상 한 명의 예약석을 등록한다
            readingClubMapper.setInvitation(clubNumb, targetUserNumb, userNumb);
            // 초대받은 활성 회원의 알림센터에 모임장과 모임명이 포함된 초대 알림을 저장하고 푸시를 예약한다
            alimService.sendAlim(
                    targetUserNumb
                  , Constant.ALIM_SITU_CLUB
                  , Constant.ALIM_TEMP_CODE_INVITE_CLUB
                  , clubNumb
                  , Map.of("userName", club.getOwnrNick(), "clubName", club.getClubName())
            );
        }

        // 초대 저장 성공 응답을 반환한다
        return ResultData.success();
    }

    /**
     * {@inheritDoc}
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 받은 초대 목록 조회 결과
     */
    @Override
    @Transactional
    public ResultData getInvitationList(Long userNumb) {
        // 인증 사용자를 검증한다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요. 다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 사용자에게 도착한 만료 초대를 먼저 삭제한다
        readingClubMapper.delUserExpiredInvitation(userNumb);
        // 만료되지 않은 받은 모임 초대를 조회한다
        List<ReadingClubDto.InvitationDto> invitations = readingClubMapper.getInvitationList(userNumb);
        // 로그인 사용자의 유효한 받은 초대 목록을 반환한다
        return ResultData.success(invitations);
    }

    /**
     * {@inheritDoc}
     *
     * @author SeungHyeon.Kang
     * @param userNumb 초대 대상 사용자 번호
     * @param clubNumb 모임 번호
     * @return 초대 수락 처리 결과
     */
    @Override
    @Transactional
    public ResultData uptInvitationAccepted(Long userNumb, Long clubNumb) {
        // 대상 모임 행을 잠가 삭제와 경합하지 않게 한다
        ReadingClubDto.ClubViewDto club = readingClubMapper.getClubForUpdate(clubNumb);
        // 유효한 운영 모임의 예약석만 활성 회원으로 전환한다
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(club)
                || readingClubMapper.uptInvitationAccepted(clubNumb, userNumb) == 0) {
            // "수정에 실패했어요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        // 초대 수락 후 모임 상세를 반환한다
        return getClubDtl(userNumb, clubNumb);
    }

    /**
     * {@inheritDoc}
     *
     * @author SeungHyeon.Kang
     * @param userNumb 초대 대상 사용자 번호
     * @param clubNumb 모임 번호
     * @return 초대 거절 처리 결과
     */
    @Override
    @Transactional
    public ResultData delInvitation(Long userNumb, Long clubNumb) {
        // 본인에게 도착한 초대 예약석만 삭제한다
        if (StringUtil.hasEmpty(userNumb, clubNumb)
                || readingClubMapper.delInvitation(clubNumb, userNumb) == 0) {
            // "삭제에 실패했어요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_DELETE_REJECTED);
        }

        // 이력을 남기지 않은 초대 거절 성공 응답을 반환한다
        return ResultData.success();
    }

    /**
     * {@inheritDoc}
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @param targetUserNumb 초대 대상 사용자 번호
     * @return 초대 취소 처리 결과
     */
    @Override
    @Transactional
    public ResultData delOwnerInvitation(Long userNumb, Long clubNumb, Long targetUserNumb) {
        // 현재 모임장만 특정 대상의 예약석을 취소할 수 있다
        if (!isOwner(readingClubMapper.getClubForUpdate(clubNumb), userNumb)
                || readingClubMapper.delOwnerInvitation(clubNumb, targetUserNumb, userNumb) == 0) {
            // "삭제에 실패했어요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_DELETE_REJECTED);
        }

        // 초대 취소 성공 응답을 반환한다
        return ResultData.success();
    }

    /**
     * {@inheritDoc}
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @return 처리 중 가입 신청 목록 조회 결과
     */
    @Override
    public ResultData getApplicationList(Long userNumb, Long clubNumb) {
        // 현재 모임장만 신청 답변을 조회할 수 있다
        if (!isOwner(readingClubMapper.getClubDtl(clubNumb, userNumb), userNumb)) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 질문과 답변 컬럼을 화면용 목록으로 변환한다
        List<ReadingClubDto.ApplicationDto> applications = readingClubMapper.getApplicationList(clubNumb);
        // 각 신청의 질문과 답변을 목록에 채운다
        for (ReadingClubDto.ApplicationDto application : applications) {
            // 질문 목록을 설정한다
            application.setQuestionList(toQuestionList(application));
            // 답변 목록을 설정한다
            application.setAnswerList(toAnswerList(application));
        }

        // 심사 대기 신청 목록을 반환한다
        return ResultData.success(applications);
    }

    /**
     * {@inheritDoc}
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @param applNumb 모임별 신청 번호
     * @param request 승인 또는 거절 상태
     * @return 가입 신청 처리 결과
     */
    @Override
    @Transactional
    public ResultData uptApplication(Long userNumb, Long clubNumb, Long applNumb
                                    , ReadingClubDto.ApplicationDecisionReqDto request) {
        // 승인 또는 거절 값만 허용한다
        if (StringUtil.hasEmpty(userNumb, clubNumb, applNumb, request)
                || !(APPLICATION_APPROVED.equals(request.getJoinStat())
                || APPLICATION_REJECTED.equals(request.getJoinStat()))) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 정원과 신청 상태를 같은 트랜잭션에서 처리하도록 모임을 잠근다
        ReadingClubDto.ClubViewDto club = readingClubMapper.getClubForUpdate(clubNumb);
        // 현재 모임장만 신청을 처리할 수 있다
        if (!isOwner(club, userNumb)) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 처리 중 신청을 잠가 중복 처리를 막는다
        ReadingClubDto.ApplicationDto application = readingClubMapper.getApplicationForUpdate(clubNumb, applNumb);
        // 이미 처리됐거나 삭제된 신청은 갱신하지 않는다
        if (StringUtil.isEmpty(application)) {
            // "수정에 실패했어요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        // 승인 시점에만 실제 좌석을 경쟁한다
        if (APPLICATION_APPROVED.equals(request.getJoinStat())) {
            // 만료 초대를 제거하고 현재 좌석을 다시 계산한다
            readingClubMapper.delExpiredInvitation(clubNumb);
            // 활성 회원과 예약석이 정원에 도달했으면 신청을 대기 상태로 유지한다
            if (readingClubMapper.getOccupiedSeatCnt(clubNumb) >= club.getMaxxMemb()) {
                // "수정에 실패했어요. 다시 시도해주세요."
                return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
            }

            // 이미 별도 경로로 가입한 신청자는 중복 등록하지 않는다
            if (!StringUtil.isEmpty(readingClubMapper.getClubMember(clubNumb, application.getUserNumb()))) {
                // "수정에 실패했어요. 다시 시도해주세요."
                return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
            }

            // 승인 대상자를 활성 일반 회원으로 등록한다
            readingClubMapper.setActiveMember(clubNumb, application.getUserNumb());
        }

        // 처리 상태와 처리자를 기록하면서 답변 본문을 즉시 삭제한다
        if (readingClubMapper.uptJoinApplication(
                clubNumb, applNumb, userNumb, request.getJoinStat()) == 0) {
            // "수정에 실패했어요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        // 승인 또는 거절 성공 응답을 반환한다
        return ResultData.success();
    }

    private boolean isValidClubRequest(ReadingClubDto.ClubCreateReqDto request) {
        // 사용자 입력 문구를 저장 전 정규화한다
        request.setClubName(StringUtil.normalizePlainText(request.getClubName(), 100));
        // 모임 소개를 저장 허용 길이로 정규화한다
        request.setClubCntn(StringUtil.normalizePlainText(request.getClubCntn(), 2000));
        // 카테고리와 질문 목록을 정규화한다
        List<String> categories = normalizeTextList(request.getCategoryList(), 20);
        // 정규화한 카테고리의 중복을 검사할 집합을 생성한다
        Set<String> categorySet = new HashSet<>(categories);
        // 정규화한 카테고리 목록을 요청에 반영한다
        request.setCategoryList(categories);
        // 질문 목록을 정규화해 요청에 반영한다
        request.setQuestionList(normalizeTextList(request.getQuestionList(), 500));

        // 공통 필수값과 허용 범위를 검사한다
        if (StringUtil.hasEmpty(request.getClubName(), request.getClubCntn(), request.getClubVisb()
                , request.getJoinType(), request.getMaxxMemb()) || request.getMaxxMemb() < 2
                || request.getMaxxMemb() > 100 || categories.isEmpty() || categories.size() > 3
                || categorySet.size() != categories.size()
                || readingClubMapper.getValidCategoryCnt(categories) != categories.size()) {
            // 생성 조건 미충족을 반환한다
            return false;
        }

        // 공개 범위와 가입 방식 조합을 검사한다
        boolean validVisibility = CLUB_PUBLIC.equals(request.getClubVisb())
                || CLUB_PRIVATE.equals(request.getClubVisb());
        // 비공개는 초대형만, 공개는 세 방식 모두 허용한다
        boolean validJoinType = CLUB_PRIVATE.equals(request.getClubVisb())
                ? JOIN_INVITE.equals(request.getJoinType())
                : JOIN_OPEN.equals(request.getJoinType()) || JOIN_APPROVAL.equals(request.getJoinType())
                  || JOIN_INVITE.equals(request.getJoinType());
        // 승인형은 질문 1~5개가 필수이고 다른 방식은 질문을 저장하지 않는다
        boolean validQuestions = JOIN_APPROVAL.equals(request.getJoinType())
                ? !request.getQuestionList().isEmpty() && request.getQuestionList().size() <= 5
                  && !hasEmptyText(request.getQuestionList())
                : request.getQuestionList().isEmpty();
        // 조합 검증 결과를 반환한다
        return validVisibility && validJoinType && validQuestions;
    }

    private boolean hasBadWord(ReadingClubDto.ClubCreateReqDto request) {
        // 모임명과 소개에서 비속어를 검사한다
        if (badWordDetectionService.findBadWord(request.getClubName()).isPresent()
                || badWordDetectionService.findBadWord(request.getClubCntn()).isPresent()) {
            // 비속어가 있음을 반환한다
            return true;
        }

        // 가입 질문을 순차 검사한다
        for (String question : request.getQuestionList()) {
            // 비속어가 포함된 질문이 있으면 검사를 종료한다
            if (badWordDetectionService.findBadWord(question).isPresent()) {
                // 비속어가 있음을 반환한다
                return true;
            }
        }

        // 모든 입력이 검사 조건을 통과했음을 반환한다
        return false;
    }

    private List<ReadingClubDto.ClubViewDto> fillClubRelations(List<ReadingClubDto.ClubViewDto> clubs
                                                              , boolean includeQuestions) {
        // 각 모임에 별도 관계 데이터를 결합한다
        for (ReadingClubDto.ClubViewDto club : clubs) {
            // 카테고리와 선택적 질문을 결합한다
            fillClubRelation(club, includeQuestions);
        }

        // 관계 데이터가 채워진 원래 목록을 반환한다
        return clubs;
    }

    private void fillClubRelation(ReadingClubDto.ClubViewDto club, boolean includeQuestions) {
        // 모임 카테고리 목록을 노출 순서대로 설정한다
        club.setCategoryList(readingClubMapper.getClubCategoryList(club.getClubNumb()));
        // 상세 화면이고 승인형인 경우에만 현재 질문을 설정한다
        if (includeQuestions && JOIN_APPROVAL.equals(club.getJoinType())) {
            // 현재 질문 컬럼을 화면용 목록으로 변환한다
            club.setQuestionList(toQuestionList(readingClubMapper.getClubQuestion(club.getClubNumb())));
        }
    }

    private boolean isOwner(ReadingClubDto.ClubViewDto club, Long userNumb) {
        // 모임 정보와 사용자 번호가 모두 있고 현재 모임장 번호가 같은지 반환한다
        return !StringUtil.isEmpty(club) && !StringUtil.isEmpty(userNumb)
                && userNumb.equals(club.getOwnrNumb());
    }

    private ReadingClubDto.QuestionDto toQuestion(Long clubNumb, List<String> questions) {
        // 가입 질문을 고정 컬럼에 담을 DTO를 생성한다
        ReadingClubDto.QuestionDto result = new ReadingClubDto.QuestionDto();
        // 질문이 귀속될 모임 번호를 설정한다
        result.setClubNumb(clubNumb);
        // 최대 다섯 개 질문을 고정 컬럼에 순서대로 설정한다
        setQuestionFields(result, questions);
        // 저장용 질문 DTO를 반환한다
        return result;
    }

    private ReadingClubDto.ApplicationDto toApplication(Long clubNumb, Long userNumb
                                                       , List<String> questions, List<String> answers) {
        // 가입 질문 사본과 답변을 담을 신청 DTO를 생성한다
        ReadingClubDto.ApplicationDto result = new ReadingClubDto.ApplicationDto();
        // 신청 대상 모임 번호를 설정한다
        result.setClubNumb(clubNumb);
        // 신청 사용자 번호를 설정한다
        result.setUserNumb(userNumb);
        // 질문 사본을 고정 컬럼에 설정한다
        setApplicationQuestions(result, questions);
        // 답변을 고정 컬럼에 설정한다
        setApplicationAnswers(result, answers);
        // 저장용 신청 DTO를 반환한다
        return result;
    }

    private void setQuestionFields(ReadingClubDto.QuestionDto target, List<String> values) {
        // 첫 번째 질문은 승인형 모임에 필수로 설정한다
        target.setQuesFirs(getListValue(values, 0));
        // 선택적인 두 번째 질문을 설정한다
        target.setQuesSeco(getListValue(values, 1));
        // 선택적인 세 번째 질문을 설정한다
        target.setQuesThir(getListValue(values, 2));
        // 선택적인 네 번째 질문을 설정한다
        target.setQuesFour(getListValue(values, 3));
        // 선택적인 다섯 번째 질문을 설정한다
        target.setQuesFift(getListValue(values, 4));
    }

    private void setApplicationQuestions(ReadingClubDto.ApplicationDto target, List<String> values) {
        // 질문 사본을 순서별 컬럼에 설정한다
        target.setQuesFirs(getListValue(values, 0));
        // 두 번째 질문 사본을 설정한다
        target.setQuesSeco(getListValue(values, 1));
        // 세 번째 질문 사본을 설정한다
        target.setQuesThir(getListValue(values, 2));
        // 네 번째 질문 사본을 설정한다
        target.setQuesFour(getListValue(values, 3));
        // 다섯 번째 질문 사본을 설정한다
        target.setQuesFift(getListValue(values, 4));
    }

    private void setApplicationAnswers(ReadingClubDto.ApplicationDto target, List<String> values) {
        // 답변을 질문과 같은 순서의 컬럼에 설정한다
        target.setAnsrFirs(getListValue(values, 0));
        // 두 번째 가입 답변을 설정한다
        target.setAnsrSeco(getListValue(values, 1));
        // 세 번째 가입 답변을 설정한다
        target.setAnsrThir(getListValue(values, 2));
        // 네 번째 가입 답변을 설정한다
        target.setAnsrFour(getListValue(values, 3));
        // 다섯 번째 가입 답변을 설정한다
        target.setAnsrFift(getListValue(values, 4));
    }

    private List<String> toQuestionList(ReadingClubDto.QuestionDto question) {
        // 질문 행이 없으면 빈 목록을 반환한다
        if (StringUtil.isEmpty(question)) {
            // 질문 없음 목록을 반환한다
            return List.of();
        }

        // 질문 컬럼을 Null 제외 목록으로 반환한다
        return compactList(question.getQuesFirs(), question.getQuesSeco(), question.getQuesThir()
                         , question.getQuesFour(), question.getQuesFift());
    }

    private List<String> toQuestionList(ReadingClubDto.ApplicationDto application) {
        // 신청 당시 질문 컬럼을 Null 제외 목록으로 반환한다
        return compactList(application.getQuesFirs(), application.getQuesSeco(), application.getQuesThir()
                         , application.getQuesFour(), application.getQuesFift());
    }

    private List<String> toAnswerList(ReadingClubDto.ApplicationDto application) {
        // 처리 전 답변 컬럼을 Null 제외 목록으로 반환한다
        return compactList(application.getAnsrFirs(), application.getAnsrSeco(), application.getAnsrThir()
                         , application.getAnsrFour(), application.getAnsrFift());
    }

    private List<String> compactList(String... values) {
        // Null이 아닌 고정 컬럼 값을 담을 목록을 생성한다
        List<String> result = new ArrayList<>();
        // 고정 컬럼 값을 순서대로 순회한다
        for (String value : values) {
            // 값이 있는 컬럼만 화면용 목록에 추가한다
            if (!StringUtil.isEmpty(value)) {
                // 원래 순서를 유지해 목록에 추가한다
                result.add(value);
            }
        }

        // Null이 제거된 순서 목록을 반환한다
        return result;
    }

    /**
     * 모임 독서 등록 요청의 도서 필드와 목표 기간을 정규화하고 검증한다.
     *
     * @author Hanwon.Jang
     * @param request 검증할 모임 독서 등록 요청
     * @return 저장 가능한 요청이면 true
     */
    private boolean isValidReadingRequest(ReadingClubDto.ReadingCreateReqDto request) {

        // 외부 도서 검색 결과도 서버 저장 규격에 맞춰 길이와 공백을 정규화한다
        request.setBookTitl(StringUtil.normalizePlainText(request.getBookTitl(), 500));
        request.setBookAthr(StringUtil.normalizePlainText(request.getBookAthr(), 500));
        request.setBookPubl(StringUtil.normalizePlainText(request.getBookPubl(), 500));
        request.setBookIsbn(StringUtil.normalizePlainText(request.getBookIsbn(), 100));
        request.setBookCvim(StringUtil.normalizePlainText(request.getBookCvim(), 1000));
        request.setBookDesc(StringUtil.normalizePlainText(request.getBookDesc(), 4000));
        request.setPublDate(StringUtil.normalizePlainText(request.getPublDate(), 10));
        request.setGoalStdt(StringUtil.normalizePlainText(request.getGoalStdt(), 10));
        request.setGoalEndt(StringUtil.normalizePlainText(request.getGoalEndt(), 10));
        request.setIdemKeyx(StringUtil.normalizePlainText(request.getIdemKeyx(), 64));

        // 독후감 등록과 동일한 도서 필수값과 회차 키 및 기간을 요구한다
        if (StringUtil.hasEmpty(request.getBookTitl(), request.getBookAthr(), request.getBookPubl()
                              , request.getBookIsbn(), request.getBookCvim(), request.getBookDesc()
                              , request.getGoalStdt(), request.getGoalEndt(), request.getIdemKeyx())) {
            // 필수값 누락을 검증 실패로 반환한다
            return false;
        }

        try {
            // ISO 날짜만 허용하고 시작일이 종료일보다 늦지 않은지 확인한다
            LocalDate startDate = LocalDate.parse(request.getGoalStdt());
            LocalDate endDate = LocalDate.parse(request.getGoalEndt());
            // 날짜 범위 검증 결과를 반환한다
            return !startDate.isAfter(endDate);
        } catch (DateTimeParseException exception) {
            // 형식이 맞지 않는 날짜는 저장 요청에서 제외한다
            return false;
        }
    }

    /**
     * 모임 독서 회차의 도서와 목표 기간으로 멤버별 읽는 중 독후감을 구성한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 자동 생성 대상 사용자 번호
     * @param request 모임 독서 회차 정보
     * @param reportColor 기본 책갈피 색상 코드
     * @return 자동 생성할 읽는 중 독후감
     */
    private ReportDto toReadingReport(Long userNumb, ReadingClubDto.ReadingCreateReqDto request
                                     , String reportColor) {

        // 자동 생성 독후감 DTO를 구성한다
        ReportDto report = new ReportDto();
        report.setUserNumb(userNumb);
        report.setBookNumb(request.getBookNumb());
        report.setReptStat(Constant.REPORT_STAT_READ);
        report.setReptStdt(request.getGoalStdt());
        report.setReptEndt(request.getGoalEndt());
        report.setReptGrde("0");
        report.setReptColr(reportColor);
        report.setPubcYsno(Constant.COMM_NO);
        report.setReptCntn("");
        // 동일한 목표 기간의 읽는 중 독후감을 반환한다
        return report;
    }

    private List<String> normalizeTextList(List<String> values, int maxLength) {
        // Null 목록은 빈 목록으로 정규화한다
        if (StringUtil.isEmpty(values)) {
            // 공통 빈 목록을 반환한다
            return List.of();
        }

        // 정규화된 문구를 담을 새 목록을 생성한다
        List<String> result = new ArrayList<>();
        // 각 문구를 같은 최대 길이로 정규화한다
        for (String value : values) {
            // 정규화한 문구를 순서대로 추가한다
            result.add(StringUtil.normalizePlainText(value, maxLength));
        }

        // 정규화된 새 목록을 반환한다
        return result;
    }

    private boolean hasEmptyText(List<String> values) {
        // 모든 문구의 빈 값 여부를 검사한다
        for (String value : values) {
            // 하나라도 비어 있으면 유효하지 않다
            if (StringUtil.isEmpty(value)) {
                // 빈 문구가 있음을 반환한다
                return true;
            }
        }

        // 빈 문구가 없음을 반환한다
        return false;
    }

    private String getListValue(List<String> values, int index) {
        // 요청한 순서가 목록 안에 있을 때만 값을 반환한다
        if (!StringUtil.isEmpty(values) && index < values.size()) {
            // 해당 순서의 문구를 반환한다
            return values.get(index);
        }

        // 선택 항목이 없는 고정 컬럼에는 Null을 반환한다
        return null;
    }
}
