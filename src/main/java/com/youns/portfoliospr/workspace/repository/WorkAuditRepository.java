package com.youns.portfoliospr.workspace.repository;

import com.youns.portfoliospr.workspace.domain.WorkAudit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkAuditRepository extends JpaRepository<WorkAudit, Long> {
  List<WorkAudit> findByProjectIdOrderByCreatedAtDescIdDesc(Long id);
}
