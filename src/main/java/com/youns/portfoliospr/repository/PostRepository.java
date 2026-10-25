package com.youns.portfoliospr.repository;

import com.youns.portfoliospr.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
            select p from Post p
            where p.boardId = :boardId
              and (:keyword = ''
                or lower(p.title) like lower(concat('%', :keyword, '%'))
                or lower(p.author) like lower(concat('%', :keyword, '%')))
            """)
    Page<Post> search(@Param("boardId") Long boardId, @Param("keyword") String keyword, Pageable pageable);

    Optional<Post> findByIdAndBoardId(Long id, Long boardId);
    List<Post> findTop5ByBoardIdOrderByCreatedAtDescIdDesc(Long boardId);
    long countByBoardId(Long boardId);

    @Query("select p.id from Post p where p.boardId = :boardId")
    List<Long> findIdsByBoardId(@Param("boardId") Long boardId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Post p where p.boardId = :boardId")
    void deleteAllByBoardId(@Param("boardId") Long boardId);
}
