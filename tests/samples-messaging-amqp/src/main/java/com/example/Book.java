package com.example;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Book {

	private String name;

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public Book(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
