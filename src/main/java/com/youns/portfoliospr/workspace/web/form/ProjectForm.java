package com.youns.portfoliospr.workspace.web.form;

import com.youns.portfoliospr.workspace.domain.WorkProject;
import com.youns.portfoliospr.workspace.domain.WorkStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class ProjectForm {
  @NotBlank
  @Size(max = 30)
  @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9-]{2,29}", message = "사업 코드는 영문·숫자·하이픈 3~30자로 입력하세요.")
  private String code = "";

  @NotBlank
  @Size(max = 120)
  private String name = "";

  @NotBlank
  @Size(max = 60)
  private String owner = "";

  @NotNull
  @Size(max = 2000)
  private String description = "";

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate startDate;

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate dueDate;

  @NotNull private WorkStatus status = WorkStatus.PLANNED;
  private Long version;

  public static ProjectForm from(WorkProject p) {
    ProjectForm f = new ProjectForm();
    f.code = p.getCode();
    f.name = p.getName();
    f.owner = p.getOwner();
    f.description = p.getDescription();
    f.startDate = p.getStartDate();
    f.dueDate = p.getDueDate();
    f.status = p.getStatus();
    f.version = p.getVersion();
    return f;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String v) {
    code = v;
  }

  public String getName() {
    return name;
  }

  public void setName(String v) {
    name = v;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String v) {
    owner = v;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String v) {
    description = v;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate v) {
    startDate = v;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate v) {
    dueDate = v;
  }

  public WorkStatus getStatus() {
    return status;
  }

  public void setStatus(WorkStatus v) {
    status = v;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long v) {
    version = v;
  }
}
