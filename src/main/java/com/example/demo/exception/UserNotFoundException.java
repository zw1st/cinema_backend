package com.example.demo.exception;

public class UserNotFoundException extends RuntimeException {
    public <T> UserNotFoundException(Long userId) {
        super(String.format("User with id %s is not found", userId));
    }
}
