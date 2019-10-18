package wolox.reactortraining.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Service;

@Service
public class TwitterService {

    @Autowired
    private Twitter twitter;

    public SearchResults searchSpringBoot(String query) {
        return twitter
            .searchOperations()
            .search(query);
    }
}
