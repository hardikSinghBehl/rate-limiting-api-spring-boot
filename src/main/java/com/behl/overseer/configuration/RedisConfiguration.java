package com.behl.overseer.configuration;

import java.util.UUID;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.jcache.JCacheProxyManager;

@Configuration
public class RedisConfiguration {

	private static final String CACHE_NAME = "rate-limit";

	@Bean
	public Config config(final RedisProperties redisProperties) {
		final var connectionUrl = String.format("redis://%s:%d", redisProperties.getHost(), redisProperties.getPort());
		final var cacheCanfiguration = new Config();
		cacheCanfiguration.useSingleServer().setPassword(redisProperties.getPassword()).setAddress(connectionUrl);
		return cacheCanfiguration;
	}

	@Bean(name = "rate-limit-cache-manager")
	public CacheManager cacheManager(final Config config) {
		final var cacheManager = Caching.getCachingProvider().getCacheManager();
		cacheManager.createCache(CACHE_NAME, RedissonConfiguration.fromConfig(config));
		return cacheManager;
	}

	@Bean
	ProxyManager<UUID> proxyManager(final CacheManager cacheManager) {
		return new JCacheProxyManager<UUID>(cacheManager.getCache(CACHE_NAME));
	}

}