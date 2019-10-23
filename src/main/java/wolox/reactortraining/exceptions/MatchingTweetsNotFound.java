package wolox.reactortraining.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Tweets about topics not found in stream")
public class MatchingTweetsNotFound extends RuntimeException {

}
