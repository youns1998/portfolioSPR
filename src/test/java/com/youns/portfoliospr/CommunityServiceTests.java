package com.youns.portfoliospr;

import com.youns.portfoliospr.domain.Board;
import com.youns.portfoliospr.domain.Comment;
import com.youns.portfoliospr.domain.Post;
import com.youns.portfoliospr.service.CommunityException;
import com.youns.portfoliospr.service.CommunityService;
import com.youns.portfoliospr.service.PasswordHasher;
import com.youns.portfoliospr.web.form.BoardForm;
import com.youns.portfoliospr.web.form.CommentForm;
import com.youns.portfoliospr.web.form.PostForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class CommunityServiceTests {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private PasswordHasher passwordHasher;

    @Test
    void anonymousBoardPostAndPasswordCommentFlowWorks() {
        BoardForm boardForm = new BoardForm();
        boardForm.setName("테스트 게시판");
        boardForm.setDescription("통합 테스트용 게시판");
        boardForm.setPassword("board-pass");
        Board board = communityService.createBoard(boardForm);

        PostForm postForm = new PostForm();
        postForm.setTitle("첫 게시글");
        postForm.setAuthor("익명 작성자");
        postForm.setContent("본문입니다.");
        postForm.setPassword("post-pass");
        Post post = communityService.createPost(board.getId(), postForm);

        CommentForm commentForm = new CommentForm();
        commentForm.setContent("비밀번호 기반 댓글입니다.");
        commentForm.setPassword("comment-pass");
        Comment comment = communityService.createComment(board.getId(), post.getId(), commentForm, "192.168.0.14");

        assertEquals("192.168.0.*", comment.getMaskedIp());
        assertTrue(comment.getAuthorAlias().startsWith("익명-"));
        assertFalse(comment.getPasswordHash().equals("comment-pass"));
        assertTrue(passwordHasher.matches("comment-pass", comment.getPasswordHash()));
        assertEquals(1, communityService.peekPost(board.getId(), post.getId()).comments().size());

        CommentForm changed = new CommentForm();
        changed.setContent("수정된 댓글입니다.");
        changed.setPassword("wrong-pass");
        assertThrows(CommunityException.class,
                () -> communityService.updateComment(board.getId(), post.getId(), comment.getId(), changed));
        assertEquals("비밀번호 기반 댓글입니다.", communityService.peekPost(board.getId(), post.getId()).comments().getFirst().getContent());
        changed.setPassword("comment-pass");
        communityService.updateComment(board.getId(), post.getId(), comment.getId(), changed);
        assertEquals("수정된 댓글입니다.", communityService.peekPost(board.getId(), post.getId()).comments().getFirst().getContent());

        assertThrows(CommunityException.class,
                () -> communityService.deleteComment(board.getId(), post.getId(), comment.getId(), "wrong-pass"));
        assertEquals(1, communityService.peekPost(board.getId(), post.getId()).comments().size());
        communityService.deleteComment(board.getId(), post.getId(), comment.getId(), "comment-pass");
        assertTrue(communityService.peekPost(board.getId(), post.getId()).comments().isEmpty());
    }

    @Test
    void postUpdateRequiresItsPassword() {
        BoardForm boardForm = new BoardForm();
        boardForm.setName("비밀번호 테스트");
        boardForm.setDescription("수정 권한 테스트");
        boardForm.setPassword("board-pass");
        Board board = communityService.createBoard(boardForm);

        PostForm postForm = new PostForm();
        postForm.setTitle("원래 제목");
        postForm.setAuthor("작성자");
        postForm.setContent("원래 본문");
        postForm.setPassword("correct-pass");
        Post post = communityService.createPost(board.getId(), postForm);

        postForm.setTitle("바뀐 제목");
        postForm.setPassword("wrong-pass");
        assertThrows(CommunityException.class,
                () -> communityService.updatePost(board.getId(), post.getId(), postForm));
    }

    @Test
    void updateAndCascadeDeleteFlowWorks() {
        BoardForm boardForm = new BoardForm();
        boardForm.setName("삭제 테스트");
        boardForm.setDescription("삭제 전 설명");
        boardForm.setPassword("board-pass");
        Board board = communityService.createBoard(boardForm);

        boardForm.setName("수정된 게시판");
        boardForm.setDescription("수정된 설명");
        communityService.updateBoard(board.getId(), boardForm);
        assertEquals("수정된 게시판", communityService.getBoard(board.getId()).getName());

        PostForm postForm = new PostForm();
        postForm.setTitle("삭제할 글");
        postForm.setAuthor("작성자");
        postForm.setContent("삭제할 본문");
        postForm.setPassword("post-pass");
        Post post = communityService.createPost(board.getId(), postForm);

        postForm.setTitle("수정된 글");
        communityService.updatePost(board.getId(), post.getId(), postForm);
        assertEquals("수정된 글", communityService.peekPost(board.getId(), post.getId()).post().getTitle());

        CommentForm commentForm = new CommentForm();
        commentForm.setContent("삭제할 댓글");
        commentForm.setPassword("comment-pass");
        Comment comment = communityService.createComment(board.getId(), post.getId(), commentForm, "10.0.0.3");
        communityService.deleteComment(board.getId(), post.getId(), comment.getId(), "comment-pass");
        assertTrue(communityService.peekPost(board.getId(), post.getId()).comments().isEmpty());

        communityService.createComment(board.getId(), post.getId(), commentForm, "10.0.0.3");
        communityService.deletePost(board.getId(), post.getId(), "post-pass");
        assertTrue(communityService.findPosts(board.getId(), 0, "").isEmpty());

        Post secondPost = communityService.createPost(board.getId(), postForm);
        communityService.createComment(board.getId(), secondPost.getId(), commentForm, "10.0.0.3");
        communityService.deleteBoard(board.getId(), "board-pass");
        assertTrue(communityService.findBoards().stream()
                .noneMatch(item -> item.board().getId().equals(board.getId())));
    }
}
