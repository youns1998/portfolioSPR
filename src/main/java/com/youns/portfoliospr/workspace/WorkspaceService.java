package com.youns.portfoliospr.workspace;

import com.youns.portfoliospr.service.CommunityException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WorkspaceService {
  private final WorkProjectRepository projects;
  private final WorkItemRepository items;
  private final WorkAuditRepository audits;
  private final Clock clock;

  public WorkspaceService(
      WorkProjectRepository p, WorkItemRepository i, WorkAuditRepository a, Clock c) {
    projects = p;
    items = i;
    audits = a;
    clock = c;
  }

  public Page<WorkProject> search(String query, WorkStatus status, int page) {
    String term = Objects.toString(query, "").strip().toLowerCase(Locale.ROOT);
    if (term.length() > 100) throw fail(HttpStatus.BAD_REQUEST, "검색어는 100자 이내로 입력하세요.");
    Specification<WorkProject> spec =
        (root, q, cb) -> {
          String escaped = term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
          var text =
              cb.or(
                  cb.like(cb.lower(root.get("name")), "%" + escaped + "%", '\\'),
                  cb.like(cb.lower(root.get("code")), "%" + escaped + "%", '\\'),
                  cb.like(cb.lower(root.get("owner")), "%" + escaped + "%", '\\'));
          return status == null ? text : cb.and(text, cb.equal(root.get("status"), status));
        };
    return projects.findAll(
        spec, PageRequest.of(Math.max(0, page), 10, Sort.by(Sort.Direction.DESC, "id")));
  }

  public WorkProject get(Long id) {
    return projects.findById(id).orElseThrow(() -> fail(HttpStatus.NOT_FOUND, "사업을 찾을 수 없습니다."));
  }

  public List<WorkItem> items(Long id) {
    get(id);
    return items.findByProject_IdOrderByDueDateAscIdAsc(id);
  }

  public List<WorkAudit> history(Long id) {
    get(id);
    return audits.findByProjectIdOrderByCreatedAtDescIdDesc(id);
  }

  public record Stats(long total, long active, long completed, long overdue) {}

  public Stats stats() {
    var all = projects.findAll();
    var today = LocalDate.now(clock);
    return new Stats(
        all.size(),
        all.stream().filter(p -> p.getStatus() == WorkStatus.ACTIVE).count(),
        all.stream().filter(p -> p.getStatus() == WorkStatus.COMPLETED).count(),
        all.stream()
            .filter(p -> p.getStatus() != WorkStatus.COMPLETED && p.getDueDate().isBefore(today))
            .count());
  }

  @Transactional
  public WorkProject create(ProjectForm f, String actor) {
    validate(f, null);
    var p = projects.saveAndFlush(new WorkProject(f));
    audit(p.getId(), actor, "사업 등록 · " + p.getCode());
    return p;
  }

  @Transactional
  public void update(Long id, ProjectForm f, String actor) {
    var p = get(id);
    if (!Objects.equals(p.getVersion(), f.getVersion()))
      throw fail(HttpStatus.CONFLICT, "다른 사용자가 수정했습니다. 새로고침 후 다시 시도하세요.");
    validate(f, id);
    if (f.getStatus() == WorkStatus.COMPLETED
        && items.countByProject_IdAndStatusNot(id, ItemStatus.DONE) > 0)
      throw fail(HttpStatus.CONFLICT, "진행 중인 업무를 먼저 완료하세요.");
    var before = p.getStatus();
    p.update(f);
    projects.flush();
    audit(id, actor, "사업 수정 · " + before.getLabel() + " → " + p.getStatus().getLabel());
  }

  @Transactional
  public void delete(Long id, String actor) {
    var p = get(id);
    if (items.existsByProject_Id(id)) throw fail(HttpStatus.CONFLICT, "업무가 연결된 사업은 삭제할 수 없습니다.");
    audit(id, actor, "사업 삭제 · " + p.getCode());
    projects.delete(p);
  }

  @Transactional
  public WorkItem addItem(Long projectId, ItemForm form, String actor) {
    var p = get(projectId);
    if (p.getStatus() == WorkStatus.COMPLETED)
      throw fail(HttpStatus.CONFLICT, "완료된 사업에는 업무를 추가할 수 없습니다.");
    var i = items.saveAndFlush(new WorkItem(p, form));
    audit(projectId, actor, "업무 등록 · " + i.getTitle());
    return i;
  }

  @Transactional
  public void transition(
      Long projectId, Long itemId, ItemStatus next, String memo, Long version, String actor) {
    var p = get(projectId);
    var i =
        items
            .findById(itemId)
            .filter(v -> v.getProjectId().equals(projectId))
            .orElseThrow(() -> fail(HttpStatus.NOT_FOUND, "업무를 찾을 수 없습니다."));
    if (p.getStatus() == WorkStatus.COMPLETED)
      throw fail(HttpStatus.CONFLICT, "완료 사업의 상태는 변경할 수 없습니다.");
    if (!Objects.equals(i.getVersion(), version))
      throw fail(HttpStatus.CONFLICT, "업무가 변경됐습니다. 새로고침 후 다시 시도하세요.");
    if (next == null || !i.getStatus().canMoveTo(next))
      throw fail(HttpStatus.CONFLICT, "접수 → 처리 중 → 처리 완료 순서로 변경하세요. 완료 후 재개는 처리 중으로 변경합니다.");
    String note = Objects.toString(memo, "").strip();
    if (note.isBlank() || note.length() > 1000)
      throw fail(HttpStatus.BAD_REQUEST, "조치 메모는 1~1000자로 입력하세요.");
    var before = i.getStatus();
    i.transition(next, note);
    items.flush();
    audit(
        projectId,
        actor,
        "업무 상태 · "
            + i.getTitle()
            + " · "
            + before.getLabel()
            + " → "
            + next.getLabel()
            + " · "
            + note);
  }

  public String exportCsv() {
    StringBuilder out = new StringBuilder("\ufeff사업코드,사업명,담당,시작일,마감일,상태\r\n");
    for (var p : projects.findAll(Sort.by("code"))) {
      out.append(
              List.of(
                      p.getCode(),
                      p.getName(),
                      p.getOwner(),
                      p.getStartDate().toString(),
                      p.getDueDate().toString(),
                      p.getStatus().getLabel())
                  .stream()
                  .map(WorkspaceService::csvCell)
                  .collect(Collectors.joining(",")))
          .append("\r\n");
    }
    return out.toString();
  }

  public static String csvCell(String raw) {
    String value = Objects.toString(raw, "");
    String trimmed = value.stripLeading();
    if (!trimmed.isEmpty() && "=+-@".indexOf(trimmed.charAt(0)) >= 0) value = "'" + value;
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }

  private void validate(ProjectForm f, Long id) {
    if (f.getStartDate() == null
        || f.getDueDate() == null
        || f.getDueDate().isBefore(f.getStartDate()))
      throw fail(HttpStatus.BAD_REQUEST, "마감일은 시작일보다 빠를 수 없습니다.");
    String code = f.getCode().strip().toUpperCase(Locale.ROOT);
    if (id == null ? projects.existsByCode(code) : projects.existsByCodeAndIdNot(code, id))
      throw fail(HttpStatus.CONFLICT, "이미 등록된 사업 코드입니다.");
  }

  private void audit(Long id, String actor, String action) {
    audits.save(new WorkAudit(id, actor, action, LocalDateTime.now(clock)));
  }

  private static CommunityException fail(HttpStatus status, String text) {
    return new CommunityException(status, text);
  }
}
