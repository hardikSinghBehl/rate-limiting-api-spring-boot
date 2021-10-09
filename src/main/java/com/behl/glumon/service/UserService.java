package com.behl.glumon.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.behl.glumon.dto.UserAccountCreationRequestDto;
import com.behl.glumon.dto.UserLoginRequestDto;
import com.behl.glumon.dto.UserLoginSuccessDto;
import com.behl.glumon.entity.User;
import com.behl.glumon.entity.UserPlanMapping;
import com.behl.glumon.repository.PlanRepository;
import com.behl.glumon.repository.UserPlanMappingRepository;
import com.behl.glumon.repository.UserRepository;
import com.behl.glumon.security.utility.JwtUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlanRepository planRepository;
    private final UserPlanMappingRepository userPlanMappingRepository;
    private final JwtUtils jwtUtils;

    @Transactional
    public ResponseEntity<?> signUp(final UserAccountCreationRequestDto userAccountCreationRequestDto) {

        if (userRepository.existsByEmailId(userAccountCreationRequestDto.getEmailId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "user account already eixsts with provided email-id");

        final var user = new User();
        user.setEmailId(userAccountCreationRequestDto.getEmailId());
        user.setPassword(passwordEncoder.encode(userAccountCreationRequestDto.getPassword()));
        final var savedUser = userRepository.save(user);

        final var plan = planRepository.findById(userAccountCreationRequestDto.getPlanId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid plan-id provided"));

        final var userPlanMapping = new UserPlanMapping();
        userPlanMapping.setIsActive(true);
        userPlanMapping.setUser(savedUser);
        userPlanMapping.setPlan(plan);
        userPlanMappingRepository.save(userPlanMapping);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<UserLoginSuccessDto> login(final UserLoginRequestDto userLoginRequestDto) {
        final User user = userRepository.findByEmailId(userLoginRequestDto.getEmailId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid login credentials"));

        if (!passwordEncoder.matches(userLoginRequestDto.getPassword(), user.getPassword()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid login credentials");

        return ResponseEntity.ok(UserLoginSuccessDto.builder().jwt(jwtUtils.generateToken(user)).build());
    }

}
