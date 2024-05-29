package com.hzboiler.erp.core.exception;

import com.hzboiler.erp.core.security.DataAccessException;
import com.hzboiler.erp.core.protocal.R;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.hzboiler.erp.core.exception.CoreBusinessExceptionEnums.*;

/**
 * @author gongshuiwen
 */
@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class GlobalExceptionHandler {

    private static final String _BUSINESS_EXCEPTION_MESSAGE_TEMPLATE = "BusinessException [%s,%d]: %s";

    @ExceptionHandler(BusinessException.class)
    public R<String> handleBusinessException(BusinessException e) {
        String namespace = e.getBusinessExceptionEnum().namespace();
        int code = e.getBusinessExceptionEnum().code();
        String message = e.getMessage();
        if (log.isDebugEnabled()) {
            log.debug(String.format(_BUSINESS_EXCEPTION_MESSAGE_TEMPLATE, namespace, code, message));
        }
        return R.error(code, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public R<String> handleAccessDeniedException(Exception e) {
        return R.error(ERROR_ACCESS_DENIED);
    }

    @ExceptionHandler(DataAccessException.class)
    public R<String> handleDataAccessException(Exception e) {
        return R.error(ERROR_ACCESS_DENIED);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public R<Map<String, String>> handleConstraintViolationException(
            ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath() instanceof PathImpl path
                    ? path.getLeafNode().getName()
                    : violation.getPropertyPath().toString();
            errors.put(fieldName, violation.getMessage());
        }
        return R.error(ERROR_INVALID_ARGUMENTS, errors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Map<String, String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return R.error(ERROR_INVALID_ARGUMENTS, errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<String> handleMethodArgumentTypeMismatchException(Exception e) {
        return R.error(ERROR_INVALID_ARGUMENTS);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<String> handleMissingServletRequestParameterException(Exception e) {
        return R.error(ERROR_INVALID_ARGUMENTS);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<String> handleHttpMessageNotReadableException(Exception e) {
        return R.error(ERROR_INVALID_REQUEST_BODY);
    }

    @ExceptionHandler(Exception.class)
    public R<String> handleInternalException(Exception e) {
        log.error(e.toString(), e);
        return R.error(ERROR_INTERNAL);
    }
}
