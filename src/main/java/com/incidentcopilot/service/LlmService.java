package com.incidentcopilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class LlmService {
    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper om = new ObjectMapper();
    @Value("${app.openai.apiKey:}") private String apiKey;
    @Value("${app.openai.baseUrl:https://api.openai.com/v1}") private String baseUrl;
    @Value("${app.openai.chatModel:gpt-4o-mini}") private String model;

    public JsonNode reason(String system, String prompt, Map<String,Object> toolsSpec) throws IOException {
        String toolHint = toolsSpec != null ? ("\nTools JSON schema: " + om.writeValueAsString(toolsSpec)) : "";
        String bodyJson = om.writeValueAsString(Map.of(
                "model", model,
                "response_format", Map.of("type","json_object"),
                "messages", new Object[] {
                        new java.util.HashMap<>() {{ put("role","system"); put("content", system); }},
                        new java.util.HashMap<>() {{ put("role","user"); put("content", prompt + toolHint); }}
                }
        ));
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), bodyJson);
        Request req = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body).build();
        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new IOException("LLM failed: "+resp.code()+" "+resp.message());
            JsonNode root = om.readTree(resp.body().string());
            String content = root.at("/choices/0/message/content").asText();
            return om.readTree(content);
        }
    }
}
