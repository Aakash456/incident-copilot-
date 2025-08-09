package com.incidentcopilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GitHubService {
    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper om = new ObjectMapper();
    @Value("${app.github.token:}") private String token;
    @Value("${app.github.repo:}") private String repo;

    public String createIssue(String title, String body) throws Exception {
        if (token==null || token.isBlank()) throw new IllegalStateException("GITHUB_TOKEN missing");
        if (repo==null || repo.isBlank()) throw new IllegalStateException("GITHUB_REPO missing");
        String json = om.writeValueAsString(java.util.Map.of("title", title, "body", body));
        RequestBody rb = RequestBody.create(MediaType.parse("application/json"), json);
        Request req = new Request.Builder()
                .url("https://api.github.com/repos/"+repo+"/issues")
                .addHeader("Authorization", "Bearer "+token)
                .addHeader("Accept", "application/vnd.github+json")
                .post(rb).build();
        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new RuntimeException("GitHub error: "+resp.code());
            JsonNode root = om.readTree(resp.body().string());
            return root.path("html_url").asText();
        }
    }
}
