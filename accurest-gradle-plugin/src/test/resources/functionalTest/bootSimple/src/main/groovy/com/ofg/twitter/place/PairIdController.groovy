package com.ofg.twitter.place

import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import static org.springframework.web.bind.annotation.RequestMethod.PUT

@Slf4j
@RestController
@RequestMapping('/api')
@TypeChecked
class PairIdController {

    @RequestMapping(
            value = '{pairId}',
            method = PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    void getPlacesFromTweets(@PathVariable long pairId, @RequestBody List<Tweet> tweets) {
        log.info("Inside PairIdController, doing very important logic")
        if (tweets?.text != ["Gonna see you at Warsaw"]) {
            throw new IllegalArgumentException("Wrong text in tweet: ${tweets?.text}")
        }
    }
}
