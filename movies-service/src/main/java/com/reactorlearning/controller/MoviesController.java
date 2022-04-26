package com.reactorlearning.controller;

import com.reactorlearning.client.MovieInfoRestClient;
import com.reactorlearning.client.ReviewsRestClient;
import com.reactorlearning.domain.Movie;
import com.reactorlearning.domain.MovieInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movies")
public class MoviesController {

    private MovieInfoRestClient movieInfoRestClient;
    private ReviewsRestClient reviewsRestClient;

    public MoviesController(MovieInfoRestClient movieInfoRestClient, ReviewsRestClient reviewsRestClient) {
        this.movieInfoRestClient = movieInfoRestClient;
        this.reviewsRestClient = reviewsRestClient;
    }

    @GetMapping("/{id}")
    public Mono<Movie> retrieveMovieById(@PathVariable("id") String movieId) {

        return movieInfoRestClient.retrieveMovieInfo(movieId)
                .flatMap(movieInfo -> {
                    var reviewsListMono = reviewsRestClient.retrieveMovieInfo(movieId)
                            .collectList();

                    return reviewsListMono.map(reviews -> new Movie(movieInfo, reviews));
                });
    }
}