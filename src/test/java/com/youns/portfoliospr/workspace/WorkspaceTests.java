package com.youns.portfoliospr.workspace;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.youns.portfoliospr.common.BusinessException;
import com.youns.portfoliospr.workspace.domain.ItemStatus;
import com.youns.portfoliospr.workspace.domain.WorkItem;
import com.youns.portfoliospr.workspace.domain.WorkStatus;
import com.youns.portfoliospr.workspace.repository.WorkItemRepository;
import com.youns.portfoliospr.workspace.repository.WorkProjectRepository;
import com.youns.portfoliospr.workspace.service.WorkspaceService;
import com.youns.portfoliospr.workspace.web.form.ItemForm;
import com.youns.portfoliospr.workspace.web.form.ProjectForm;
import java.time.LocalDate;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Transactional
class WorkspaceTests {
  @Autowired WorkspaceService service;
  @Autowired WorkProjectRepository projects;
  @Autowired WorkItemRepository items;
  @Autowired WebApplicationContext context;
  MockMvc mvc;

  @BeforeEach
  void setup() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  ProjectForm form(String code) {
    var f = new ProjectForm();
    f.setCode(code);
    f.setName("가상 점검 사업");
    f.setOwner("테스트 담당");
    f.setDescription("합성 테스트 데이터");
    f.setStartDate(LocalDate.now());
    f.setDueDate(LocalDate.now().plusDays(30));
    f.setStatus(WorkStatus.ACTIVE);
    return f;
  }

  WorkItem task(Long id) {
    var f = new ItemForm();
    f.setTitle("자료 확인");
    f.setAssignee("가상 담당");
    f.setDueDate(LocalDate.now().plusDays(10));
    return service.addItem(id, f, "editor");
  }

  @Test
  void requiresAuthentication() throws Exception {
    mvc.perform(get("/workspace"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/workspace/login"));
  }

  @Test
  void loginAndProjectViewsRender() throws Exception {
    mvc.perform(get("/workspace/login"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("_csrf")));
    mvc.perform(get("/workspace").with(user("viewer").roles("VIEWER")))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("사업 현황")));
  }

  @Test
  void viewerCannotWrite() throws Exception {
    mvc.perform(post("/workspace/projects").with(user("viewer").roles("VIEWER")).with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void editorStillNeedsCsrf() throws Exception {
    mvc.perform(post("/workspace/projects").with(user("editor").roles("EDITOR")))
        .andExpect(status().isForbidden());
  }

  @Test
  void realLoginCredentialsWork() throws Exception {
    mvc.perform(
            post("/workspace/login")
                .with(csrf())
                .param("username", "editor")
                .param("password", "Portfolio-2026!"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/workspace"));
  }

  @Test
  void projectCreateReadUpdateDelete() {
    var p = service.create(form("TEST-CRUD"), "editor");
    assertEquals("TEST-CRUD", service.get(p.getId()).getCode());
    var f = ProjectForm.from(p);
    f.setName("수정한 사업");
    service.update(p.getId(), f, "editor");
    assertEquals("수정한 사업", service.get(p.getId()).getName());
    assertEquals(2, service.history(p.getId()).size());
    service.delete(p.getId(), "editor");
    assertThrows(BusinessException.class, () -> service.get(p.getId()));
  }

  @Test
  void duplicateCodesAreCaseInsensitive() {
    service.create(form("TEST-DUP"), "editor");
    assertThrows(BusinessException.class, () -> service.create(form("test-dup"), "editor"));
  }

  @Test
  void reversedDatesRejected() {
    var f = form("TEST-DATE");
    f.setDueDate(f.getStartDate().minusDays(1));
    assertThrows(BusinessException.class, () -> service.create(f, "editor"));
  }

  @Test
  void searchAndFilter() {
    var p = service.create(form("TEST-SEARCH"), "editor");
    assertEquals(1, service.search("test-search", WorkStatus.ACTIVE, 0).getTotalElements());
    assertEquals(0, service.search("test-search", WorkStatus.COMPLETED, 0).getTotalElements());
    assertEquals(0, service.search("TEST-SEARCH%", null, 0).getTotalElements());
  }

  @Test
  void statusTransitionAndAudit() {
    var p = service.create(form("TEST-FLOW"), "editor");
    var t = task(p.getId());
    service.transition(
        p.getId(), t.getId(), ItemStatus.IN_PROGRESS, "확인 시작", t.getVersion(), "editor");
    service.transition(p.getId(), t.getId(), ItemStatus.DONE, "조치 완료", t.getVersion(), "editor");
    assertEquals(ItemStatus.DONE, items.findById(t.getId()).orElseThrow().getStatus());
    assertEquals(4, service.history(p.getId()).size());
  }

  @Test
  void skippingStatusRejected() {
    var p = service.create(form("TEST-SKIP"), "editor");
    var t = task(p.getId());
    assertThrows(
        BusinessException.class,
        () ->
            service.transition(
                p.getId(), t.getId(), ItemStatus.DONE, "완료", t.getVersion(), "editor"));
  }

  @Test
  void crossProjectTaskAccessRejected() {
    var a = service.create(form("TEST-A"), "editor");
    var b = service.create(form("TEST-B"), "editor");
    var t = task(a.getId());
    assertThrows(
        BusinessException.class,
        () ->
            service.transition(
                b.getId(), t.getId(), ItemStatus.IN_PROGRESS, "시도", t.getVersion(), "editor"));
  }

  @Test
  void staleEditRejected() {
    var p = service.create(form("TEST-STALE"), "editor");
    var f = ProjectForm.from(p);
    var stale = ProjectForm.from(p);
    f.setName("다른 사용자의 변경");
    service.update(p.getId(), f, "editor");
    assertThrows(BusinessException.class, () -> service.update(p.getId(), stale, "editor"));
  }

  @Test
  void deletionAndCompletionGuardOpenTasks() {
    var p = service.create(form("TEST-GUARD"), "editor");
    task(p.getId());
    assertThrows(BusinessException.class, () -> service.delete(p.getId(), "editor"));
    var f = ProjectForm.from(p);
    f.setStatus(WorkStatus.COMPLETED);
    assertThrows(BusinessException.class, () -> service.update(p.getId(), f, "editor"));
  }

  @Test
  void fullLengthMemoFitsAudit() {
    var p = service.create(form("TEST-MEMO"), "editor");
    var t = task(p.getId());
    service.transition(
        p.getId(), t.getId(), ItemStatus.IN_PROGRESS, "가".repeat(1000), t.getVersion(), "editor");
    assertEquals(1000, items.findById(t.getId()).orElseThrow().getResolution().length());
  }

  @Test
  void csvEscapesDelimitersAndFormula() {
    assertEquals("\"a,\"\"b\"\"\"", WorkspaceService.csvCell("a,\"b\""));
    assertTrue(WorkspaceService.csvCell(" =1\n+2").startsWith("\"'"));
    assertTrue(service.exportCsv().startsWith("\ufeff"));
  }

  @Test
  void contentIsHtmlEscaped() throws Exception {
    var f = form("TEST-XSS");
    f.setName("<script>alert(1)</script>");
    var p = service.create(f, "editor");
    mvc.perform(get("/workspace/projects/" + p.getId()).with(user("viewer").roles("VIEWER")))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("&lt;script&gt;")));
  }

  @Test
  void formValidationDoesNotPersistInvalidProject() throws Exception {
    long before = projects.count();
    mvc.perform(
            post("/workspace/projects")
                .with(user("editor").roles("EDITOR"))
                .with(csrf())
                .param("code", "!")
                .param("name", "")
                .param("owner", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("workspace/form"));
    assertEquals(before, projects.count());
  }

  @Test
  void csvDownloadHasUtf8AndAttachment() throws Exception {
    mvc.perform(get("/workspace/export.csv").with(user("viewer").roles("VIEWER")))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition", "attachment; filename=projects.csv"));
  }
}
