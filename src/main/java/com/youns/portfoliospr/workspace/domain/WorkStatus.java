package com.youns.portfoliospr.workspace.domain;

public enum WorkStatus {
  PLANNED("계획"),
  ACTIVE("진행"),
  COMPLETED("완료");
  private final String label;

  WorkStatus(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
