package com.example.demo;

public class JsonParsingFailureException extends RuntimeException {
	public JsonParsingFailureException(Exception message) {
		super(message);
	}
}
