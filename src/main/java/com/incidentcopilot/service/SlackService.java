package com.incidentcopilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SlackService {
    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper om = new ObjectMapper();
    @Value("${app.slack.botToken:}") private String token;
    @Value("${app.slack.channelId:}") private String channelId;

    public String postMessage(String text) throws Exception {
        if (token==null || token.isBlank()) throw new IllegalStateException("SLACK_BOT_TOKEN missing");
        if (channelId==null || channelId.isBlank()) throw new IllegalStateException("SLACK_CHANNEL_ID missing");
        String json = om.writeValueAsString(java.util.Map.of("channel", channelId, "text", text));
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
        Request req = new Request.Builder().url("https://slack.com/api/chat.postMessage")
                .addHeader("Authorization", "Bearer "+token).post(body).build();
        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new RuntimeException("Slack error: "+resp.code());
            JsonNode root = om.readTree(resp.body().string());
            if (!root.path("ok").asBoolean()) throw new RuntimeException("Slack API not ok: "+root.toString());
            return root.path("ts").asText();
        }
    }
}
