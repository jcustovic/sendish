package com.sendish.api.controller.advice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.sendish.api.controller.model.ValidationError;

import java.util.*;

@ControllerAdvice
public class ErrorAdvice {

    private MessageSource messageSource;

    @Autowired
    public ErrorAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationError handleValidationException(MethodArgumentNotValidException exception) {
        BindingResult result = exception.getBindingResult();

        Map<String, List<String>> errors = new HashMap<>(result.getFieldErrorCount());
        for (FieldError fieldError : result.getFieldErrors()) {
            List<String> fieldErrors =  errors.get(fieldError.getField());
            if (fieldErrors == null) {
                fieldErrors = new ArrayList<>();
                errors.put(fieldError.getField(), fieldErrors);
            }
            String message = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
            fieldErrors.add(message);
        }

        return new ValidationError(errors);
    }

}
