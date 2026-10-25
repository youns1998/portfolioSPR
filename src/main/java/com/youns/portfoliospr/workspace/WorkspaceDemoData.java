package com.youns.portfoliospr.workspace;

import java.time.*;
import org.springframework.boot.*;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceDemoData implements ApplicationRunner {
  private final WorkProjectRepository projects;
  private final WorkspaceService service;
  private final Clock clock;

  public WorkspaceDemoData(WorkProjectRepository p, WorkspaceService s, Clock c) {
    projects = p;
    service = s;
    clock = c;
  }

  public void run(ApplicationArguments args) {
    if (projects.count() > 0) return;
    String[] names = {"생활권 보행환경 정비", "공공시설 배수 점검", "안전시설 위치 조사", "준공도서 정리"};
    for (int n = 0; n < names.length; n++) {
      ProjectForm f = new ProjectForm();
      f.setCode("DEMO-2026-" + (n + 1));
      f.setName(names[n]);
      f.setOwner("가상 사업지원팀 " + (n + 1));
      f.setDescription("실제 국가사업이나 기관 실적이 아닌 개인 포트폴리오용 가상 사업입니다.");
      f.setStartDate(LocalDate.now(clock).minusDays(20));
      f.setDueDate(LocalDate.now(clock).plusDays(n == 0 ? -2 : 14 + n * 7));
      f.setStatus(n == 3 ? WorkStatus.PLANNED : WorkStatus.ACTIVE);
      var p = service.create(f, "demo-seed");
      for (int j = 0; j < 3; j++) {
        ItemForm item = new ItemForm();
        item.setTitle(new String[] {"도면·자료 접수", "현장 점검 결과 정리", "검토 의견 반영"}[j]);
        item.setAssignee("가상 담당 " + (j + 1));
        item.setDueDate(f.getDueDate());
        item.setDescription("가상 샘플 문서/업무입니다. 실제 개인정보는 포함하지 않습니다.");
        service.addItem(p.getId(), item, "demo-seed");
      }
    }
  }
}
