package com.sionic.ai.controller

import com.sionic.ai.dto.ActivityReportResponse
import com.sionic.ai.security.UserPrincipal
import com.sionic.ai.service.ReportService
import com.sionic.ai.util.UnauthorizedException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody

@RestController
@RequestMapping("/reports")
class ReportController(
    private val reportService: ReportService,
) {
    @GetMapping("/activity")
    fun activityReport(
        @AuthenticationPrincipal principal: UserPrincipal?,
    ): ActivityReportResponse {
        val auth = principal ?: throw UnauthorizedException("Unauthorized")
        return reportService.activityReport(auth)
    }

    @GetMapping("/chats.csv")
    fun chatReportCsv(
        @AuthenticationPrincipal principal: UserPrincipal?,
    ): org.springframework.http.ResponseEntity<StreamingResponseBody> {
        val auth = principal ?: throw UnauthorizedException("Unauthorized")
        val data = reportService.dailyChatReportCsv(auth)
        val body = StreamingResponseBody { out ->
            out.write(data)
        }
        return org.springframework.http.ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=chat_report.csv")
            .contentType(MediaType.TEXT_PLAIN)
            .body(body)
    }
}
