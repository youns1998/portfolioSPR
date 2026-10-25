package com.youns.portfoliospr.workspace.repository;

import com.youns.portfoliospr.workspace.domain.WorkProject;
import org.springframework.data.jpa.repository.*;

public interface WorkProjectRepository
    extends JpaRepository<WorkProject, Long>, JpaSpecificationExecutor<WorkProject> {
  boolean existsByCode(String code);

  boolean existsByCodeAndIdNot(String code, Long id);
}
