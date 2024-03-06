package com.behl.overseer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.behl.overseer.InitializeMysqlContainer;
import com.behl.overseer.entity.User;

import net.bytebuddy.utility.RandomString;

@DataJpaTest
@InitializeMysqlContainer
class UserRepositoryIT {

	@Autowired
	private UserRepository userRepository;

	@Test
	void shouldReturnTrueIfRecordWithEmailIdExists() {
		// insert test user record in datasource
		final var emailId = RandomString.make();
		final var password = RandomString.make();
		final var user = new User();
		user.setEmailId(emailId);
		user.setPassword(password);
		userRepository.save(user);

		// invoke method under test
		final var response = userRepository.existsByEmailId(emailId);

		// assert response
		assertThat(response).isTrue();
	}

	@Test
	void shouldReturnFalseIfRecordDoesNotExistWithEmailId() {
		// prepare random invalid emailId
		final var emailId = RandomString.make();

		// invoke method under test
		final var response = userRepository.existsByEmailId(emailId);

		// assert response
		assertThat(response).isFalse();
	}

	@Test
	void shouldFetchUserByEmailId() {
		// insert test user record in datasource
		final var emailId = RandomString.make();
		final var password = RandomString.make();
		final var user = new User();
		user.setEmailId(emailId);
		user.setPassword(password);
		userRepository.save(user);

		// invoke method under test
		final var fetchedUser = userRepository.findByEmailId(emailId);

		// assert fetched record's attributes
		assertThat(fetchedUser).isPresent().get().satisfies(record -> {
			assertThat(record.getId()).isNotNull();
			assertThat(record.getEmailId()).isEqualTo(emailId);
			assertThat(record.getPassword()).isEqualTo(password);
			assertThat(record.getCreatedAt()).isNotNull();
		});
	}

	@Test
	void shouldReturnEmptyOptionalWhenFetchingUserWithInvalidEmailId() {
		// prepare random invalid emailId
		final var emailId = RandomString.make();

		// invoke method under test
		final var fetchedUser = userRepository.findByEmailId(emailId);

		// assert fetched record
		assertThat(fetchedUser).isEmpty();
	}

}