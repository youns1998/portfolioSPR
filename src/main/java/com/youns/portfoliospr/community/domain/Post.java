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
    name = "posts",
    indexes = {
      @Index(name = "idx_post_board_created", columnList = "board_id,created_at"),
      @Index(name = "idx_post_title", columnList = "title")
    })
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "board_id", nullable = false)
  private Long boardId;

  @Column(nullable = false, length = 120)
  private String title;

  @Column(nullable = false, length = 30)
  private String author;

  @Column(nullable = false, length = 10_000)
  private String content;

  @Column(name = "password_hash", nullable = false, length = 180)
  private String passwordHash;

  @Column(name = "view_count", nullable = false)
  private long viewCount;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  protected Post() {}

  public Post(
      Long boardId,
      String title,
      String author,
      String content,
      String passwordHash,
      LocalDateTime now) {
    this.boardId = boardId;
    this.title = title;
    this.author = author;
    this.content = content;
    this.passwordHash = passwordHash;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void update(String title, String author, String content, LocalDateTime now) {
    this.title = title;
    this.author = author;
    this.content = content;
    this.updatedAt = now;
  }

  public void increaseViewCount() {
    this.viewCount++;
  }

  public Long getId() {
    return id;
  }

  public Long getBoardId() {
    return boardId;
  }

  public String getTitle() {
    return title;
  }

  public String getAuthor() {
    return author;
  }

  public String getContent() {
    return content;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public long getViewCount() {
    return viewCount;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
