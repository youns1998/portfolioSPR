package com.youns.portfoliospr.community.web;

import com.youns.portfoliospr.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BusinessException.class)
  public String handleCommunityException(
      BusinessException exception,
      HttpServletRequest request,
      HttpServletResponse response,
      Model model) {
    response.setStatus(exception.getStatus().value());
    return errorPage(exception.getStatus(), exception.getMessage(), request, model);
  }

  @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleNotFound(Exception exception, HttpServletRequest request, Model model) {
    return errorPage(HttpStatus.NOT_FOUND, "요청하신 페이지를 찾을 수 없습니다.", request, model);
  }

  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String handleBadRequest(Exception exception, HttpServletRequest request, Model model) {
    return errorPage(HttpStatus.BAD_REQUEST, "요청 값을 확인해 주세요.", request, model);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  public String handleMethodNotAllowed(
      Exception exception, HttpServletRequest request, Model model) {
    return errorPage(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다.", request, model);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String handleUnexpectedException(
      Exception exception, HttpServletRequest request, Model model) {
    log.error("요청 처리 실패: {} {}", request.getMethod(), request.getRequestURI(), exception);
    return errorPage(HttpStatus.INTERNAL_SERVER_ERROR, "요청을 처리하는 중 문제가 발생했습니다.", request, model);
  }

  private String errorPage(
      HttpStatus status, String message, HttpServletRequest request, Model model) {
    model.addAttribute("status", status.value());
    model.addAttribute("title", status.getReasonPhrase());
    model.addAttribute("message", message);
    model.addAttribute("path", request.getRequestURI());
    return "community/error";
  }
}
