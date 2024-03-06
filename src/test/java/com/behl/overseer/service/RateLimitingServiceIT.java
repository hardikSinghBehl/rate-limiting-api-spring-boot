package com.behl.overseer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import com.behl.overseer.InitializeApplicationSecretKey;
import com.behl.overseer.InitializeMysqlContainer;
import com.behl.overseer.InitializeRedisContainer;
import com.behl.overseer.entity.Plan;
import com.behl.overseer.entity.User;
import com.behl.overseer.entity.UserPlanMapping;
import com.behl.overseer.repository.PlanRepository;
import com.behl.overseer.repository.UserPlanMappingRepository;
import com.behl.overseer.repository.UserRepository;

import io.github.bucket4j.Bucket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bytebuddy.utility.RandomString;

@DirtiesContext
@SpringBootTest
@InitializeRedisContainer
@InitializeMysqlContainer
@InitializeApplicationSecretKey
class RateLimitingServiceIT {

	@Autowired
	private RateLimitingService rateLimitingService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PlanRepository planRepository;

	@SpyBean
	private UserPlanMappingRepository userPlanMappingRepository;

	@Test
	void getBucketOrCreateNewIfNotFoundInCache() {
		// prepare test data in datasource
		final var testData = new TestData().createTestData();
		final var user = testData.getUser();
		final var plan = testData.getPlan();

		// invoke method under test
		Bucket bucket;
		bucket = rateLimitingService.getBucket(user.getId());

		// assert bucket configuration are equal to user plan
		assertThat(bucket.getAvailableTokens()).isEqualTo(Long.valueOf(plan.getLimitPerHour()));

		// verify interaction with datasource to fetch user's active plan
		// confirming the creation of bucket configuration from scratch
		// on initial invocation
		verify(userPlanMappingRepository).getActivePlan(user.getId());
		Mockito.clearInvocations(userPlanMappingRepository);

		// consume tokens from bucket
		final var tokensToConsume = 5;
		bucket.tryConsume(tokensToConsume);

		// invoke method under test again
		bucket = rateLimitingService.getBucket(user.getId());

		// assert available tokens with bucket
		assertThat(bucket.getAvailableTokens()).isEqualTo(plan.getLimitPerHour() - tokensToConsume);

		// assert no interaction with datasource to fetch user's active plan on second
		// invocation
		verify(userPlanMappingRepository, times(0)).getActivePlan(user.getId());
	}

	@Test
	void shouldClearRateLimitConfigurationInCache() {
		// prepare test data in datasource
		final var testData = new TestData().createTestData();
		final var user = testData.getUser();
		final var plan = testData.getPlan();

		// create bucket for user
		Bucket bucket;
		bucket = rateLimitingService.getBucket(user.getId());

		// assert bucket configuration are equal to user plan
		assertThat(bucket.getAvailableTokens()).isEqualTo(Long.valueOf(plan.getLimitPerHour()));

		// consume tokens from bucket and assert available tokens are less than plan
		// configuration
		final var tokensToConsume = 5;
		bucket.tryConsume(tokensToConsume);
		assertThat(bucket.getAvailableTokens()).isLessThan(Long.valueOf(plan.getLimitPerHour()));

		// invoke method under test
		rateLimitingService.reset(user.getId());

		// retrieve bucket configuration for user again
		bucket = rateLimitingService.getBucket(user.getId());

		// assert bucket's available token are equal to original plan configuration
		assertThat(bucket.getAvailableTokens()).isEqualTo(Long.valueOf(plan.getLimitPerHour()));
	}

	@Getter
	@Setter
	@RequiredArgsConstructor
	class TestData {

		private User user;
		private Plan plan;

		public TestData createTestData() {
			// insert test user record in datasource
			final String emailId = RandomString.make();
			final String password = RandomString.make();
			final User user = new User();
			user.setEmailId(emailId);
			user.setPassword(password);
			final User savedUser = userRepository.save(user);

			// fetch a plan record from datasource
			final Plan plan = planRepository.findAll().get(0);

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