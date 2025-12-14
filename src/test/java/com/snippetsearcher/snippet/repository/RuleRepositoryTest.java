package com.snippetsearcher.snippet.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.snippetsearcher.snippet.model.RuleEntity;
import com.snippetsearcher.snippet.model.RuleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:snippets;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.username=sa",
      "spring.datasource.password=sa",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.jpa.properties.hibernate.hbm2ddl.auto=create-drop",
      "spring.flyway.enabled=false",
      "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
    })
class RuleRepositoryTest {

  @Autowired private RuleRepository repository;

  @Test
  void findByTypeOrdersByName() {
    repository.deleteAll();
    repository.save(new RuleEntity("b", "B-name", RuleType.FORMAT, true, null));
    repository.save(new RuleEntity("a", "A-name", RuleType.FORMAT, true, null));

    var results = repository.findByTypeOrderByNameAsc(RuleType.FORMAT);

    assertThat(results).hasSize(2);
    assertThat(results.getFirst().getName()).isEqualTo("A-name");
  }

  @Test
  void deleteByTypeRemovesOnlyMatching() {
    repository.save(new RuleEntity("lint", "Lint", RuleType.LINT, true, null));
    repository.save(new RuleEntity("fmt", "Fmt", RuleType.FORMAT, true, null));

    repository.deleteByType(RuleType.LINT);

    assertThat(repository.findAll()).hasSize(1);
    assertThat(repository.findAll().getFirst().getType()).isEqualTo(RuleType.FORMAT);
  }
}
