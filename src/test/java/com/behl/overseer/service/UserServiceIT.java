package com.behl.overseer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.behl.overseer.InitializeApplicationSecretKey;
import com.behl.overseer.InitializeMysqlContainer;
import com.behl.overseer.InitializeRedisContainer;
import com.behl.overseer.dto.UserCreationRequestDto;
import com.behl.overseer.dto.UserLoginRequestDto;
import com.behl.overseer.entity.User;
import com.behl.overseer.exception.AccountAlreadyExistsException;
import com.behl.overseer.exception.InvalidLoginCredentialsException;
import com.behl.overseer.exception.InvalidPlanException;
import com.behl.overseer.repository.PlanRepository;
import com.behl.overseer.repository.UserRepository;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bytebuddy.utility.RandomString;

@SpringBootTest
@InitializeRedisContainer
@InitializeMysqlContainer
@InitializeApplicationSecretKey
class UserServiceIT {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PlanRepository planRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private static final String JWT_STRUCTURE_REGEX = "^[^.]+\\.[^.]+\\.[^.]+$";

	@Test
	void userCreationShouldThrowExceptionForDuplicateEmailId() {
		// insert a user in datasource
		final var emailId = RandomString.make() + "@domain.ut";
		final var password = RandomString.make();
		final var user = new User();
		user.setEmailId(emailId);
		user.setPassword(password);
		userRepository.save(user);

		// prepare user creation request with duplicate email-id
		final var userCreationRequest = mock(UserCreationRequestDto.class);
		when(userCreationRequest.getEmailId()).thenReturn(emailId);

		// invoke method under test and assert exception
		final var exception = assertThrows(AccountAlreadyExistsException.class,
				() -> userService.create(userCreationRequest));
		assertThat(exception.getReason()).isEqualTo("Account with provided email-id already exists");
	}

	@Test
	void userCreationShouldThrowExceptionForInvalidPlanId() {
		// prepare user creation request with random plan-id
		final var emailId = RandomString.make() + "@domain.ut";
		final var planId = UUID.randomUUID();
		final var userCreationRequest = mock(UserCreationRequestDto.class);
		when(userCreationRequest.getEmailId()).thenReturn(emailId);
		when(userCreationRequest.getPlanId()).thenReturn(planId);

		// invoke method under test and verify mock interactions
		final var exception = assertThrows(InvalidPlanException.class, () -> userService.create(userCreationRequest));
		assertThat(exception.getReason()).isEqualTo("No plan exists in the system with provided-id");
	}

	@Test
	void shouldCreateUserEntityForValidUserCreationRequest() {
		// fetch a plan record from datasource
		final var plan = planRepository.findAll().get(0);

		// prepare user creation request with valid plan-id
		final var emailId = RandomString.make() + "@domain.ut";
		final var password = RandomString.make();
		final var planId = plan.getId();
		final var userCreationRequest = mock(UserCreationRequestDto.class);
		when(userCreationRequest.getEmailId()).thenReturn(emailId);
		when(userCreationRequest.getPassword()).thenReturn(password);
		when(userCreationRequest.getPlanId()).thenReturn(planId);

		// invoke method under test
		userService.create(userCreationRequest);

		// assert user record is saved in datasource
		final var recordSaved = userRepository.existsByEmailId(emailId);
		assertThat(recordSaved).isTrue();
	}

	@Test
	void loginShouldThrowExceptionForNonRegisteredEmailId() {
		// prepare login request
		final var emailId = RandomString.make() + "@domain.ut";
		final var userLoginRequest = mock(UserLoginRequestDto.class);
		when(userLoginRequest.getEmailId()).thenReturn(emailId);

		// assert InvalidLoginCredentialsException is thrown for unregistered email-id
		assertThrows(InvalidLoginCredentialsException.class, () -> userService.login(userLoginRequest));
	}

	@Test
	void loginShouldThrowExceptionForInvalidPassword() {
		// create test data in datasource
		final var password = RandomString.make();
		final var testData = new TestData().createTestData(password);
		final var user = testData.getUser();

		// prepare login request with wrong password
		final var emailId = user.getEmailId();
		final var wrongPassword = RandomString.make();
		final var userLoginRequest = mock(UserLoginRequestDto.class);
		when(userLoginRequest.getEmailId()).thenReturn(emailId);
		when(userLoginRequest.getPassword()).thenReturn(wrongPassword);

		// assert InvalidLoginCredentialsException is thrown for invalid password
		assertThrows(InvalidLoginCredentialsException.class, () -> userService.login(userLoginRequest));
	}

	@Test
	void shouldReturnTokenResponseForValidLoginCredentials() {
		// create test data in datasource
		final var password = RandomString.make();
		final var testData = new TestData().createTestData(password);
		final var user = testData.getUser();

		// prepare valid login request
		final var emailId = user.getEmailId();
		final var userLoginRequest = mock(UserLoginRequestDto.class);
		when(userLoginRequest.getEmailId()).thenReturn(emailId);
		when(userLoginRequest.getPassword()).thenReturn(password);

		// invoke method under test
		final var response = userService.login(userLoginRequest);

		// assert response
		assertThat(response.getAccessToken()).isNotBlank().matches(JWT_STRUCTURE_REGEX);
	}

	@Getter
	@Setter
	@RequiredArgsConstructor
	class TestData {

		private User user;

		public TestData createTestData(@NonNull String planTextPassword) {
			// insert test user record in datasource
			final String emailId = RandomString.make();
			final String password = passwordEncoder.encode(planTextPassword);
			final User user = new User();
			user.setEmailId(emailId);
			user.setPassword(password);
			final User savedUser = userRepository.save(user);

			final var testData = new TestData();
			testData.setUser(savedUser);
			return testData;
		}

	}

}
