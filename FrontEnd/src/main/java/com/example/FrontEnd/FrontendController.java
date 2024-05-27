package com.example.frontend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class FrontendController {

    @Value("${backend.service.url}")
    private String backendServiceUrl;

    @GetMapping("/")
    public String getHelloFromBackend() {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(backendServiceUrl + "/hello", String.class);
        return "Response from backend: " + response;
    }
}
