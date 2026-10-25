package com.youns.portfoliospr.workspace.web;

import com.youns.portfoliospr.common.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice(basePackageClasses = WorkspaceController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkspaceAdvice {
  @ExceptionHandler(BusinessException.class)
  public String error(BusinessException e, HttpServletResponse response, Model model) {
    response.setStatus(e.getStatus().value());
    model.addAttribute("status", e.getStatus().value());
    model.addAttribute("message", e.getMessage());
    return "workspace/error";
  }

  @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
  public String conflict(HttpServletResponse response, Model model) {
    response.setStatus(409);
    model.addAttribute("status", 409);
    model.addAttribute("message", "다른 사용자가 수정했습니다. 목록에서 다시 열어 주세요.");
    return "workspace/error";
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  public String duplicate(HttpServletResponse response, Model model) {
    response.setStatus(409);
    model.addAttribute("status", 409);
    model.addAttribute("message", "데이터가 변경되었거나 사업 코드가 중복됩니다. 목록에서 다시 확인하세요.");
    return "workspace/error";
  }
}
