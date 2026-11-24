package com.companio.service;

import com.companio.repository.TrustedContactRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Polls Telegram for "/start CODE" messages and links the sender's chat to the matching contact.
 */
@Service
public class TelegramConnectService {

    private static final Pattern UPDATE_ID = Pattern.compile("^(\\d+)");
    private static final Pattern CHAT_ID = Pattern.compile("\"chat\":\\{\"id\":(-?\\d+)");
    private static final Pattern START = Pattern.compile("\"text\":\"/start ([A-Za-z0-9_-]+)\"");

    @Autowired
    private TelegramClient telegram;

    @Autowired
    private TrustedContactRepository contacts;

    private long offset = 0;

    @Scheduled(fixedDelay = 3000)
    public void poll() {
        if (!telegram.enabled()) {
            return;
        }
        String body = telegram.getUpdates(offset);
        if (body == null) {
            return;
        }
        String[] chunks = body.split("\"update_id\":");
        for (int i = 1; i < chunks.length; i++) {
            String chunk = chunks[i];
            Matcher id = UPDATE_ID.matcher(chunk);
            if (id.find()) {
                offset = Math.max(offset, Long.parseLong(id.group(1)) + 1);
            }
            Matcher start = START.matcher(chunk);
            Matcher chat = CHAT_ID.matcher(chunk);
            if (start.find() && chat.find()) {
                String code = start.group(1);
                String chatId = chat.group(1);
                contacts.findByConnectCode(code).ifPresent(c -> {
                    c.setTelegramChatId(chatId);
                    contacts.save(c);
                    telegram.send(chatId, "Connected. You will now receive Companio alerts from "
                            + c.getUser().getName() + ".");
                    System.out.println("[TELEGRAM] contact " + c.getId() + " connected");
                });
            }
        }
    }
}
