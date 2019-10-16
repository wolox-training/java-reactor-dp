package wolox.reactortraining.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.SocialConfigurer;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;

@Configuration
public class TwitterConfig implements SocialConfigurer {

    @Autowired
    private TwitterCredentialProperties twitterCredentialProperties;

    @Override
    public void addConnectionFactories(ConnectionFactoryConfigurer connectionFactoryConfigurer,
        Environment environment) {
        TwitterConnectionFactory twitterConnectionFactory = new TwitterConnectionFactory(
            twitterCredentialProperties.getConsumerKey(),
            twitterCredentialProperties.getConsumerSecret());

        connectionFactoryConfigurer.addConnectionFactory(twitterConnectionFactory);
    }

    @Override
    public UserIdSource getUserIdSource() {
        return null;
    }

    @Override
    public UsersConnectionRepository getUsersConnectionRepository(
        ConnectionFactoryLocator connectionFactoryLocator) {
        return null;
    }

    @Bean
    public Twitter twitterTemplate() {
        return new TwitterTemplate(twitterCredentialProperties.getConsumerKey(),
            twitterCredentialProperties.getConsumerSecret());
    }
}
