package org.our.sadari.alim.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.our.sadari.alim.dto.AlimDto;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.global.common.result.ResultData;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 알림 목록 조회와 공통 알림 발송을 제공하는 API Controller이다.
 * 일반 화면에서는 내 알림 목록을 조회하고, 내부 테스트나 관리자성 호출이 필요한 경우 같은 sendAlim 공통 로직을 API로도 실행할 수 있다.
 *
 * @author Seunghyeon.Kang
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alim")
@Tag(name = "알림", description = "사용자 알림 목록 및 알림 발송 API")
public class AlimController {

    private final AlimService alimService;

    /**
     * 로그인 사용자의 알림 목록을 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @return 알림 목록
     */
    @GetMapping("/list")
    @Operation(summary = "내 알림 목록 조회", description = "로그인 사용자의 삭제되지 않은 알림 목록을 최신순으로 조회한다.")
    public ResultData getMyAlimList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb) {
        return alimService.getMyAlimList(loginUserNumb);
    }

    /**
     * 수신자, 알림 상황, 템플릿 코드, 대상 번호, 치환 Map으로 공통 알림 발송 메서드를 실행한다.
     * 실제 서비스 로직에서 사용하는 sendAlim과 동일한 경로를 타므로 알림 도메인 자체에서도 발송 동작을 재사용할 수 있다.
     *
     * @author Seunghyeon.Kang
     * @param request 알림 발송 요청
     * @return 발송 결과
     */
    @PostMapping("/send")
    @Operation(summary = "알림 발송", description = "알림 상황, 템플릿 코드, 치환 Map으로 사용자 알림을 발송한다.")
    public ResultData sendAlim(@RequestBody AlimDto.AlimSendDto request) {
        if (request == null) {
            request = new AlimDto.AlimSendDto();
        }

        // null body로 들어온 요청은 위에서 빈 DTO로 보정했으므로 서비스의 공통 검증 분기에서 실패 응답을 만들게 둔다.
        return alimService.sendAlim(
                request.getUserNumb(),
                request.getAlimSitu(),
                request.getTempCode(),
                request.getTagtNumb(),
                request.getReplaceMap()
        );
    }
}