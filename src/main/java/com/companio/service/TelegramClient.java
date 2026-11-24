package com.companio.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Thin wrapper around the Telegram Bot API. The token is never logged.
 */
@Service
public class TelegramClient {

    @Value("${telegram.bot-token:}")
    private String botToken;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public boolean enabled() {
        return botToken != null && !botToken.isBlank();
    }

    public void send(String chatId, String text) {
        if (!enabled() || chatId == null || chatId.isBlank()) {
            return;
        }
        try {
            String body = "chat_id=" + URLEncoder.encode(chatId, StandardCharsets.UTF_8)
                    + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(
                            URI.create("https://api.telegram.org/bot" + botToken + "/sendMessage"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(r -> System.out.println(
                            "[TELEGRAM] chat " + chatId + " -> HTTP " + r.statusCode()))
                    .exceptionally(ex -> {
                        System.out.println("[TELEGRAM] send failed: " + ex.getClass().getSimpleName());
                        return null;
                    });
        } catch (Exception e) {
            System.out.println("[TELEGRAM] send failed: " + e.getClass().getSimpleName());
        }
    }

    /** Returns the raw JSON of new updates, or null on any problem. */
    public String getUpdates(long offset) {
        if (!enabled()) {
            return null;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(
                            "https://api.telegram.org/bot" + botToken
                                    + "/getUpdates?timeout=0&offset=" + offset))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> r = http.send(request, HttpResponse.BodyHandlers.ofString());
            return r.statusCode() == 200 ? r.body() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
