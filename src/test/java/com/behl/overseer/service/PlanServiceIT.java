package com.behl.overseer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.behl.overseer.InitializeApplicationSecretKey;
import com.behl.overseer.InitializeMysqlContainer;
import com.behl.overseer.InitializeRedisContainer;
import com.behl.overseer.dto.PlanUpdationRequestDto;
import com.behl.overseer.entity.Plan;
import com.behl.overseer.entity.User;
import com.behl.overseer.entity.UserPlanMapping;
import com.behl.overseer.exception.InvalidPlanException;
import com.behl.overseer.repository.PlanRepository;
import com.behl.overseer.repository.UserPlanMappingRepository;
import com.behl.overseer.repository.UserRepository;
import com.behl.overseer.utility.AuthenticatedUserIdProvider;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bytebuddy.utility.RandomString;

@SpringBootTest
@InitializeRedisContainer
@InitializeMysqlContainer
@InitializeApplicationSecretKey
class PlanServiceIT {

	@Autowired
	private PlanService planService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PlanRepository planRepository;

	@Autowired
	private UserPlanMappingRepository userPlanMappingRepository;

	@MockBean
	private AuthenticatedUserIdProvider authenticatedUserIdProvider;

	@Test
	void planUpdationshouldThrowExceptionForInvalidPlanId() {
		// prepare plan updation request with invalid plan-id
		final var planId = UUID.randomUUID();
		final var planUpdationRequest = mock(PlanUpdationRequestDto.class);
		when(planUpdationRequest.getPlanId()).thenReturn(planId);

		// invoke method under test and assert exception
		final var exception = assertThrows(InvalidPlanException.class, () -> planService.update(planUpdationRequest));
		assertThat(exception.getReason()).isEqualTo("No plan exists in the system with provided-id");
	}

	@Test
	void datasourceShouldNotBeUpdatedForExistingPlanId() {
		// fetch plan records from datasource
		final var plans = planRepository.findAll();
		final var userPlan = plans.get(0);

		// populate datasource with test data
		final var testData = new TestData().createTestData(userPlan);
		final var user = testData.getUser();

		// prepare plan updation request with existing plan-id
		final var planId = userPlan.getId();
		final var planUpdationRequest = mock(PlanUpdationRequestDto.class);
		when(planUpdationRequest.getPlanId()).thenReturn(planId);

		// configure authenticated user
		final var userId = user.getId();
		when(authenticatedUserIdProvider.getUserId()).thenReturn(userId);

		// invoke method under test
		planService.update(planUpdationRequest);

		// assert plan activation status in datasource
		final var isActiveWithSamePlan = userPlanMappingRepository.isActivePlan(user.getId(), userPlan.getId());
		assertThat(isActiveWithSamePlan).isTrue();
	}

	@Test
	void shouldUpdateUserPlanForValidRequestAndDeactivatePreviousPlan() {
		// fetch plan records from datasource
		final var plans = planRepository.findAll();
		final var userPlan = plans.get(0);
		final var planToUpdate = plans.get(1);

		// populate datasource with test data
		final var testData = new TestData().createTestData(userPlan);
		final var user = testData.getUser();

		// prepare plan updation request with valid plan-id
		final var planId = planToUpdate.getId();
		final var planUpdationRequest = mock(PlanUpdationRequestDto.class);
		when(planUpdationRequest.getPlanId()).thenReturn(planId);

		// configure authenticated user
		final var userId = user.getId();
		when(authenticatedUserIdProvider.getUserId()).thenReturn(userId);

		// invoke method under test
		planService.update(planUpdationRequest);

		// assert plan activation status in datasource
		final var isActiveWithPreviousPlan = userPlanMappingRepository.isActivePlan(user.getId(), userPlan.getId());
		final var isActiveWithNewPlan = userPlanMappingRepository.isActivePlan(user.getId(), planToUpdate.getId());
		assertThat(isActiveWithPreviousPlan).isFalse();
		assertThat(isActiveWithNewPlan).isTrue();
	}

	/**
	 * @see src/main/resources/db/migration/V002__adding_plans.sql
	 */
	@Test
	void shouldRetrievePlansFromDatasource() {
		// invoke method under test
		final var plans = planService.retrieve();

		// assert fetched record's attributes
		assertThat(plans).hasSize(3);
		plans.forEach(plan -> {
			assertThat(plan.getId()).isNotNull();
			assertThat(plan.getName()).isNotNull();
			assertThat(plan.getLimitPerHour()).isNotNull();
		});
	}

	@Getter
	@Setter
	@RequiredArgsConstructor
	class TestData {

		private User user;
		private Plan plan;

		public TestData createTestData(@NonNull Plan plan) {
			// insert test user record in datasource
			final String emailId = RandomString.make();
			final String password = RandomString.make();
			final User user = new User();
			user.setEmailId(emailId);
			user.setPassword(password);
			final User savedUser = userRepository.save(user);

			// insert an active user plan mapping record
			final UserPlanMapping userPlanMapping = new UserPlanMapping();
			userPlanMapping.setUserId(savedUser.getId());
			userPlanMapping.setPlanId(plan.getId());
			userPlanMappingRepository.save(userPlanMapping);

			final var testData = new TestData();
			testData.setUser(savedUser);
			testData.setPlan(plan);
			return testData;
		}

	}

}
