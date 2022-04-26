package com.reactorlearning.repository;

import com.reactorlearning.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
@ActiveProfiles("test")
class MovieInfoRepositoryIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp() {
        var listOfMoviesInfo = List.of(
                new MovieInfo(null, "Batman Begins",
                        2005, List.of("Bale", "Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo("abc", "Welcome Back",
                        2012, List.of("AK"), LocalDate.parse("2012-08-12"))
        );

        movieInfoRepository.saveAll(listOfMoviesInfo)
                .blockLast();
    }

    @AfterEach
    void tearDown() {

        movieInfoRepository.deleteAll().block();
    }

    @Test
    void findAll() {

        var moviesInfoFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findById() {

        var moviesInfoMono = movieInfoRepository.findById("abc").log();

        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals("Welcome Back", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void findByYear() {

        var moviesInfoMono = movieInfoRepository.findByYear(2012).log();

        StepVerifier.create(moviesInfoMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {

        var movieItem = new MovieInfo(null, "Welcome Back 2",
                2022, List.of("AK"), LocalDate.parse("2022-04-10"));

        var moviesInfoMono = movieInfoRepository.save(movieItem).log();

        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo -> {
                    assertNotNull(movieInfo.getMovieInfoId());
                    assertEquals("Welcome Back 2", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {

        var movieItem = movieInfoRepository.findById("abc").block();
        movieItem.setYear(2020);

        var moviesInfoMono = movieInfoRepository.save(movieItem).log();

        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo -> {
                    assertNotNull(movieInfo.getMovieInfoId());
                    assertEquals(2020, movieInfo.getYear());
                })
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {

        movieInfoRepository.deleteById("abc").block();
        var moviesInfoFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

}