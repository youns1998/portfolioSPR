package com.youns.portfoliospr.community.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentForm {

  @NotBlank(message = "댓글 내용을 입력해 주세요.")
  @Size(max = 1_000, message = "댓글은 1,000자 이내여야 합니다.")
  private String content = "";

  @NotBlank(message = "댓글 수정·삭제 비밀번호를 입력해 주세요.")
  @Size(min = 4, max = 64, message = "비밀번호는 4~64자여야 합니다.")
  private String password = "";

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
