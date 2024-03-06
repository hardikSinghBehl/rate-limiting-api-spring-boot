package com.behl.overseer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.behl.overseer.dto.UserCreationRequestDto;
import com.behl.overseer.dto.UserLoginRequestDto;
import com.behl.overseer.entity.User;
import com.behl.overseer.entity.UserPlanMapping;
import com.behl.overseer.exception.AccountAlreadyExistsException;
import com.behl.overseer.exception.InvalidLoginCredentialsException;
import com.behl.overseer.exception.InvalidPlanException;
import com.behl.overseer.repository.PlanRepository;
import com.behl.overseer.repository.UserPlanMappingRepository;
import com.behl.overseer.repository.UserRepository;
import com.behl.overseer.utility.JwtUtility;

class UserServiceTest {

	private final JwtUtility jwtUtility = mock(JwtUtility.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final PlanRepository planRepository = mock(PlanRepository.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
	private final UserPlanMappingRepository userPlanMappingRepository = mock(UserPlanMappingRepository.class);
	private final UserService userService = new UserService(jwtUtility, userRepository, planRepository, passwordEncoder,
			userPlanMappingRepository);

	@Test
	void userCreationShouldThrowExceptionForDuplicateEmailId() {
		// prepare user creation request
		final var emailId = "duplicate@domain.ut";
		final var userCreationRequest = mock(UserCreationRequestDto.class);
		when(userCreationRequest.getEmailId()).thenReturn(emailId);

		// set datasource to evaluate duplicate emailid
		when(userRepository.existsByEmailId(emailId)).thenReturn(Boolean.TRUE);

		// invoke method under test and assert exception
		final var exception = assertThrows(AccountAlreadyExistsException.class,
				() -> userService.create(userCreationRequest));
		assertThat(exception.getReason()).isEqualTo("Account with provided email-id already exists");

		// verify mock interactions
		verify(userRepository, times(1)).existsByEmailId(emailId);
		verify(userCreationRequest, times(1)).getEmailId();
		verify(userRepository, times(0)).save(any(User.class));
	}

	@Test
	void userCreationShouldThrowExceptionForInvalidPlanId() {
		// prepare user creation request
		final var emailId = "valid@domain.ut";
		final var planId = UUID.randomUUID();
		final var userCreationRequest = mock(UserCreationRequestDto.class);
		when(userCreationRequest.getEmailId()).thenReturn(emailId);
		when(userCreationRequest.getPlanId()).thenReturn(planId);

		// set datasource to evaluate valid email-id and invalid plan-id
		when(userRepository.existsByEmailId(emailId)).thenReturn(Boolean.FALSE);
		when(planRepository.existsById(planId)).thenReturn(Boolean.FALSE);

		// invoke method under test and verify mock interactions
		final var exception = assertThrows(InvalidPlanException.class, () -> userService.create(userCreationRequest));
		assertThat(exception.getReason()).isEqualTo("No plan exists in the system with provided-id");

		// verify mock interactions
		verify(userCreationRequest, times(1)).getEmailId();
		verify(userCreationRequest, times(1)).getPlanId();
		verify(planRepository, times(1)).existsById(planId);
		verify(userRepository, times(0)).save(any(User.class));
	}

	@Test
	void shouldCreateUserEntityForValidUserCreationRequest() {
		// prepare user creation request
		final var emailId = "valid@domain.ut";
		final var password = "valid-password";
		final var planId = UUID.randomUUID();
		final var userCreationRequest = mock(UserCreationRequestDto.class);
		when(userCreationRequest.getEmailId()).thenReturn(emailId);
		when(userCreationRequest.getPassword()).thenReturn(password);
		when(userCreationRequest.getPlanId()).thenReturn(planId);

		// set datasource to evaluate valid request values
		when(userRepository.existsByEmailId(emailId)).thenReturn(Boolean.FALSE);
		when(planRepository.existsById(planId)).thenReturn(Boolean.TRUE);

		// configure password encoder to encode plan-text password
		final var encodedPassword = "encoded-password";
		when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

		// set datasource to save user successfully
		when(userRepository.save(any(User.class))).thenReturn(mock(User.class));

		// invoke method under test
		userService.create(userCreationRequest);

		// verify mock interactions
		verify(userCreationRequest, times(1)).getEmailId();
		verify(userCreationRequest, times(1)).getPassword();
		verify(userCreationRequest, times(1)).getPlanId();

		verify(userRepository).existsByEmailId(emailId);
		verify(planRepository).existsById(planId);
		verify(passwordEncoder).encode(password);

		verify(userRepository, times(1)).save(any(User.class));
		verify(userPlanMappingRepository, times(1)).save(any(UserPlanMapping.class));
	}

	@Test
	void loginShouldThrowExceptionForNonRegisteredEmailId() {
		// prepare login request
		final var emailId = "unregistered@domain.ut";
		final var userLoginRequest = mock(UserLoginRequestDto.class);
		when(userLoginRequest.getEmailId()).thenReturn(emailId);

		// set datasource to return no response for unregistered email-id
		when(userRepository.findByEmailId(emailId)).thenReturn(Optional.empty());

		// assert InvalidLoginCredentialsException is thrown for unregistered email-id
		assertThrows(InvalidLoginCredentialsException.class, () -> userService.login(userLoginRequest));

		// verify mock interactions
		verify(userLoginRequest, times(1)).getEmailId();
		verify(userRepository, times(1)).findByEmailId(emailId);
	}

	@Test
	void loginShouldThrowExceptionForInvalidPassword() {
		// prepare login request
		final var emailId = "mail@domain.ut";
		final var password = "test-password";
		final var userLoginRequest = mock(UserLoginRequestDto.class);
		when(userLoginRequest.getEmailId()).thenReturn(emailId);
		when(userLoginRequest.getPassword()).thenReturn(password);

		// prepare datasource to return saved user
		final var encodedPassword = "test-encoded-password";
		final var user = mock(User.class);
		when(user.getPassword()).thenReturn(encodedPassword);
		when(userRepository.findByEmailId(emailId)).thenReturn(Optional.of(user));

		// set password validation to fail
		when(passwordEncoder.matches(password, encodedPassword)).thenReturn(Boolean.FALSE);

		// assert InvalidLoginCredentialsException is thrown for invalid password
		assertThrows(InvalidLoginCredentialsException.class, () -> userService.login(userLoginRequest));

		// verify mock interactions
		verify(userLoginRequest, times(1)).getEmailId();
		verify(userLoginRequest, times(1)).getPassword();
		verify(user, times(1)).getPassword();

		verify(userRepository, times(1)).findByEmailId(emailId);
		verify(passwordEncoder, times(1)).matches(password, encodedPassword);
	}

	@Test
	void shouldReturnTokenResponseForValidLoginCredentials() {
		// prepare login request
		final var emailId = "mail@domain.ut";
		final var password = "test-password";
		final var userLoginRequest = mock(UserLoginRequestDto.class);
		when(userLoginRequest.getEmailId()).thenReturn(emailId);
		when(userLoginRequest.getPassword()).thenReturn(password);

		// prepare datasource to return saved user
		final var encodedPassword = "test-encoded-password";
		final var userId = UUID.randomUUID();
		final var user = mock(User.class);
		when(user.getPassword()).thenReturn(encodedPassword);
		when(user.getId()).thenReturn(userId);
		when(userRepository.findByEmailId(emailId)).thenReturn(Optional.of(user));

		// set password validation to pass
		when(passwordEncoder.matches(password, encodedPassword)).thenReturn(Boolean.TRUE);

		// set token generation
		final var accessToken = "test-access-token";
		when(jwtUtility.generateAccessToken(userId)).thenReturn(accessToken);

		// invoke method under test
		final var response = userService.login(userLoginRequest);

		// assert response contains generated access-token
		assertThat(response.getAccessToken()).isEqualTo(accessToken);

		// verify mock interactions
		verify(userLoginRequest, times(1)).getEmailId();
		verify(userLoginRequest, times(1)).getPassword();
		verify(user, times(1)).getPassword();

		verify(userRepository, times(1)).findByEmailId(emailId);
		verify(passwordEncoder, times(1)).matches(password, encodedPassword);
		verify(jwtUtility, times(1)).generateAccessToken(userId);
	}

}
