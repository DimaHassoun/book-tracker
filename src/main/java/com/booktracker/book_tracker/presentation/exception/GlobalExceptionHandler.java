package com.booktracker.book_tracker.presentation.exception;

import com.booktracker.book_tracker.domain.exception.DuplicateUserException;
import com.booktracker.book_tracker.domain.exception.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.booktracker.book_tracker.domain.exception.ExternalServiceException;
import com.booktracker.book_tracker.domain.exception.DuplicateReadingInstanceException;
import com.booktracker.book_tracker.domain.exception.UserBookNotFoundException ;
import com.booktracker.book_tracker.domain.exception.ActiveReadingInstanceExistsException ;
import com.booktracker.book_tracker.domain.exception.ReadingInstanceNotFoundException;
import com.booktracker.book_tracker.domain.exception.RereadConfirmationRequiredException;
import com.booktracker.book_tracker.domain.exception.InvalidReadingStatusException;
import com.booktracker.book_tracker.domain.exception.InvalidReadingSessionException;

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
	
	
	@ExceptionHandler(UserBookNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleUserBookNotFound(UserBookNotFoundException ex) {
  	  return ResponseEntity.status(HttpStatus.NOT_FOUND)
  	          .body(Map.of("status", 404, "title", "User Book Not Found", "detail", ex.getMessage()));
	}

	@ExceptionHandler(ActiveReadingInstanceExistsException.class)
	public ResponseEntity<Map<String, Object>> handleActiveReadingInstanceExists(ActiveReadingInstanceExistsException ex) {
 	   return ResponseEntity.status(HttpStatus.CONFLICT)
    	        .body(Map.of("status", 409, "title", "Active Reading Instance Exists", "detail", ex.getMessage()));
	}

	@ExceptionHandler(DuplicateReadingInstanceException.class)
	public ResponseEntity<Map<String, Object>> handleDuplicateReadingInstance(DuplicateReadingInstanceException ex) {
 	   return ResponseEntity.status(HttpStatus.CONFLICT)
    	        .body(Map.of("status", 409, "title", "Duplicate Reading Instance", "detail", ex.getMessage()));
	}
	
	@ExceptionHandler(RereadConfirmationRequiredException.class)
	public ResponseEntity<Map<String, Object>> handleRereadConfirmationRequired(RereadConfirmationRequiredException ex) {
    	return ResponseEntity.status(HttpStatus.CONFLICT)
        	    .body(Map.of("status", 409, "title", "Reread Confirmation Required", "detail", ex.getMessage()));
	}

	@ExceptionHandler(InvalidReadingStatusException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidReadingStatus(InvalidReadingStatusException ex) {
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        	    .body(Map.of("status", 400, "title", "Invalid Reading Status", "detail", ex.getMessage()));
	}
	
	@ExceptionHandler(ReadingInstanceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleReadingInstanceNotFound(ReadingInstanceNotFoundException ex) {
 	   return ResponseEntity.status(HttpStatus.NOT_FOUND)
    	        .body(Map.of("status", 404, "title", "Reading Instance Not Found", "detail", ex.getMessage()));
	}
	
	@ExceptionHandler(InvalidReadingSessionException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidReadingSession(InvalidReadingSessionException ex) {
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        	    .body(Map.of("status", 400, "title", "Invalid Reading Session", "detail", ex.getMessage()));
	}
}