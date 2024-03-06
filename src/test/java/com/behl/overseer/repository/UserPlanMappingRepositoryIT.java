package com.behl.overseer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.behl.overseer.InitializeMysqlContainer;
import com.behl.overseer.entity.User;
import com.behl.overseer.entity.UserPlanMapping;

import net.bytebuddy.utility.RandomString;

@DataJpaTest
@InitializeMysqlContainer
class UserPlanMappingRepositoryIT {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PlanRepository planRepository;

	@Autowired
	private UserPlanMappingRepository userPlanMappingRepository;

	@Test
	void shouldDeactivateCurrentPlanOfUser() {
		// insert test user record in datasource
		final var emailId = RandomString.make();
		final var password = RandomString.make();
		final var user = new User();
		user.setEmailId(emailId);
		user.setPassword(password);
		final var savedUser = userRepository.save(user);

		// fetch a plan record from datasource
		final var plan = planRepository.findAll().get(0);

		// insert an active user plan mapping record
		final var userPlanMapping = new UserPlanMapping();
		userPlanMapping.setUserId(savedUser.getId());
		userPlanMapping.setPlanId(plan.getId());
		final var savedUserPlanMapping = userPlanMappingRepository.save(userPlanMapping);

		// assert saved user plan mapping is active
		assertThat(savedUserPlanMapping.getIsActive()).isTrue();

		// invoke method under test
		userPlanMappingRepository.deactivateCurrentPlan(savedUser.getId());

		// fetch updated record and assert active status
		final var fetchedUserPlanMapping = userPlanMappingRepository.findById(savedUserPlanMapping.getId());
		assertThat(fetchedUserPlanMapping).isPresent().get().satisfies(userPlan -> {
			assertThat(userPlan.getIsActive()).isFalse();
		});
	}

	@Test
	void shouldGetActivePlanForUser() {
		// insert test user record in datasource
		final var emailId = RandomString.make();
		final var password = RandomString.make();
		final var user = new User();
		user.setEmailId(emailId);
		user.setPassword(password);
		final var savedUser = userRepository.save(user);

		// fetch a plan record from datasource
		final var plan = planRepository.findAll().get(0);

		// insert an active user plan mapping record
		final var userPlanMapping = new UserPlanMapping();
		userPlanMapping.setUserId(savedUser.getId());
		userPlanMapping.setPlanId(plan.getId());
		final var savedUserPlanMapping = userPlanMappingRepository.save(userPlanMapping);

		// invoke method under test
		final var fetchedUserPlanMapping = userPlanMappingRepository.getActivePlan(savedUser.getId());

		// assert the fetched record is user's active plan mapping
		assertThat(fetchedUserPlanMapping.getIsActive()).isTrue();
		assertThat(fetchedUserPlanMapping.getId()).isEqualTo(savedUserPlanMapping.getId());
		assertThat(fetchedUserPlanMapping.getUserId()).isEqualTo(savedUser.getId());
		assertThat(fetchedUserPlanMapping.getPlanId()).isEqualTo(plan.getId());
	}

	@Test
	void shouldEvaluateActiveUserPlanByPlanId() {
		// insert test user record in datasource
		final var emailId = RandomString.make();
		final var password = RandomString.make();
		final var user = new User();
		user.setEmailId(emailId);
		user.setPassword(password);
		final var savedUser = userRepository.save(user);

		// fetch plan records from datasource
		final var plans = planRepository.findAll();
		final var userPlan = plans.get(0);
		final var nonUserPlan = plans.get(1);

		// insert an active user plan mapping record
		final var userPlanMapping = new UserPlanMapping();
		userPlanMapping.setUserId(savedUser.getId());
		userPlanMapping.setPlanId(userPlan.getId());
		userPlanMappingRepository.save(userPlanMapping);

		// invoke method under test for valid plan-id and assert response
		Boolean response;
		response = userPlanMappingRepository.isActivePlan(savedUser.getId(), userPlan.getId());
		assertThat(response).isTrue();

		// invoke method under test for valid plan-id and assert response
		response = userPlanMappingRepository.isActivePlan(savedUser.getId(), nonUserPlan.getId());
		assertThat(response).isFalse();
	}

}