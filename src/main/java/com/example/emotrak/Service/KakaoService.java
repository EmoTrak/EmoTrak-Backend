package com.example.emotrak.Service;

import com.example.emotrak.dto.OauthUserInfoDto;
import com.example.emotrak.dto.TokenDto;
import com.example.emotrak.entity.User;
import com.example.emotrak.entity.UserRoleEnum;
import com.example.emotrak.exception.CustomErrorCode;
import com.example.emotrak.exception.CustomException;
import com.example.emotrak.jwt.TokenProvider;
import com.example.emotrak.repository.UserRepository;
import com.example.emotrak.util.Validation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final Validation validation;
    private final UserService userService;

    public void kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getToken(code);

        // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        OauthUserInfoDto oauthUserInfo = getKakaoUserInfo(accessToken);

        // 3. 필요시에 회원가입
        User kakaoUser = registerKakaoUserIfNeeded(oauthUserInfo);

        // 4. JWT 토큰 반환
        TokenDto tokenDto = tokenProvider.generateTokenDto(kakaoUser, kakaoUser.getRole());
        log.info("JWT Access Token: {}", tokenDto.getAccessToken());
        validation.tokenToHeaders(tokenDto,response);
    }

    // 1. "인가 코드"로 "액세스 토큰" 요청
    private String getToken(String code) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "07f88dbc408f08bcd7e1bd0b2ca3c993");
        //body.add("redirect_uri", "http://clone-thunder-market.s3-website.ap-northeast-2.amazonaws.com/oauth");
        //body.add("redirect_uri", "http://localhost:3000/oauth");
        body.add("redirect_uri", "http://localhost:3000/oauth/kakao");
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        log.info("JSON Data: {}", jsonNode.toString());
        return jsonNode.get("access_token").asText();
    }

    // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
    private OauthUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
        String email = jsonNode.get("kakao_account")
                .get("email").asText();

        log.info("카카오 사용자 정보: " + id + ", " + email);
        return new OauthUserInfoDto(String.valueOf(id), email, nickname);
    }

    // 3. 필요시에 회원가입
    private User registerKakaoUserIfNeeded(OauthUserInfoDto oauthUserInfo) {
        // DB 에 중복된 Kakao Id 가 있는지 확인
        Long kakaoId = Long.parseLong(oauthUserInfo.getId()); // String을 Long으로 변환
        User kakaoUser = userRepository.findByKakaoId(kakaoId)
                .orElse(null);
        if (kakaoUser == null) {
            // 카카오 사용자 email 동일한 email 가진 회원이 있는지 확인
            String kakaoEmail = oauthUserInfo.getEmail();
            User sameEmailUser = userRepository.findByEmail(kakaoEmail).orElse(null);
            if (sameEmailUser != null) {
                kakaoUser = sameEmailUser;
                // 기존 회원정보에 카카오 Id 추가
                kakaoUser = kakaoUser.kakaoIdUpdate(kakaoId);
            } else {
                // 신규 회원가입
                // password: random UUID
                String password = UUID.randomUUID().toString();
                String encodedPassword = passwordEncoder.encode(password);

                // email: kakao email
                String email = oauthUserInfo.getEmail();

                String nickname = oauthUserInfo.getNickname();
                boolean hasNickname = userRepository.existsByNickname(nickname);
                if (hasNickname) {
                    nickname = oauthUserInfo.getNickname() + "_" + userRepository.getUniqueNameSuffix(nickname);
                }

                kakaoUser = new User(encodedPassword, email, nickname, kakaoId, null, null, UserRoleEnum.USER);
            }
            userRepository.save(kakaoUser);
        }
        return kakaoUser;
    }

    public void unlinkKakaoAccount(User user, String accessToken) {
        // 사용자가 없거나 카카오 ID가 없는 경우에 대한 예외 처리
        if (accessToken == null || user.getKakaoId() == null) {
            throw new CustomException(CustomErrorCode.NO_OAUTH_LINK);
        }
        // 연동해제를 위한 카카오 API 호출
        boolean isUnlinked = unlinkKakaoAccountApi(user, accessToken);
        if (!isUnlinked) {
            throw new CustomException(CustomErrorCode.OAUTH_UNLINK_FAILED);
        }
        log.info("user.getId() = {}", user.getId());
        log.info("user.getKakaoId() = {}", user.getKakaoId());
        log.info("user = {}", user);
//        userService.deleteUser(user);
        log.info("카카오 연동해제 완료");
    }

    private boolean unlinkKakaoAccountApi(User user, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(accessToken);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("target_id_type", "user_id");
        body.add("target_id", String.valueOf(user.getKakaoId()));

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v1/user/unlink",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        return response.getStatusCode() == HttpStatus.OK;
    }

}