package com.youns.portfoliospr.community.repository;

import com.youns.portfoliospr.community.domain.Board;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
  List<Board> findAllByOrderByCreatedAtAsc();

  boolean existsByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
