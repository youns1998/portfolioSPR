package com.youns.portfoliospr.web;

import com.youns.portfoliospr.domain.Board;
import com.youns.portfoliospr.domain.Post;
import com.youns.portfoliospr.service.CommunityException;
import com.youns.portfoliospr.service.CommunityService;
import com.youns.portfoliospr.service.CommunityService.PostDetails;
import com.youns.portfoliospr.web.form.BoardForm;
import com.youns.portfoliospr.web.form.CommentForm;
import com.youns.portfoliospr.web.form.PostForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @ModelAttribute("navigationBoards")
    public List<CommunityService.BoardOverview> navigationBoards() {
        return communityService.findBoards();
    }

    @GetMapping("/community")
    public String home(Model model) {
        model.addAttribute("boards", communityService.findBoardPreviews());
        return "home";
    }

    /* 기본 게시판 4개만 사용하므로 게시판 생성 기능은 임시 비활성화합니다.
    @GetMapping("/boards/new")
    public String newBoard(Model model) {
        model.addAttribute("boardForm", new BoardForm());
        model.addAttribute("editing", false);
        return "board-form";
    }

    @PostMapping("/boards")
    public String createBoard(@Valid @ModelAttribute("boardForm") BoardForm form,
                              BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editing", false);
            return "board-form";
        }
        Board board = communityService.createBoard(form);
        redirectAttributes.addFlashAttribute("notice", "새 게시판을 만들었습니다.");
        return "redirect:/boards/" + board.getId();
    }
    */

    @RequestMapping(value = {"/boards/new", "/boards"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String boardCreationDisabled() {
        throw new CommunityException(HttpStatus.NOT_FOUND, "현재 새 게시판 만들기는 사용할 수 없습니다.");
    }

    @GetMapping("/boards/{boardId}")
    public String board(@PathVariable Long boardId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "") String keyword,
                        Model model) {
        model.addAttribute("board", communityService.getBoard(boardId));
        model.addAttribute("posts", communityService.findPosts(boardId, page, keyword));
        model.addAttribute("keyword", keyword == null ? "" : keyword.strip());
        return "board-detail";
    }

    @GetMapping("/boards/{boardId}/edit")
    public String editBoard(@PathVariable Long boardId, Model model) {
        Board board = communityService.getBoard(boardId);
        BoardForm form = new BoardForm();
        form.setName(board.getName());
        form.setDescription(board.getDescription());
        model.addAttribute("board", board);
        model.addAttribute("boardForm", form);
        model.addAttribute("editing", true);
        return "board-form";
    }

    @PostMapping("/boards/{boardId}/edit")
    public String updateBoard(@PathVariable Long boardId,
                              @Valid @ModelAttribute("boardForm") BoardForm form,
                              BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("board", communityService.getBoard(boardId));
            model.addAttribute("editing", true);
            return "board-form";
        }
        communityService.updateBoard(boardId, form);
        redirectAttributes.addFlashAttribute("notice", "게시판 정보를 수정했습니다.");
        return "redirect:/boards/" + boardId;
    }

    @PostMapping("/boards/{boardId}/delete")
    public String deleteBoard(@PathVariable Long boardId, @RequestParam String password,
                              RedirectAttributes redirectAttributes) {
        communityService.deleteBoard(boardId, password);
        redirectAttributes.addFlashAttribute("notice", "게시판과 모든 게시글을 삭제했습니다.");
        return "redirect:/community";
    }

    @GetMapping("/boards/{boardId}/posts/new")
    public String newPost(@PathVariable Long boardId, Model model) {
        model.addAttribute("board", communityService.getBoard(boardId));
        model.addAttribute("postForm", new PostForm());
        model.addAttribute("editing", false);
        return "post-form";
    }

    @PostMapping("/boards/{boardId}/posts")
    public String createPost(@PathVariable Long boardId,
                             @Valid @ModelAttribute("postForm") PostForm form,
                             BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("board", communityService.getBoard(boardId));
            model.addAttribute("editing", false);
            return "post-form";
        }
        Post post = communityService.createPost(boardId, form);
        redirectAttributes.addFlashAttribute("notice", "게시글을 등록했습니다.");
        return postUrl(boardId, post.getId());
    }

    @GetMapping("/boards/{boardId}/posts/{postId}")
    public String post(@PathVariable Long boardId, @PathVariable Long postId, Model model) {
        addPostDetails(model, communityService.readPost(boardId, postId));
        model.addAttribute("commentForm", new CommentForm());
        return "post-detail";
    }

    @GetMapping("/boards/{boardId}/posts/{postId}/edit")
    public String editPost(@PathVariable Long boardId, @PathVariable Long postId, Model model) {
        PostDetails details = communityService.peekPost(boardId, postId);
        PostForm form = new PostForm();
        form.setTitle(details.post().getTitle());
        form.setAuthor(details.post().getAuthor());
        form.setContent(details.post().getContent());
        model.addAttribute("board", details.board());
        model.addAttribute("post", details.post());
        model.addAttribute("postForm", form);
        model.addAttribute("editing", true);
        return "post-form";
    }

    @PostMapping("/boards/{boardId}/posts/{postId}/edit")
    public String updatePost(@PathVariable Long boardId, @PathVariable Long postId,
                             @Valid @ModelAttribute("postForm") PostForm form,
                             BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            PostDetails details = communityService.peekPost(boardId, postId);
            model.addAttribute("board", details.board());
            model.addAttribute("post", details.post());
            model.addAttribute("editing", true);
            return "post-form";
        }
        communityService.updatePost(boardId, postId, form);
        redirectAttributes.addFlashAttribute("notice", "게시글을 수정했습니다.");
        return postUrl(boardId, postId);
    }

    @PostMapping("/boards/{boardId}/posts/{postId}/delete")
    public String deletePost(@PathVariable Long boardId, @PathVariable Long postId,
                             @RequestParam String password, RedirectAttributes redirectAttributes) {
        communityService.deletePost(boardId, postId, password);
        redirectAttributes.addFlashAttribute("notice", "게시글을 삭제했습니다.");
        return "redirect:/boards/" + boardId;
    }

    @PostMapping("/boards/{boardId}/posts/{postId}/comments")
    public String createComment(@PathVariable Long boardId, @PathVariable Long postId,
                                @Valid @ModelAttribute("commentForm") CommentForm form,
                                BindingResult bindingResult, Model model, HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addPostDetails(model, communityService.peekPost(boardId, postId));
            return "post-detail";
        }
        communityService.createComment(boardId, postId, form, request.getRemoteAddr());
        redirectAttributes.addFlashAttribute("notice", "댓글을 등록했습니다.");
        return postUrl(boardId, postId) + "#comments";
    }

    @PostMapping("/boards/{boardId}/posts/{postId}/comments/{commentId}/edit")
    public String updateComment(@PathVariable Long boardId, @PathVariable Long postId,
                                @PathVariable Long commentId, @RequestParam String content,
                                @RequestParam String password, RedirectAttributes redirectAttributes) {
        CommentForm form = checkedComment(content, password);
        communityService.updateComment(boardId, postId, commentId, form);
        redirectAttributes.addFlashAttribute("notice", "댓글을 수정했습니다.");
        return postUrl(boardId, postId) + "#comments";
    }

    @PostMapping("/boards/{boardId}/posts/{postId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long boardId, @PathVariable Long postId,
                                @PathVariable Long commentId, @RequestParam String password,
                                RedirectAttributes redirectAttributes) {
        communityService.deleteComment(boardId, postId, commentId, password);
        redirectAttributes.addFlashAttribute("notice", "댓글을 삭제했습니다.");
        return postUrl(boardId, postId) + "#comments";
    }

    private CommentForm checkedComment(String content, String password) {
        if (content == null || content.isBlank() || content.length() > 1_000) {
            throw new CommunityException(HttpStatus.BAD_REQUEST, "댓글은 1~1,000자로 입력해 주세요.");
        }
        if (password == null || password.isBlank() || password.length() < 4 || password.length() > 64) {
            throw new CommunityException(HttpStatus.BAD_REQUEST, "비밀번호는 4~64자로 입력해 주세요.");
        }
        CommentForm form = new CommentForm();
        form.setContent(content);
        form.setPassword(password);
        return form;
    }

    private void addPostDetails(Model model, PostDetails details) {
        model.addAttribute("board", details.board());
        model.addAttribute("post", details.post());
        model.addAttribute("comments", details.comments());
    }

    private String postUrl(Long boardId, Long postId) {
        return "redirect:/boards/" + boardId + "/posts/" + postId;
    }
}
