package com.apigcc.core.exception;

public class TypeResolverException extends RuntimeException {
	public TypeResolverException() {
	}

	public TypeResolverException(String message) {
		super(message);
	}

	public TypeResolverException(String message, Throwable cause) {
		super(message, cause);
	}

	public TypeResolverException(Throwable cause) {
		super(cause);
	}

	public TypeResolverException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
