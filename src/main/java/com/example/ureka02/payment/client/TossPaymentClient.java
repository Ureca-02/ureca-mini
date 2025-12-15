package com.example.ureka02.payment.client;

import com.example.ureka02.payment.dto.TossConfirmRequest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
public class TossPaymentClient {

    @Value("${toss.secret-key}")
    private String secretKey;

    public JsonNode confirmPayment(TossConfirmRequest request) {
        try {
            String auth = Base64.getEncoder()
                    .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + auth);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TossConfirmRequest> httpEntity = new HttpEntity<>(request, headers);

            log.info("Toss 결제 승인 요청 - orderId: {}, amount: {}",
                    request.getOrderId(), request.getAmount());

            ResponseEntity<JsonNode> response = new RestTemplate().postForEntity(
                    "https://api.tosspayments.com/v1/payments/confirm",
                    httpEntity,
                    JsonNode.class
            );

            log.info("Toss 결제 승인 성공 - orderId: {}", request.getOrderId());
            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Toss 결제 승인 실패 - orderId: {}, 에러: {}",
                    request.getOrderId(), e.getResponseBodyAsString());
            throw new RuntimeException("Toss 결제 승인 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Toss API 호출 중 예외 발생 - orderId: {}", request.getOrderId(), e);
            throw new RuntimeException("Toss API 호출 실패: " + e.getMessage(), e);
        }
    }
}