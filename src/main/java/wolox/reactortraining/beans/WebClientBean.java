package wolox.reactortraining.beans;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientBean {

    @Value("${api.bots.uri}")
    private String botsUri;

    @Bean("bot-api")
    public WebClient botApiClient() {
        return WebClient.create(botsUri);
    }
}
