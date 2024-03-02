package com.behl.overseer.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.behl.overseer.dto.TokenSuccessResponseDto;
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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final JwtUtility jwtUtility;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPlanMappingRepository userPlanMappingRepository;

    @Transactional
    public void create(@NonNull final UserCreationRequestDto userCreationRequest) {
		final var emailId = userCreationRequest.getEmailId();
		final var userAccountExistsWithEmailId = userRepository.existsByEmailId(emailId);
		if (Boolean.TRUE.equals(userAccountExistsWithEmailId)) {
			throw new AccountAlreadyExistsException("Account with provided email-id already exists");
		}

        final var user = new User();
		final var encodedPassword = passwordEncoder.encode(userCreationRequest.getPassword());
        user.setEmailId(emailId);
        user.setPassword(encodedPassword);
        final var savedUser = userRepository.save(user);
        
        final var isPlanIdValid = planRepository.existsById(userCreationRequest.getPlanId());
        if (Boolean.FALSE.equals(isPlanIdValid)) {
        	throw new InvalidPlanException("No plan exists in the system with provided-id");
        }
        
        final var userPlanMapping = new UserPlanMapping();
        userPlanMapping.setUserId(savedUser.getId());
        userPlanMapping.setPlanId(userCreationRequest.getPlanId());
        userPlanMappingRepository.save(userPlanMapping);
    }
    
    public TokenSuccessResponseDto login(@NonNull final UserLoginRequestDto userLoginRequest) {
    	final var user = userRepository.findByEmailId(userLoginRequest.getEmailId())
    			.orElseThrow(InvalidLoginCredentialsException::new);

		final var encodedPassword = user.getPassword();
		final var plainTextPassword = userLoginRequest.getPassword();
		final var isCorrectPassword = passwordEncoder.matches(plainTextPassword, encodedPassword);
		if (Boolean.FALSE.equals(isCorrectPassword)) {
			throw new InvalidLoginCredentialsException();
		}
		
		final var accessToken = jwtUtility.generateAccessToken(user);		
		return TokenSuccessResponseDto.builder()
				.accessToken(accessToken)
				.build();
    }

}