package com.ak4n1.terra.api.terra_api.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/kick")
public class KickProxyController {

    private static final Logger logger = LoggerFactory.getLogger(KickProxyController.class);

    @Value("${kick.client-id}")
    private String clientId;

    @Value("${kick.client-secret}")
    private String clientSecret;

    @GetMapping("/channels/{slug}")
    public ResponseEntity<String> getChannel(@PathVariable String slug) {
        RestTemplate rest = new RestTemplate();

        // 1) Obtener App Access Token desde Kick
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String,String>> tokenReq = new HttpEntity<>(form, tokenHeaders);

        String tokenUrl = "https://id.kick.com/oauth/token";

        AccessTokenResponse tokenResp = rest.postForObject(tokenUrl, tokenReq, AccessTokenResponse.class);
        if (tokenResp == null || tokenResp.getAccessToken() == null) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Error al obtener token de Kick");
        }
        String accessToken = tokenResp.getAccessToken();

        // 2) Llamar a la API de canales con Bearer token
        HttpHeaders apiHeaders = new HttpHeaders();
        apiHeaders.setBearerAuth(accessToken);
        apiHeaders.set("User-Agent", "Mozilla/5.0");

        HttpEntity<Void> apiReq = new HttpEntity<>(apiHeaders);

        String apiUrl = "https://api.kick.com/public/v1/channels?slug=" + slug;

        ResponseEntity<String> apiResp = rest.exchange(apiUrl, HttpMethod.GET, apiReq, String.class);

        // ⬇️ Mostramos la respuesta en consola
        logger.debug("📺 [KICK API] Respuesta Kick API: {}", apiResp.getBody());

        return ResponseEntity.status(apiResp.getStatusCode()).body(apiResp.getBody());
    }


    // Clase auxiliar para deserializar la respuesta del token
    private static class AccessTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
