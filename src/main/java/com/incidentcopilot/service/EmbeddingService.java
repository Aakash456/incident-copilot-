package com.incidentcopilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmbeddingService {
    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper om = new ObjectMapper();
    @Value("${app.openai.apiKey:}") private String apiKey;
    @Value("${app.openai.baseUrl:https://api.openai.com/v1}") private String baseUrl;
    @Value("${app.openai.embeddingsModel:text-embedding-3-large}") private String model;

    public double[] embed(String text) throws IOException {
        String payload = "{\"model\":\""+model+"\",\"input\":"+om.writeValueAsString(text)+"}";
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), payload);
        Request req = new Request.Builder()
                .url(baseUrl + "/embeddings")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body).build();
        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new IOException("Embeddings failed: "+resp.code()+" "+resp.message());
            JsonNode root = om.readTree(resp.body().string());
            JsonNode arr = root.at("/data/0/embedding");
            double[] vec = new double[arr.size()];
            for (int i=0;i<arr.size();i++) vec[i] = arr.get(i).asDouble();
            return vec;
        }
    }
}
