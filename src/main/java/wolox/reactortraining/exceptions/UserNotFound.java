package wolox.reactortraining.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "User doesn't exists")
public class UserNotFound extends RuntimeException {

}
