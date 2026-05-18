package com.example.demo.exception;

public class NotFoundException extends RuntimeException {
    public <T> NotFoundException(Class<T> entClass, Long id) {
        super(String.format("%s with id %s is not found", entClass.getSimpleName(), id));
    }

    public <T> NotFoundException(Class<T> entClass, String fieldName, String value) {
        super(String.format("%s with %s '%s' is not found", entClass.getSimpleName(), fieldName, value));
    }

    public <T> NotFoundException(String value) {
        super(value);
    }
}
