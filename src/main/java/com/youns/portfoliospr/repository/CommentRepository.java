package com.youns.portfoliospr.repository;

import com.youns.portfoliospr.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Comment c where c.postId = :postId")
    void deleteAllByPostId(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Comment c where c.postId in :postIds")
    void deleteAllByPostIds(@Param("postIds") Collection<Long> postIds);
}
