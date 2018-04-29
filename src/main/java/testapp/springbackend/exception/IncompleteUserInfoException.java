package testapp.springbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        value = HttpStatus.BAD_REQUEST,
        reason = "Not all fields are filled"
)
public class IncompleteUserInfoException extends RuntimeException {
}
