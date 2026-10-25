package com.youns.portfoliospr.community.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PostForm {

  @NotBlank(message = "제목을 입력해 주세요.")
  @Size(max = 120, message = "제목은 120자 이내여야 합니다.")
  private String title = "";

  @NotBlank(message = "작성자 이름을 입력해 주세요.")
  @Size(max = 30, message = "작성자 이름은 30자 이내여야 합니다.")
  private String author = "";

  @NotBlank(message = "내용을 입력해 주세요.")
  @Size(max = 10_000, message = "내용은 10,000자 이내여야 합니다.")
  private String content = "";

  @NotBlank(message = "수정·삭제 비밀번호를 입력해 주세요.")
  @Size(min = 4, max = 64, message = "비밀번호는 4~64자여야 합니다.")
  private String password = "";

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

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
