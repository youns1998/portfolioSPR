package com.youns.portfoliospr.workspace;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkAuditRepository extends JpaRepository<WorkAudit, Long> {
  List<WorkAudit> findByProjectIdOrderByCreatedAtDescIdDesc(Long id);
}
