package ro.tuc.ds2020.controllers.handlers;


import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ro.tuc.ds2020.controllers.handlers.exceptions.model.*;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException e, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        List<String> details = e.getConstraintViolations().stream().map(v -> v.getPropertyPath() + ": " + v.getMessage()).toList();
        ExceptionHandlerResponseDTO errorInformation = new ExceptionHandlerResponseDTO(
                ConstraintViolationException.class.getSimpleName(),
                status.getReasonPhrase(),
                status.value(),
                e.getMessage(),
                details,
                request.getDescription(false)
        );
        return handleExceptionInternal(
                e,
                errorInformation,
                new HttpHeaders(),
                status,
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> details = new ArrayList<>();
        for (ObjectError err : ex.getBindingResult().getAllErrors()) {
            String name = (err instanceof FieldError fieldError) ? fieldError.getField() : err.getObjectName();
            details.add(name + ":" + err.getDefaultMessage());
        }
        ExceptionHandlerResponseDTO errorInformation = new ExceptionHandlerResponseDTO(ex.getParameter().getParameterName(),
                HttpStatus.valueOf(status.value()).getReasonPhrase(),
                status.value(),
                MethodArgumentNotValidException.class.getSimpleName(),
                details,
                request.getDescription(false));
        return handleExceptionInternal(
                ex,
                errorInformation,
                new HttpHeaders(),
                status,
                request
        );
    }

    @ExceptionHandler(value = {ResourceNotFoundException.class, DuplicateResourceException.class, EntityValidationException.class, ParameterValidationException.class})
    protected ResponseEntity<Object> handleCustom(CustomException ex,
                                                            WebRequest request) {
        ExceptionHandlerResponseDTO errorInformation = new ExceptionHandlerResponseDTO(ex.getResource(),
                ex.getStatus().getReasonPhrase(),
                ex.getStatus().value(),
                ex.getMessage(),
                ex.getValidationErrors(),
                request.getDescription(false));
        return handleExceptionInternal(
                ex,
                errorInformation,
                new HttpHeaders(),
                ex.getStatus(),
                request
        );
    }


}
