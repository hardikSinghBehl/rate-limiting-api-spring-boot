package com.behl.overseer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.behl.overseer.InitializeMysqlContainer;

@DataJpaTest
@InitializeMysqlContainer
class PlanRepositoryIT {

	@Autowired
	private PlanRepository planRepository;

	/**
	 * @see src/main/resources/db/migration/V002__adding_plans.sql
	 */
	@Test
	void evaluateRunOfFlywayMigrationScript() {
		// fetch all plan records from datasource
		final var plans = planRepository.findAll();

		// assert fetched record's attributes
		assertThat(plans).hasSize(3);
		plans.forEach(plan -> {
			assertThat(plan.getId()).isNotNull();
			assertThat(plan.getName()).isNotNull();
			assertThat(plan.getLimitPerHour()).isNotNull();
			assertThat(plan.getCreatedAt()).isNotNull();
			assertThat(plan.getUpdatedAt()).isNotNull();
		});
	}

}