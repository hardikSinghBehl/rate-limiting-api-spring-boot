package com.behl.overseer.utility;

import org.springframework.stereotype.Component;

import com.behl.overseer.dto.JokeResponseDto;

import net.datafaker.Faker;
import net.datafaker.providers.entertainment.Joke;

@Component
public class JokeGenerator {

	private final Joke joke;

	public JokeGenerator() {
		this.joke = new Faker().joke();
	}

	public JokeResponseDto generate() {
		final var pun = joke.pun();
		return JokeResponseDto.builder().joke(pun).build();
	}

}
