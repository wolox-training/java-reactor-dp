package wolox.reactortraining.config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "twitter")
@PropertySource("classpath:twitter.properties")
@Validated
@Getter
@Setter
public class TwitterCredentialProperties {

    @NotEmpty
    private String consumerKey;

    @NotEmpty
    private String consumerSecret;

    @NotEmpty
    private String accessToken;

    @NotEmpty
    private String accessTokenSecret;

    @Min(500)
    private Integer amountToProcess;

}
