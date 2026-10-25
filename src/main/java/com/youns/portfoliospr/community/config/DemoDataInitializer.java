package com.youns.portfoliospr.community.config;

import com.youns.portfoliospr.community.domain.Board;
import com.youns.portfoliospr.community.domain.Post;
import com.youns.portfoliospr.community.repository.BoardRepository;
import com.youns.portfoliospr.community.repository.PostRepository;
import com.youns.portfoliospr.community.service.PasswordHasher;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoDataInitializer implements ApplicationRunner {

  private static final int POSTS_PER_BOARD = 45;
  private final BoardRepository boardRepository;
  private final PostRepository postRepository;
  private final PasswordHasher passwordHasher;
  private final Clock clock;

  public DemoDataInitializer(
      BoardRepository boardRepository,
      PostRepository postRepository,
      PasswordHasher passwordHasher,
      Clock clock) {
    this.boardRepository = boardRepository;
    this.postRepository = postRepository;
    this.passwordHasher = passwordHasher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    // 이미 데이터가 있으면 사용자가 작성한 내용을 보존하고 중복 등록하지 않습니다.
    if (boardRepository.count() > 0) {
      return;
    }

    String boardPassword = passwordHasher.hash("board-demo-1234");
    String postPassword = passwordHasher.hash("post-demo-1234");
    LocalDateTime now = LocalDateTime.now(clock);
    List<DemoBoard> boards =
        List.of(
            new DemoBoard(
                "인기글",
                "함께 보고 싶은 이야기와 화제의 글을 나누는 공간입니다.",
                List.of(
                    "요즘 가장 즐겁게 하고 있는 취미가 있나요?",
                    "주말을 알차게 보내는 나만의 방법",
                    "직접 써 보고 만족한 생활 아이템",
                    "퇴근 후 한 시간, 여러분은 어떻게 보내세요?",
                    "다시 가고 싶은 동네 산책 코스",
                    "작은 습관 하나로 달라진 일상",
                    "최근에 발견한 숨은 맛집 이야기",
                    "이번 달에 도전해 보고 싶은 일",
                    "오늘 있었던 소소하지만 기분 좋은 일")),
            new DemoBoard(
                "게임",
                "좋아하는 게임, 공략과 플레이 후기를 자유롭게 나눠요.",
                List.of(
                    "친구들과 가볍게 즐길 협동 게임 추천",
                    "처음 시작하는 분들을 위한 초반 팁",
                    "이번 주말에 플레이한 게임 후기",
                    "여러분의 인생 게임은 무엇인가요?",
                    "스토리가 좋은 게임을 찾고 있어요",
                    "키보드와 패드 중 어떤 쪽이 편한가요?",
                    "오랜만에 다시 해도 재미있는 고전 게임",
                    "혼자 차분하게 즐기는 퍼즐 게임 이야기",
                    "오늘 드디어 어려운 스테이지를 깼습니다")),
            new DemoBoard(
                "음악",
                "오늘의 플레이리스트부터 공연, 악기 연습까지 함께 이야기해요.",
                List.of(
                    "출근길에 듣기 좋은 플레이리스트",
                    "집중할 때 자주 듣는 연주곡 모음",
                    "기타를 처음 배울 때 도움이 된 연습법",
                    "최근 다녀온 작은 공연 후기",
                    "비 오는 날 생각나는 노래가 있나요?",
                    "오랜만에 꺼내 들은 추억의 앨범",
                    "운동할 때 힘이 나는 음악 추천",
                    "피아노 연습을 꾸준히 하는 방법",
                    "좋아하는 영화 음악을 공유해 주세요")),
            new DemoBoard(
                "운동",
                "러닝, 헬스, 등산 등 몸을 움직이는 즐거움을 공유해요.",
                List.of(
                    "첫 5km 러닝을 완주했습니다",
                    "운동을 꾸준히 이어 가는 나만의 방법",
                    "주말에 다녀오기 좋은 가벼운 등산 코스",
                    "책상 앞에서 잠깐 하는 스트레칭",
                    "헬스를 처음 시작한 한 달 후기",
                    "아침 운동과 저녁 운동, 언제 하시나요?",
                    "집에서 즐기는 짧은 맨몸 운동 루틴",
                    "자전거 타기 좋은 강변 코스 후기",
                    "오늘의 운동 기록을 함께 나눠요")));

    for (int boardIndex = 0; boardIndex < boards.size(); boardIndex++) {
      DemoBoard demo = boards.get(boardIndex);
      Board board =
          boardRepository.save(
              new Board(
                  demo.name(),
                  demo.description(),
                  boardPassword,
                  now.minusDays(7).plusSeconds(boardIndex)));
      List<Post> posts = new ArrayList<>();
      for (int index = 0; index < POSTS_PER_BOARD; index++) {
        String title = demo.titles().get(index % demo.titles().size()) + " · 이야기 " + (index + 1);
        String content =
            title
                + "\n\n"
                + demo.name()
                + " 게시판에서 함께 이야기 나누고 싶어 글을 남깁니다.\n"
                + "요즘 틈틈이 시간을 내어 즐기고 있는데, 작은 경험을 공유하는 것만으로도 재미있네요.\n"
                + "여러분의 경험이나 추천도 댓글로 알려 주세요!";
        posts.add(
            new Post(
                board.getId(),
                title,
                "취미생활" + (index % 12 + 1),
                content,
                postPassword,
                now.minusHours(POSTS_PER_BOARD - index)));
      }
      postRepository.saveAll(posts);
    }
  }

  private record DemoBoard(String name, String description, List<String> titles) {}
}
