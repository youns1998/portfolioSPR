package com.youns.portfoliospr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class ClientIdentityService {

    private final String secret;

    public ClientIdentityService(@Value("${app.ip-hash-secret}") String secret) {
        this.secret = secret;
    }

    public ClientIdentity from(String remoteAddress) {
        String ip = remoteAddress == null || remoteAddress.isBlank() ? "unknown" : remoteAddress.trim();
        String hash = sha256(secret + ":" + ip);
        return new ClientIdentity(hash, "익명-" + hash.substring(0, 4).toUpperCase(), mask(ip));
    }

    private String mask(String ip) {
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "local";
        }
        if (ip.contains(".")) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) return parts[0] + "." + parts[1] + "." + parts[2] + ".*";
        }
        if (ip.contains(":")) {
            String[] parts = ip.split(":");
            StringBuilder masked = new StringBuilder();
            for (int i = 0; i < Math.min(4, parts.length); i++) {
                if (i > 0) masked.append(':');
                masked.append(parts[i]);
            }
            return masked.append(":*").toString();
        }
        return "unknown";
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", ex);
        }
    }

    public record ClientIdentity(String hash, String alias, String maskedIp) {
    }
}
