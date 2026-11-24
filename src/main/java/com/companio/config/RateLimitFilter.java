package com.companio.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiter for the endpoints most worth protecting.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

    private record Rule(String method, String path, int max, long windowMs) {}

    private static final List<Rule> RULES = List.of(
            new Rule("POST", "/api/auth/login", 10, 60_000),
            new Rule("POST", "/api/users", 5, 60_000),
            new Rule("POST", "/api/sos/trigger", 5, 60_000)
    );

    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        Rule rule = null;
        for (Rule r : RULES) {
            if (r.method().equals(req.getMethod()) && r.path().equals(req.getRequestURI())) {
                rule = r;
                break;
            }
        }
        if (rule != null && !allow(rule, clientIp(req))) {
            res.setStatus(429);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Too many requests. Please wait a minute and try again.\"}");
            return;
        }
        chain.doFilter(req, res);
    }

    private boolean allow(Rule rule, String ip) {
        Deque<Long> q = hits.computeIfAbsent(rule.path() + "|" + ip, k -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        synchronized (q) {
            while (!q.isEmpty() && now - q.peekFirst() > rule.windowMs()) {
                q.pollFirst();
            }
            if (q.size() >= rule.max()) {
                return false;
            }
            q.addLast(now);
            return true;
        }
    }

    // Behind the Cloudflare tunnel every request comes from the tunnel, so use its header.
    private String clientIp(HttpServletRequest req) {
        String cf = req.getHeader("CF-Connecting-IP");
        return (cf != null && !cf.isBlank()) ? cf.trim() : req.getRemoteAddr();
    }
} 
