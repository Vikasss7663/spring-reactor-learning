package com.reactorlearning.controller;

import com.reactorlearning.domain.MovieInfo;
import com.reactorlearning.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    static String MOVIES_INFO_URL = "/v1/movieinfos";

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
    void addMovieInfo() {

        var movieInfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Bale", "Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();

                    assertNotNull(savedMovieInfo);
                    assertNotNull(savedMovieInfo.getMovieInfoId());
                });
    }

    @Test
    void addMovieInfo_validation() {

        var movieInfo = new MovieInfo(null, "",
                -2005, List.of(""), LocalDate.parse("2005-06-15"));

        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    System.out.println(responseBody);
                    var expectedErrorMessage = "movieInfo.cast must be present,movieInfo.name must be present,movieInfo.year must be a Positive value";
                    assertEquals(expectedErrorMessage, responseBody);
                    assert responseBody!=null;
                });
    }

    @Test
    void getAllMovieInfos() {

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(2);
    }

    @Test
    void getMovieInfoByYear() {

        var year = 2005;

        var uri = UriComponentsBuilder.fromUriString(MOVIES_INFO_URL)
                .queryParam("year", year)
                .buildAndExpand().toUri();

        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void getMovieInfoById() {

        var id = "abc";
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL+"/{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();

                    assertNotNull(movieInfo);
                    assertEquals("abc", movieInfo.getMovieInfoId());
                    assertEquals("Welcome Back", movieInfo.getName());
                });
    }

    @Test
    void getMovieInfoById_approach2() {

        var id = "abc";
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL+"/{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Welcome Back");
    }

    @Test
    void getMovieInfoById_notFound() {

        var id = "def";
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL+"/{id}", id)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void updateMovieInfo() {

        var id = "abc";
        var movieInfo = new MovieInfo("abc", "Welcome Back2",
                2012, List.of("AK"), LocalDate.parse("2012-08-12"));

        webTestClient
                .put()
                .uri(MOVIES_INFO_URL+"/{id}", id)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();

                    assertNotNull(updatedMovieInfo);
                    assertNotNull(updatedMovieInfo.getMovieInfoId());
                    assertEquals("Welcome Back2", updatedMovieInfo.getName());
                });
    }

    @Test
    void updateMovieInfo_notFound() {

        var id = "def";
        var movieInfo = new MovieInfo("abc", "Welcome Back2",
                2012, List.of("AK"), LocalDate.parse("2012-08-12"));

        webTestClient
                .put()
                .uri(MOVIES_INFO_URL+"/{id}", id)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void deleteMovieInfo() {

        var id = "abc";

        webTestClient
                .delete()
                .uri(MOVIES_INFO_URL+"/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}