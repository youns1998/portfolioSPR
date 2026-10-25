package com.youns.portfoliospr.community;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.youns.portfoliospr.community.config.DemoDataInitializer;
import com.youns.portfoliospr.community.domain.Board;
import com.youns.portfoliospr.community.domain.Post;
import com.youns.portfoliospr.community.repository.BoardRepository;
import com.youns.portfoliospr.community.repository.PostRepository;
import com.youns.portfoliospr.community.service.CommunityService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Transactional
class CommunityBoardTests {

  @Autowired private CommunityService communityService;
  @Autowired private BoardRepository boardRepository;
  @Autowired private PostRepository postRepository;
  @Autowired private DemoDataInitializer initializer;
  @Autowired private WebApplicationContext context;

  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  void startupProvidesFourBoardsAndDoesNotDuplicateDemoData() {
    var boards = communityService.findBoards();
    assertEquals(
        List.of("인기글", "게임", "음악", "운동"),
        boards.stream().map(item -> item.board().getName()).toList());
    assertTrue(boards.stream().allMatch(item -> item.postCount() == 45));

    initializer.run(new DefaultApplicationArguments(new String[0]));
    assertEquals(4, boardRepository.count());
    assertEquals(180, postRepository.count());
  }

  @Test
  void demoPostsAreSplitIntoTwentyTwentyAndFiveWithoutOverlap() {
    Long boardId = communityService.findBoards().getFirst().board().getId();
    Page<Post> first = communityService.findPosts(boardId, 0, "");
    Page<Post> second = communityService.findPosts(boardId, 1, "");
    Page<Post> third = communityService.findPosts(boardId, 2, "");

    assertEquals(20, first.getNumberOfElements());
    assertEquals(20, second.getNumberOfElements());
    assertEquals(5, third.getNumberOfElements());
    assertEquals(3, first.getTotalPages());
    assertFalse(third.hasNext());
    assertEquals(
        45,
        java.util.stream.Stream.of(first, second, third)
            .flatMap(Page::stream)
            .map(Post::getId)
            .distinct()
            .count());
    assertTrue(
        first
            .getContent()
            .getLast()
            .getCreatedAt()
            .isAfter(second.getContent().getFirst().getCreatedAt()));
  }

  @Test
  void twentyFirstPostAddsSecondPageAndUsesStableOrdering() throws Exception {
    LocalDateTime now = LocalDateTime.now();
    Board board = boardRepository.save(new Board("페이지 경계 테스트", "20개 경계 검증", "unused", now));
    postRepository.saveAll(
        IntStream.rangeClosed(1, 20)
            .mapToObj(
                index -> new Post(board.getId(), "경계 테스트 " + index, "작성자", "본문", "unused", now))
            .toList());
    assertEquals(1, communityService.findPosts(board.getId(), 0, "").getTotalPages());
    mvc.perform(get("/boards/{id}", board.getId()))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("class=\"pagination\""))));

    Post latest =
        postRepository.save(new Post(board.getId(), "경계 테스트 21", "작성자", "본문", "unused", now));
    Page<Post> first = communityService.findPosts(board.getId(), 0, "");
    Page<Post> second = communityService.findPosts(board.getId(), 1, "");
    assertEquals(2, first.getTotalPages());
    assertEquals(20, first.getNumberOfElements());
    assertEquals(1, second.getNumberOfElements());
    assertEquals(latest.getId(), first.getContent().getFirst().getId());
    mvc.perform(get("/boards/{id}", board.getId()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("class=\"pagination\"")))
        .andExpect(content().string(containsString("page=1")));
  }

  @Test
  void creationLinksAreHiddenAndDirectCreationRequestsAreRejected() throws Exception {
    Long boardId = communityService.findBoards().getFirst().board().getId();
    Post latest = communityService.findPosts(boardId, 0, "").getContent().getFirst();
    mvc.perform(get("/community"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(latest.getTitle())))
        .andExpect(
            content().string(containsString("/boards/" + boardId + "/posts/" + latest.getId())))
        .andExpect(content().string(not(containsString("/boards/new"))));
    mvc.perform(get("/boards/new")).andExpect(status().isNotFound());
    mvc.perform(
            post("/boards")
                .param("name", "새 게시판")
                .param("description", "생성 차단 확인")
                .param("password", "test-pass"))
        .andExpect(status().isNotFound());
    assertEquals(4, boardRepository.count());
  }

  @Test
  void postAndFormPagesRenderWithBoardNavigation() throws Exception {
    Long boardId = communityService.findBoards().getFirst().board().getId();
    Post latest = communityService.findPosts(boardId, 0, "").getContent().getFirst();
    mvc.perform(get("/boards/{boardId}/posts/{postId}", boardId, latest.getId()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(latest.getTitle())))
        .andExpect(content().string(containsString("게임")))
        .andExpect(content().string(not(containsString("H2 MEMORY"))));
    mvc.perform(get("/boards/{id}/posts/new", boardId))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("글쓰기")))
        .andExpect(content().string(containsString("취소")));
    mvc.perform(get("/boards/{boardId}/posts/{postId}/edit", boardId, latest.getId()))
        .andExpect(status().isOk());
    mvc.perform(get("/boards/{id}/edit", boardId)).andExpect(status().isOk());
  }

  @Test
  void commentsSharingAnIpRequireTheirOwnPasswordsEvenFromAnotherIp() throws Exception {
    Long boardId = communityService.findBoards().getFirst().board().getId();
    var posts = communityService.findPosts(boardId, 0, "").getContent();
    Long postId = posts.getFirst().getId();
    String commentsUrl = "/boards/" + boardId + "/posts/" + postId + "/comments";
    for (String password : List.of("first-comment-secret", "second-comment-secret")) {
      mvc.perform(
              post(commentsUrl)
                  .param("content", password.startsWith("first") ? "첫 댓글" : "두 번째 댓글")
                  .param("password", password)
                  .with(
                      request -> {
                        request.setRemoteAddr("192.168.0.14");
                        return request;
                      }))
          .andExpect(status().is3xxRedirection());
    }
    var comments = communityService.peekPost(boardId, postId).comments();
    assertEquals(2, comments.size());
    assertEquals(comments.getFirst().getIpHash(), comments.getLast().getIpHash());
    Long firstId = comments.getFirst().getId();
    Long secondId = comments.getLast().getId();
    mvc.perform(get("/boards/{boardId}/posts/{postId}", boardId, postId))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("delete-password-" + firstId)))
        .andExpect(content().string(not(containsString("first-comment-secret"))))
        .andExpect(content().string(not(containsString(comments.getFirst().getPasswordHash()))));

    mvc.perform(
            post(commentsUrl + "/" + firstId + "/edit")
                .param("content", "타인 수정 시도")
                .param("password", "second-comment-secret")
                .with(
                    request -> {
                      request.setRemoteAddr("192.168.0.14");
                      return request;
                    }))
        .andExpect(status().isForbidden());
    mvc.perform(
            post(commentsUrl + "/" + firstId + "/delete")
                .param("password", "second-comment-secret")
                .with(
                    request -> {
                      request.setRemoteAddr("192.168.0.14");
                      return request;
                    }))
        .andExpect(status().isForbidden());
    assertEquals(
        "첫 댓글", communityService.peekPost(boardId, postId).comments().getFirst().getContent());
    assertEquals(2, communityService.peekPost(boardId, postId).comments().size());
    mvc.perform(post(commentsUrl + "/" + firstId + "/delete")).andExpect(status().isBadRequest());
    mvc.perform(post(commentsUrl + "/" + firstId + "/edit").param("content", "비밀번호 누락"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/boards/"
                    + boardId
                    + "/posts/"
                    + posts.get(1).getId()
                    + "/comments/"
                    + firstId
                    + "/delete")
                .param("password", "first-comment-secret"))
        .andExpect(status().isNotFound());

    mvc.perform(
            post(commentsUrl + "/" + firstId + "/edit")
                .param("content", "다른 IP에서 수정")
                .param("password", "first-comment-secret")
                .with(
                    request -> {
                      request.setRemoteAddr("192.168.0.99");
                      return request;
                    }))
        .andExpect(status().is3xxRedirection());
    assertEquals(
        "다른 IP에서 수정",
        communityService.peekPost(boardId, postId).comments().getFirst().getContent());
    mvc.perform(
            post(commentsUrl + "/" + firstId + "/delete")
                .param("password", "first-comment-secret")
                .with(
                    request -> {
                      request.setRemoteAddr("192.168.0.99");
                      return request;
                    }))
        .andExpect(status().is3xxRedirection());
    var remaining = communityService.peekPost(boardId, postId).comments();
    assertEquals(1, remaining.size());
    assertEquals(secondId, remaining.getFirst().getId());
  }

  @Test
  void commentsAndPostsCannotBeCreatedWithoutAValidPassword() throws Exception {
    Long boardId = communityService.findBoards().getFirst().board().getId();
    Long postId = communityService.findPosts(boardId, 0, "").getContent().getFirst().getId();
    long initialCount = postRepository.count();
    for (String password : List.of("", "123", "    ", "a".repeat(65))) {
      mvc.perform(
              post("/boards/{boardId}/posts/{postId}/comments", boardId, postId)
                  .param("content", "유효성 검증 댓글")
                  .param("password", password))
          .andExpect(status().isOk())
          .andExpect(content().string(containsString("field-error")));
      mvc.perform(
              post("/boards/{boardId}/posts", boardId)
                  .param("title", "비밀번호 검증")
                  .param("author", "작성자")
                  .param("content", "본문")
                  .param("password", password))
          .andExpect(status().isOk())
          .andExpect(content().string(containsString("field-error")));
    }
    assertTrue(communityService.peekPost(boardId, postId).comments().isEmpty());
    assertEquals(initialCount, postRepository.count());
  }

  @Test
  void missingPagesAndInvalidRequestsKeepTheirHttpStatus() throws Exception {
    mvc.perform(get("/favicon.ico")).andExpect(status().isNotFound());
    mvc.perform(get("/missing-page")).andExpect(status().isNotFound());
    mvc.perform(get("/boards/not-a-number")).andExpect(status().isBadRequest());
    mvc.perform(get("/boards/1").param("page", "abc")).andExpect(status().isBadRequest());
    mvc.perform(post("/")).andExpect(status().isMethodNotAllowed());
  }

  @Test
  void searchAndPaginationRenderTogetherAndKeepTheKeyword() throws Exception {
    Long boardId = communityService.findBoards().getFirst().board().getId();
    assertEquals(45, communityService.findPosts(boardId, 0, "이야기").getTotalElements());
    mvc.perform(get("/boards/{id}", boardId).param("keyword", "이야기").param("page", "1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("page=2")))
        .andExpect(content().string(containsString("keyword=")))
        .andExpect(content().string(containsString("aria-current=\"page\"")));
    mvc.perform(get("/boards/{id}", boardId).param("keyword", "존재하지않는검색어"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("검색 결과가 없습니다.")))
        .andExpect(content().string(not(containsString("class=\"pagination\""))));
  }
}
