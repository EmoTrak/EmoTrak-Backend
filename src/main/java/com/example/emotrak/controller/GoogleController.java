package com.example.emotrak.controller;

import com.example.emotrak.Service.GoogleService;
import com.example.emotrak.exception.ResponseMessage;
import com.example.emotrak.security.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@Tag(name = "Oauth2", description = "소셜로그인")
public class GoogleController {

    private final GoogleService googleService;

    @Operation(summary = "구글 로그인", description = "소셜로그인")
    @GetMapping("/google/callback")
    public ResponseEntity<?> googleLogin(@RequestParam String code,
                                         @RequestParam String scope,
                                         HttpServletResponse response) throws JsonProcessingException {
        googleService.googleLogin(code, scope, response);
        return ResponseMessage.successResponse(HttpStatus.OK, "구글 로그인 완료", null);
    }

    @Tag(name = "Oauth2")
    @Operation(summary = "구글 연동 해제", description = "구글 연동 해제")
    @GetMapping("/google/unlink")
    public ResponseEntity<?> googleUnlink(@ApiIgnore @AuthenticationPrincipal UserDetailsImpl userDetails,
                                          @RequestHeader("access_token") String accessToken) {
        googleService.unlinkGoogleAccount(userDetails.getUser(), accessToken);
        return ResponseMessage.successResponse(HttpStatus.OK, "구글 연동 해제 완료", null);
    }

}