package com.reactorlearning.client;

import com.reactorlearning.domain.MovieInfo;
import com.reactorlearning.domain.Review;
import com.reactorlearning.exception.MoviesInfoClientException;
import com.reactorlearning.exception.MoviesInfoServerException;
import com.reactorlearning.exception.ReviewsClientException;
import com.reactorlearning.exception.ReviewsServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReviewsRestClient {

    private WebClient webClient;

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    public ReviewsRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Review> retrieveMovieInfo(String movieId) {

        var url = UriComponentsBuilder.fromHttpUrl(reviewsUrl)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand().toUriString();

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.empty();
                    }

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ReviewsClientException(
                                    responseMessage
                            )));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(responseMessage -> Mono.error(new ReviewsServerException(
                                        "Server Exception in ReviewsService " + responseMessage
                                ))))
                .bodyToFlux(Review.class)
                .log();
    }
}
