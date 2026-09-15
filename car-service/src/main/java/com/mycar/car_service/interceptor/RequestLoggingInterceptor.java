package com.mycar.car_service.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = "startTime";
    private static final int MAX_REQUESTS_PER_WINDOW = 200;
    private static final long TIME_WINDOW_MS = 60000;
    private static final int MAX_BAD_REQUESTS = 10;
    private static final long BLOCK_DURATION_MS = 60000;
    private final Map<String, IpRateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    private final Map<String, BadRequestsInfo> badRequestsMap = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long currentTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, currentTime);
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        if (isSuspiciousBot(userAgent)) {
            log.warn("Заблокирован подозрительный бот/скрипт. IP: {} | User-Agent: {}", clientIp, userAgent);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Access denied: Suspicious bot or automated script detected.");
            return false;
        }

        if (isRateLimited(clientIp, currentTime)) {
            log.warn("Превышен лимит запросов! IP: {} временно заблокирован.", clientIp);
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Too many requests!\"}");
            return false;
        }
        BadRequestsInfo badInfo = badRequestsMap.get(clientIp);

        if (badInfo != null) {
            if (badInfo.getBlockedUntil() > 0 && currentTime > badInfo.getBlockedUntil()) {
                log.info("Время блокировки для IP: '{}' истекло. Сброс счетчика.", clientIp);
                badRequestsMap.remove(clientIp);
            } else if (badInfo.getBlockedUntil() > 0) {
                long remainingSec = (badInfo.getBlockedUntil() - currentTime) / 1000;
                log.warn("IP: '{}' заблокирован. Осталось блокировки: '{}' сек.", clientIp, remainingSec);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Too many bad requests, try again in " + remainingSec + " seconds.\"}");
                return false;
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long duration = (startTime != null) ? (System.currentTimeMillis() - startTime) : 0;
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = (queryString != null) ? uri + "?" + queryString : uri;
        int status = response.getStatus();
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String lastProxy = extractLastProxyIp(xForwardedFor);

        if (duration > 200) {
            log.warn("Медленный запрос: '{}' занял '{}' мс", fullPath, duration);
        }

        if (status >= 400) {
            BadRequestsInfo info = badRequestsMap.computeIfAbsent(clientIp, key -> new BadRequestsInfo());
            int currentCount = info.getCount().incrementAndGet();
            if (currentCount >= MAX_BAD_REQUESTS && info.getBlockedUntil() == 0) {
                info.setBlockedUntil(System.currentTimeMillis() + BLOCK_DURATION_MS);
                log.warn("IP '{}' превысил лимит ошибок '{}'. Заблокирован на '{}' мс",
                        clientIp, currentCount, BLOCK_DURATION_MS);
            }
        } else {
            BadRequestsInfo info = badRequestsMap.get(clientIp);
            if (info != null && info.getBlockedUntil() == 0) {
                badRequestsMap.remove(clientIp);
            }
        }

        log.info("{} {} | Status: {} | Time: {} ms | IP: {} | Client: {} | LastProxy: {}",
                method, fullPath, status, duration, clientIp, userAgent, lastProxy);
    }

    private boolean isRateLimited(String ip, long currentTime) {
        IpRateLimitInfo info = rateLimitMap.compute(ip, (key, existingInfo) -> {
            if (existingInfo == null || (currentTime - existingInfo.startTime) > TIME_WINDOW_MS) {
                return new IpRateLimitInfo(currentTime, new AtomicInteger(1));
            } else {
                existingInfo.count.incrementAndGet();
                return existingInfo;
            }
        });

        return info.count.get() > MAX_REQUESTS_PER_WINDOW;
    }

    private boolean isSuspiciousBot(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return true;
        }
        String lowerCaseAgent = userAgent.toLowerCase();
        return lowerCaseAgent.contains("python") || lowerCaseAgent.contains("scrapy") || lowerCaseAgent.contains("httpclient");
    }

    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String extractLastProxyIp(String xForwardedFor) {
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String[] parts = xForwardedFor.split(",");
            return parts[parts.length - 1].trim();
        }
        return xForwardedFor;
    }

    private static class IpRateLimitInfo {
        final long startTime;
        final AtomicInteger count;

        IpRateLimitInfo(long startTime, AtomicInteger count) {
            this.startTime = startTime;
            this.count = count;
        }
    }

    private static class BadRequestsInfo {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long blockedUntil = 0;

        public AtomicInteger getCount() { return count; }
        public long getBlockedUntil() { return blockedUntil; }
        public void setBlockedUntil(long blockedUntil) { this.blockedUntil = blockedUntil; }
    }
}