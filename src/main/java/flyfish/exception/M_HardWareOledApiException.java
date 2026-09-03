package flyfish.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class M_HardWareOledApiException extends RuntimeException {
    private final HttpStatus status;

    public M_HardWareOledApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static M_HardWareOledApiException badRequest(String message) {
        return new M_HardWareOledApiException(HttpStatus.BAD_REQUEST, message);
    }

    public static M_HardWareOledApiException unauthorized(String message) {
        return new M_HardWareOledApiException(HttpStatus.UNAUTHORIZED, message);
    }

    public static M_HardWareOledApiException notFound(String message) {
        return new M_HardWareOledApiException(HttpStatus.NOT_FOUND, message);
    }

    public static M_HardWareOledApiException conflict(String message) {
        return new M_HardWareOledApiException(HttpStatus.CONFLICT, message);
    }
}
