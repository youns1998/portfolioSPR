package com.youns.portfoliospr.workspace.domain;

public enum ItemStatus {
  NEW("접수"),
  IN_PROGRESS("처리 중"),
  DONE("처리 완료");
  private final String label;

  ItemStatus(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public boolean canMoveTo(ItemStatus next) {
    return this == NEW && next == IN_PROGRESS
        || this == IN_PROGRESS && next == DONE
        || this == DONE && next == IN_PROGRESS;
  }
}
