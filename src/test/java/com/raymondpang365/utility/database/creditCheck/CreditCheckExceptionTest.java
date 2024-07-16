package com.raymondpang365.utility.database.creditCheck;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CreditCheckExceptionTest {

    @Test
    @DisplayName("Test CreditCheckException default constructor")
    void testCreditCheckExceptionDefaultConstructor() {
        final CreditCheckException exception = new CreditCheckException();
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Test CreditCheckException constructor with String argument")
    void testCreditCheckExceptionConstructorWithStringArgument() {
        final String message = "exception message";
        final CreditCheckException exception = new CreditCheckException(message);
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Test CreditCheckException constructor with Throwable argument")
    void testCreditCheckExceptionConstructorWithThrowableArgument() {
        final Throwable cause = new Throwable();
        final CreditCheckException exception = new CreditCheckException(cause);
        assertNotNull(exception);
        assertEquals(cause, exception.getCause());
        assertEquals("java.lang.Throwable", exception.getMessage());
    }

    @Test
    @DisplayName("Test CreditCheckException constructor with String and Throwable arguments")
    void testCreditCheckExceptionConstructorWithStringAndThrowableArguments() {
        final String message = "exception message";
        final Throwable cause = new Throwable();
        final CreditCheckException exception = new CreditCheckException(message, cause);
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

}
