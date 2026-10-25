package com.youns.portfoliospr.workspace;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WorkspaceLanding {
  @GetMapping("/")
  public String home() {
    return "redirect:/workspace";
  }
}
