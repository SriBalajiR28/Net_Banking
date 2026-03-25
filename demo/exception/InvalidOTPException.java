package com.cmrit.demo.exception;

public class InvalidOTPException  extends RuntimeException{

	public InvalidOTPException(String message) {
		super(message);
	}
}
