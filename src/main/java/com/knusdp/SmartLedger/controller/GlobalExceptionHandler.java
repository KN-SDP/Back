package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.ErrorResponseDto;
import com.knusdp.SmartLedger.entity.TransactionType;
import com.knusdp.SmartLedger.exception.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                409, // 409
                "DATA_INTEGRITY_VIOLATION",
                "이미 사용 중인 정보가 포함되어 있습니다. (예: 이름, 이메일, 닉네임 등)"
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(MissingRequiredFieldException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingRequiredFieldException(MissingRequiredFieldException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                400, // 409
                "MISSING_REQUIRED_FIELD",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidAmountException(InvalidAmountException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                400, // 409
                "INVALID_AMOUNT",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message;
        // TransactionType 변환 오류일 경우 더 구체적인 메시지 제공
        if (ex.getRequiredType() != null && ex.getRequiredType().equals(TransactionType.class)) {
            message = "거래 타입은 'INCOME', 'EXPENSE', 'SAVING', 'TRANSFER' 중 하나여야 합니다.";
        } else {
            message = "요청 파라미터의 형식이 올바르지 않습니다.";
        }

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_QUERY_PARAMETER",
                message
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}