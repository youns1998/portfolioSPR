package com.youns.portfoliospr.workspace.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_audit", indexes = @Index(columnList = "projectId"))
public class WorkAudit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long projectId;

  @Column(nullable = false, length = 80)
  private String actor;

  @Column(nullable = false, length = 2000)
  private String action;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  protected WorkAudit() {}

  public WorkAudit(Long p, String actor, String action, LocalDateTime time) {
    projectId = p;
    this.actor = actor;
    this.action = action;
    createdAt = time;
  }

  public Long getId() {
    return id;
  }

  public Long getProjectId() {
    return projectId;
  }

  public String getActor() {
    return actor;
  }

  public String getAction() {
    return action;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
