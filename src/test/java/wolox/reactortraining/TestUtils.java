package wolox.reactortraining;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import wolox.reactortraining.dtos.UserDto;
import wolox.reactortraining.models.User;

public class TestUtils {

    public static MockResponse mockResponse(ObjectMapper mapper, String contentType, int statusCode,
        Object responseData) throws JsonProcessingException {
        return new MockResponse()
            .addHeader(HttpHeaders.CONTENT_TYPE, contentType)
            .setResponseCode(statusCode)
            .setBody(mapper.writeValueAsString(responseData));
    }

    @NotNull
    public static User generateUser() {
        UserDto dto = new UserDto();
        dto.setUsername(randomAlphanumeric(6));

        return new User(dto);
    }

}
