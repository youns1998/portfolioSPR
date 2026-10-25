package com.youns.portfoliospr.community.service;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {

  private static final int ITERATIONS = 210_000;
  private static final int KEY_LENGTH = 256;
  private final SecureRandom secureRandom = new SecureRandom();

  public String hash(String password) {
    byte[] salt = new byte[16];
    secureRandom.nextBytes(salt);
    byte[] derived = derive(password.toCharArray(), salt, ITERATIONS);
    return ITERATIONS
        + ":"
        + Base64.getEncoder().encodeToString(salt)
        + ":"
        + Base64.getEncoder().encodeToString(derived);
  }

  public boolean matches(String password, String encoded) {
    try {
      String[] parts = encoded.split(":", 3);
      int iterations = Integer.parseInt(parts[0]);
      byte[] salt = Base64.getDecoder().decode(parts[1]);
      byte[] expected = Base64.getDecoder().decode(parts[2]);
      byte[] actual = derive(password.toCharArray(), salt, iterations);
      return MessageDigest.isEqual(expected, actual);
    } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
      return false;
    }
  }

  private byte[] derive(char[] password, byte[] salt, int iterations) {
    PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("비밀번호를 안전하게 처리할 수 없습니다.", ex);
    } finally {
      spec.clearPassword();
    }
  }
}
