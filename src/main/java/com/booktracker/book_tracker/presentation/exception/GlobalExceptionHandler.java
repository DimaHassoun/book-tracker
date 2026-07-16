package com.booktracker.book_tracker.presentation.exception;

import com.booktracker.book_tracker.domain.exception.DuplicateUserException;
import com.booktracker.book_tracker.domain.exception.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.booktracker.book_tracker.domain.exception.ExternalServiceException;
import com.booktracker.book_tracker.domain.exception.DuplicateBookException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateUser(DuplicateUserException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("status", 409, "title", "Duplicate User", "detail", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("status", 401, "title", "Invalid Credentials", "detail", ex.getMessage()));
    }
	
	@ExceptionHandler(ExternalServiceException.class)
	public ResponseEntity<Map<String, Object>> handleExternalServiceException(
  	  	    ExternalServiceException ex) {

   	 return ResponseEntity
      	      .status(HttpStatus.SERVICE_UNAVAILABLE)
      	      .body(Map.of(
        	           "status", 503,
         	           "title", "External Service Unavailable",
            	        "detail", ex.getMessage()
          		  ));
	}
	
	@ExceptionHandler(DuplicateBookException.class)
	public ResponseEntity<Map<String, Object>> handleDuplicateBook(DuplicateBookException ex) {
   	 return ResponseEntity.status(HttpStatus.CONFLICT)
   	         .body(Map.of("status", 409, "title", "Duplicate Book", "detail", ex.getMessage()));
	}
}