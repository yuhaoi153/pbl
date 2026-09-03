package flyfish.handler;

import flyfish.contoller.M_HardWareOledController;
import flyfish.contoller.M_HardWareMessageController;
import flyfish.exception.M_HardWareOledApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = {
        M_HardWareOledController.class,
        M_HardWareMessageController.class
})
public class M_HardWareOledApiExceptionHandler {
    @ExceptionHandler(M_HardWareOledApiException.class)
    public ResponseEntity<M_HardWareOledApiError> handleOledApiException(
            M_HardWareOledApiException exception,
            HttpServletRequest request) {
        return error(exception.getStatus(), exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<M_HardWareOledApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                fields.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "请求参数校验失败", request, fields);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<M_HardWareOledApiError> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                fields.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return error(HttpStatus.BAD_REQUEST, "请求参数校验失败", request, fields);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<M_HardWareOledApiError> handleMalformedRequest(
            Exception exception,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "请求格式或参数类型不正确", request, Map.of());
    }

    private ResponseEntity<M_HardWareOledApiError> error(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors) {
        M_HardWareOledApiError body = new M_HardWareOledApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors);
        return ResponseEntity.status(status).body(body);
    }

    public record M_HardWareOledApiError(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> fieldErrors) {
    }
}
