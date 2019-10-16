package wolox.reactortraining.controllers;

import javax.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wolox.reactortraining.services.TwitterService;

@RestController
@Validated
public class TwitterController {

    @Autowired
    private TwitterService twitterService;

    @GetMapping("/twitter")
    public SearchResults twitter(@NotEmpty @RequestParam("query") String query) {
        return twitterService.searchSpringBoot(query);
    }
}
