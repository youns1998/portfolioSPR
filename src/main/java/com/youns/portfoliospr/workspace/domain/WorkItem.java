package com.youns.portfoliospr.workspace.domain;

import com.youns.portfoliospr.workspace.web.form.ItemForm;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "work_item", indexes = @Index(columnList = "project_id"))
public class WorkItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  private WorkProject project;

  @Column(nullable = false, length = 150)
  private String title;

  @Column(nullable = false, length = 60)
  private String assignee;

  @Column(nullable = false)
  private LocalDate dueDate;

  @Column(nullable = false, length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ItemStatus status = ItemStatus.NEW;

  @Column(nullable = false, length = 1000)
  private String resolution = "";

  @Version private Long version;

  protected WorkItem() {}

  public WorkItem(WorkProject p, ItemForm f) {
    project = p;
    title = f.getTitle().strip();
    assignee = f.getAssignee().strip();
    dueDate = f.getDueDate();
    description = f.getDescription().strip();
  }

  public void transition(ItemStatus next, String memo) {
    status = next;
    resolution = memo;
  }

  public Long getId() {
    return id;
  }

  public Long getProjectId() {
    return project.getId();
  }

  public String getTitle() {
    return title;
  }

  public String getAssignee() {
    return assignee;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public String getDescription() {
    return description;
  }

  public ItemStatus getStatus() {
    return status;
  }

  public String getResolution() {
    return resolution;
  }

  public Long getVersion() {
    return version;
  }
}
