package com.behl.overseer;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;

@Slf4j
public class RedisCacheInitializer implements BeforeAllCallback {

	private static final int REDIS_PORT = 6379;
	private static final String REDIS_PASSWORD = RandomString.make(10);
	private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7.2.3-alpine");

	private static final GenericContainer<?> redisContainer = new GenericContainer<>(REDIS_IMAGE)
			.withExposedPorts(REDIS_PORT).withCommand("redis-server", "--requirepass", REDIS_PASSWORD);

	@Override
	public void beforeAll(final ExtensionContext context) {
		log.info("Creating test cache container : {}", REDIS_IMAGE);
		redisContainer.start();
		addCacheProperties();
		log.info("Successfully started test cache container : {}", REDIS_IMAGE);
	}

	private void addCacheProperties() {
		System.setProperty("spring.data.redis.host", redisContainer.getHost());
		System.setProperty("spring.data.redis.port", String.valueOf(redisContainer.getMappedPort(REDIS_PORT)));
		System.setProperty("spring.data.redis.password", REDIS_PASSWORD);
	}

}