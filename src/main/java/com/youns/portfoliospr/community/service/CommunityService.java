package com.youns.portfoliospr.community.service;

import com.youns.portfoliospr.common.BusinessException;
import com.youns.portfoliospr.community.domain.Board;
import com.youns.portfoliospr.community.domain.Comment;
import com.youns.portfoliospr.community.domain.Post;
import com.youns.portfoliospr.community.repository.BoardRepository;
import com.youns.portfoliospr.community.repository.CommentRepository;
import com.youns.portfoliospr.community.repository.PostRepository;
import com.youns.portfoliospr.community.service.ClientIdentityService.ClientIdentity;
import com.youns.portfoliospr.community.web.form.BoardForm;
import com.youns.portfoliospr.community.web.form.CommentForm;
import com.youns.portfoliospr.community.web.form.PostForm;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityService {

  private static final int PAGE_SIZE = 20;

  private final BoardRepository boardRepository;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final PasswordHasher passwordHasher;
  private final ClientIdentityService clientIdentityService;
  private final Clock clock;

  public CommunityService(
      BoardRepository boardRepository,
      PostRepository postRepository,
      CommentRepository commentRepository,
      PasswordHasher passwordHasher,
      ClientIdentityService clientIdentityService,
      Clock clock) {
    this.boardRepository = boardRepository;
    this.postRepository = postRepository;
    this.commentRepository = commentRepository;
    this.passwordHasher = passwordHasher;
    this.clientIdentityService = clientIdentityService;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public List<BoardOverview> findBoards() {
    return boardRepository.findAllByOrderByCreatedAtAsc().stream()
        .map(board -> new BoardOverview(board, postRepository.countByBoardId(board.getId())))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<BoardPreview> findBoardPreviews() {
    return findBoards().stream()
        .map(
            item ->
                new BoardPreview(
                    item.board(),
                    item.postCount(),
                    postRepository.findTop5ByBoardIdOrderByCreatedAtDescIdDesc(
                        item.board().getId())))
        .toList();
  }

  @Transactional(readOnly = true)
  public Board getBoard(Long boardId) {
    return boardRepository
        .findById(boardId)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "게시판을 찾을 수 없습니다."));
  }

  @Transactional
  public Board createBoard(BoardForm form) {
    String name = form.getName().strip();
    if (boardRepository.existsByNameIgnoreCase(name)) {
      throw new BusinessException(HttpStatus.CONFLICT, "이미 같은 이름의 게시판이 있습니다.");
    }
    Board board =
        new Board(
            name, form.getDescription().strip(), passwordHasher.hash(form.getPassword()), now());
    return boardRepository.save(board);
  }

  @Transactional
  public void updateBoard(Long boardId, BoardForm form) {
    Board board = getBoard(boardId);
    verifyPassword(form.getPassword(), board.getPasswordHash());
    String name = form.getName().strip();
    if (boardRepository.existsByNameIgnoreCaseAndIdNot(name, boardId)) {
      throw new BusinessException(HttpStatus.CONFLICT, "이미 같은 이름의 게시판이 있습니다.");
    }
    board.update(name, form.getDescription().strip());
  }

  @Transactional
  public void deleteBoard(Long boardId, String password) {
    Board board = getBoard(boardId);
    verifyPassword(password, board.getPasswordHash());
    List<Long> postIds = postRepository.findIdsByBoardId(boardId);
    if (!postIds.isEmpty()) commentRepository.deleteAllByPostIds(postIds);
    postRepository.deleteAllByBoardId(boardId);
    boardRepository.delete(board);
  }

  @Transactional(readOnly = true)
  public Page<Post> findPosts(Long boardId, int page, String keyword) {
    getBoard(boardId);
    String query = keyword == null ? "" : keyword.strip();
    return postRepository.search(
        boardId,
        query,
        PageRequest.of(
            Math.max(0, page), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt", "id")));
  }

  @Transactional
  public Post createPost(Long boardId, PostForm form) {
    getBoard(boardId);
    Post post =
        new Post(
            boardId,
            form.getTitle().strip(),
            form.getAuthor().strip(),
            form.getContent().strip(),
            passwordHasher.hash(form.getPassword()),
            now());
    return postRepository.save(post);
  }

  @Transactional
  public PostDetails readPost(Long boardId, Long postId) {
    Post post = getPost(boardId, postId);
    post.increaseViewCount();
    return details(getBoard(boardId), post);
  }

  @Transactional(readOnly = true)
  public PostDetails peekPost(Long boardId, Long postId) {
    return details(getBoard(boardId), getPost(boardId, postId));
  }

  @Transactional
  public void updatePost(Long boardId, Long postId, PostForm form) {
    Post post = getPost(boardId, postId);
    verifyPassword(form.getPassword(), post.getPasswordHash());
    post.update(
        form.getTitle().strip(), form.getAuthor().strip(), form.getContent().strip(), now());
  }

  @Transactional
  public void deletePost(Long boardId, Long postId, String password) {
    Post post = getPost(boardId, postId);
    verifyPassword(password, post.getPasswordHash());
    commentRepository.deleteAllByPostId(postId);
    postRepository.delete(post);
  }

  @Transactional
  public Comment createComment(Long boardId, Long postId, CommentForm form, String remoteAddress) {
    getPost(boardId, postId);
    ClientIdentity identity = clientIdentityService.from(remoteAddress);
    Comment comment =
        new Comment(
            postId,
            form.getContent().strip(),
            identity.alias(),
            identity.maskedIp(),
            identity.hash(),
            passwordHasher.hash(form.getPassword()),
            now());
    return commentRepository.save(comment);
  }

  @Transactional
  public void updateComment(Long boardId, Long postId, Long commentId, CommentForm form) {
    getPost(boardId, postId);
    Comment comment = getComment(postId, commentId);
    verifyPassword(form.getPassword(), comment.getPasswordHash());
    comment.update(form.getContent().strip(), now());
  }

  @Transactional
  public void deleteComment(Long boardId, Long postId, Long commentId, String password) {
    getPost(boardId, postId);
    Comment comment = getComment(postId, commentId);
    verifyPassword(password, comment.getPasswordHash());
    commentRepository.delete(comment);
  }

  private PostDetails details(Board board, Post post) {
    return new PostDetails(
        board, post, commentRepository.findByPostIdOrderByCreatedAtAsc(post.getId()));
  }

  private Post getPost(Long boardId, Long postId) {
    return postRepository
        .findByIdAndBoardId(postId, boardId)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
  }

  private Comment getComment(Long postId, Long commentId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));
    if (!Objects.equals(comment.getPostId(), postId)) {
      throw new BusinessException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다.");
    }
    return comment;
  }

  private void verifyPassword(String raw, String encoded) {
    if (raw == null || !passwordHasher.matches(raw, encoded)) {
      throw new BusinessException(HttpStatus.FORBIDDEN, "비밀번호가 일치하지 않습니다.");
    }
  }

  private LocalDateTime now() {
    return LocalDateTime.now(clock);
  }

  public record BoardOverview(Board board, long postCount) {}

  public record BoardPreview(Board board, long postCount, List<Post> posts) {}

  public record PostDetails(Board board, Post post, List<Comment> comments) {}
}
