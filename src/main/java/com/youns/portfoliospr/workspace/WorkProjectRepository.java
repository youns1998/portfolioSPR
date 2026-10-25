package com.youns.portfoliospr.workspace;

import org.springframework.data.jpa.repository.*;

public interface WorkProjectRepository
    extends JpaRepository<WorkProject, Long>, JpaSpecificationExecutor<WorkProject> {
  boolean existsByCode(String code);

  boolean existsByCodeAndIdNot(String code, Long id);
}
