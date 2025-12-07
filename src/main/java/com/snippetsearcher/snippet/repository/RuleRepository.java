package com.snippetsearcher.snippet.repository;

import com.snippetsearcher.snippet.model.RuleEntity;
import com.snippetsearcher.snippet.model.RuleType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleRepository extends JpaRepository<RuleEntity, String> {

  List<RuleEntity> findByTypeOrderByNameAsc(RuleType type);

  void deleteByType(RuleType type);
}
