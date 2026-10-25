package com.youns.portfoliospr.community.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BoardForm {

  @NotBlank(message = "게시판 이름을 입력해 주세요.")
  @Size(max = 40, message = "게시판 이름은 40자 이내여야 합니다.")
  private String name = "";

  @NotBlank(message = "게시판 설명을 입력해 주세요.")
  @Size(max = 200, message = "게시판 설명은 200자 이내여야 합니다.")
  private String description = "";

  @NotBlank(message = "관리 비밀번호를 입력해 주세요.")
  @Size(min = 4, max = 64, message = "비밀번호는 4~64자여야 합니다.")
  private String password = "";

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
