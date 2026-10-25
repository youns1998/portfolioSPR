package com.youns.portfoliospr.workspace.web.form;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class ItemForm {
  @NotBlank
  @Size(max = 150)
  private String title = "";

  @NotBlank
  @Size(max = 60)
  private String assignee = "";

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate dueDate;

  @NotNull
  @Size(max = 2000)
  private String description = "";

  public String getTitle() {
    return title;
  }

  public void setTitle(String v) {
    title = v;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String v) {
    assignee = v;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate v) {
    dueDate = v;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String v) {
    description = v;
  }
}
