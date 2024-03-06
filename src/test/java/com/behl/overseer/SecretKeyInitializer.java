package com.behl.overseer;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecretKeyInitializer implements BeforeAllCallback {

	@Override
	public void beforeAll(final ExtensionContext context) {
		log.info("Initializing secret key in application");
		final var secretKey = Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded());
		System.setProperty("com.behl.overseer.token.secret-key", secretKey);
	}

}