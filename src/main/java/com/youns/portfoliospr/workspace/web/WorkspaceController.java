package com.youns.portfoliospr.workspace.web;

import com.youns.portfoliospr.workspace.domain.ItemStatus;
import com.youns.portfoliospr.workspace.domain.WorkStatus;
import com.youns.portfoliospr.workspace.service.WorkspaceService;
import com.youns.portfoliospr.workspace.web.form.ItemForm;
import com.youns.portfoliospr.workspace.web.form.ProjectForm;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/workspace")
public class WorkspaceController {
  private final WorkspaceService service;

  public WorkspaceController(WorkspaceService s) {
    service = s;
  }

  @ModelAttribute("canEdit")
  public boolean canEdit(org.springframework.security.core.Authentication auth) {
    return auth != null
        && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EDITOR"));
  }

  @ModelAttribute("workStatuses")
  public WorkStatus[] statuses() {
    return WorkStatus.values();
  }

  @GetMapping("/login")
  public String login() {
    return "workspace/login";
  }

  @GetMapping
  public String index(
      @RequestParam(defaultValue = "") String q,
      @RequestParam(required = false) WorkStatus status,
      @RequestParam(defaultValue = "0") int page,
      Model model) {
    model.addAttribute("projects", service.search(q, status, page));
    model.addAttribute("stats", service.stats());
    model.addAttribute("q", q);
    model.addAttribute("selectedStatus", status);
    model.addAttribute("today", LocalDate.now());
    return "workspace/index";
  }

  @GetMapping("/projects/new")
  public String createForm(Model model) {
    ProjectForm f = new ProjectForm();
    f.setStartDate(LocalDate.now());
    f.setDueDate(LocalDate.now().plusDays(30));
    model.addAttribute("projectForm", f);
    model.addAttribute("editing", false);
    return "workspace/form";
  }

  @PostMapping("/projects")
  public String create(
      @Valid @ModelAttribute("projectForm") ProjectForm f,
      BindingResult errors,
      Model model,
      Principal actor) {
    if (errors.hasErrors()) {
      model.addAttribute("editing", false);
      return "workspace/form";
    }
    var p = service.create(f, actor.getName());
    return "redirect:/workspace/projects/" + p.getId();
  }

  @GetMapping("/projects/{id}")
  public String detail(@PathVariable Long id, Model model) {
    model.addAttribute("project", service.get(id));
    model.addAttribute("items", service.items(id));
    model.addAttribute("history", service.history(id));
    model.addAttribute("itemForm", new ItemForm());
    model.addAttribute("itemStatuses", ItemStatus.values());
    return "workspace/detail";
  }

  @GetMapping("/projects/{id}/edit")
  public String edit(@PathVariable Long id, Model model) {
    var p = service.get(id);
    model.addAttribute("project", p);
    model.addAttribute("projectForm", ProjectForm.from(p));
    model.addAttribute("editing", true);
    return "workspace/form";
  }

  @PostMapping("/projects/{id}/edit")
  public String update(
      @PathVariable Long id,
      @Valid @ModelAttribute("projectForm") ProjectForm f,
      BindingResult errors,
      Model model,
      Principal actor) {
    if (errors.hasErrors()) {
      model.addAttribute("project", service.get(id));
      model.addAttribute("editing", true);
      return "workspace/form";
    }
    service.update(id, f, actor.getName());
    return "redirect:/workspace/projects/" + id;
  }

  @PostMapping("/projects/{id}/delete")
  public String delete(@PathVariable Long id, Principal actor, RedirectAttributes redirect) {
    service.delete(id, actor.getName());
    redirect.addFlashAttribute("notice", "사업을 삭제했습니다.");
    return "redirect:/workspace";
  }

  @PostMapping("/projects/{id}/items")
  public String add(
      @PathVariable Long id,
      @Valid @ModelAttribute("itemForm") ItemForm f,
      BindingResult errors,
      Model model,
      Principal actor) {
    if (errors.hasErrors()) {
      model.addAttribute("project", service.get(id));
      model.addAttribute("items", service.items(id));
      model.addAttribute("history", service.history(id));
      model.addAttribute("itemStatuses", ItemStatus.values());
      return "workspace/detail";
    }
    service.addItem(id, f, actor.getName());
    return "redirect:/workspace/projects/" + id;
  }

  @PostMapping("/projects/{id}/items/{itemId}/status")
  public String transition(
      @PathVariable Long id,
      @PathVariable Long itemId,
      @RequestParam ItemStatus status,
      @RequestParam String memo,
      @RequestParam Long version,
      Principal actor) {
    service.transition(id, itemId, status, memo, version, actor.getName());
    return "redirect:/workspace/projects/" + id;
  }

  @GetMapping("/export.csv")
  @ResponseBody
  public ResponseEntity<String> export() {
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=projects.csv")
        .body(service.exportCsv());
  }
}
