package wolox.reactortraining;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import org.springframework.http.HttpHeaders;

public class TestUtils {

    public static MockResponse mockResponse(ObjectMapper mapper, String contentType, int statusCode,
        Object responseData) throws JsonProcessingException {
        return new MockResponse()
            .addHeader(HttpHeaders.CONTENT_TYPE, contentType)
            .setResponseCode(statusCode)
            .setBody(mapper.writeValueAsString(responseData));
    }

}
