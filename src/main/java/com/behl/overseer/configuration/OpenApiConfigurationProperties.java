package com.behl.overseer.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "com.behl.overseer")
public class OpenApiConfigurationProperties {

	private OpenAPI openApi = new OpenAPI();

	@Getter
	@Setter
	public class OpenAPI {
		
		/**
		 * Determines whether Swagger v3 API documentation and related endpoints are
		 * accessible bypassing Authentication and Authorization checks. Swagger
		 * endpoints are restricted by default.
		 * 
		 * Can be used in profile-specific configuration files to control
		 * access based on current environments.
		 */
		private boolean enabled;
		
		private String title;
		private String apiVersion;
		private String description;
		
	}

}