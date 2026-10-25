package com.youns.portfoliospr.community.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "boards",
    uniqueConstraints = @UniqueConstraint(name = "uk_board_name", columnNames = "name"))
public class Board {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 40)
  private String name;

  @Column(nullable = false, length = 200)
  private String description;

  @Column(name = "password_hash", nullable = false, length = 180)
  private String passwordHash;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  protected Board() {}

  public Board(String name, String description, String passwordHash, LocalDateTime createdAt) {
    this.name = name;
    this.description = description;
    this.passwordHash = passwordHash;
    this.createdAt = createdAt;
  }

  public void update(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
