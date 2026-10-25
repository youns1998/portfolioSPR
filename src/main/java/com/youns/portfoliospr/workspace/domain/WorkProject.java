package com.youns.portfoliospr.workspace.domain;

import com.youns.portfoliospr.workspace.web.form.ProjectForm;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "work_project", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class WorkProject {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 30)
  private String code;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false, length = 60)
  private String owner;

  @Column(nullable = false, length = 2000)
  private String description;

  @Column(nullable = false)
  private LocalDate startDate;

  @Column(nullable = false)
  private LocalDate dueDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WorkStatus status;

  @Version private Long version;

  protected WorkProject() {}

  public WorkProject(ProjectForm form) {
    update(form);
  }

  public void update(ProjectForm f) {
    code = f.getCode().strip().toUpperCase(java.util.Locale.ROOT);
    name = f.getName().strip();
    owner = f.getOwner().strip();
    description = f.getDescription().strip();
    startDate = f.getStartDate();
    dueDate = f.getDueDate();
    status = f.getStatus();
  }

  public Long getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public String getOwner() {
    return owner;
  }

  public String getDescription() {
    return description;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public WorkStatus getStatus() {
    return status;
  }

  public Long getVersion() {
    return version;
  }
}
