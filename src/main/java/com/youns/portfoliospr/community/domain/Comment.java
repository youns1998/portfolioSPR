package com.youns.portfoliospr.community.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "comments",
    indexes = @Index(name = "idx_comment_post_created", columnList = "post_id,created_at"))
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "post_id", nullable = false)
  private Long postId;

  @Column(nullable = false, length = 1_000)
  private String content;

  @Column(name = "author_alias", nullable = false, length = 20)
  private String authorAlias;

  @Column(name = "masked_ip", nullable = false, length = 80)
  private String maskedIp;

  @Column(name = "ip_hash", nullable = false, length = 64)
  private String ipHash;

  @Column(name = "password_hash", nullable = false, length = 180)
  private String passwordHash;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  protected Comment() {}

  public Comment(
      Long postId,
      String content,
      String authorAlias,
      String maskedIp,
      String ipHash,
      String passwordHash,
      LocalDateTime now) {
    this.postId = postId;
    this.content = content;
    this.authorAlias = authorAlias;
    this.maskedIp = maskedIp;
    this.ipHash = ipHash;
    this.passwordHash = passwordHash;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void update(String content, LocalDateTime now) {
    this.content = content;
    this.updatedAt = now;
  }

  public Long getId() {
    return id;
  }

  public Long getPostId() {
    return postId;
  }

  public String getContent() {
    return content;
  }

  public String getAuthorAlias() {
    return authorAlias;
  }

  public String getMaskedIp() {
    return maskedIp;
  }

  public String getIpHash() {
    return ipHash;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
