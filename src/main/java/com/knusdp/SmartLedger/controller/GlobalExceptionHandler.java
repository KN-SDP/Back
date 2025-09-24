package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.ErrorResponseDto;
import com.knusdp.SmartLedger.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // EmailDuplicateException이 발생하면 이 메소드가 실행됩니다.
    @ExceptionHandler(EmailDuplicateException.class)
    public ResponseEntity<ErrorResponseDto> handleEmailDuplicateException(EmailDuplicateException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                409,
                "DuplicateEmail",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(NickNameDuplicateException.class)
    public ResponseEntity<ErrorResponseDto> handleNickNameDuplicateException(NickNameDuplicateException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                409,
                "DuplicateNickname",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(EmailValidationError.class)
    public ResponseEntity<ErrorResponseDto> handleEmailValidationError(EmailValidationError ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                400,
                "ValidationError",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                404, // 404
                "USER_NOT_FOUND",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ErrorResponseDto> handleLoginFailedException(LoginFailedException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                401, // 401
                "LOGIN_FAILED",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
}