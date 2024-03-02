package com.behl.overseer.utility;

import org.springframework.stereotype.Component;

import com.behl.overseer.dto.JokeResponseDto;

import net.datafaker.Faker;
import net.datafaker.providers.entertainment.Joke;

/**
 * Utility class for generating random jokes.
 */
@Component
public class JokeGenerator {

	private final Joke joke;

	public JokeGenerator() {
		this.joke = new Faker().joke();
	}

	/**
	 * Generates a random joke.
	 * 
	 * @return JokeResponseDto containing the generated joke
	 */
	public JokeResponseDto generate() {
		final var pun = joke.pun();
		return JokeResponseDto.builder().joke(pun).build();
	}

}
