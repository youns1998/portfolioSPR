package com.youns.portfoliospr.workspace;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {
  List<WorkItem> findByProject_IdOrderByDueDateAscIdAsc(Long id);

  boolean existsByProject_Id(Long id);

  long countByProject_IdAndStatusNot(Long id, ItemStatus status);
}
