package com.reactorlearning.controller;

import com.reactorlearning.domain.MovieInfo;
import com.reactorlearning.service.MoviesInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
public class MoviesInfoController {

    private MoviesInfoService moviesInfoService;

    public MoviesInfoController(MoviesInfoService moviesInfoService) {
        this.moviesInfoService = moviesInfoService;
    }

    @GetMapping("/movieinfos")
    public Flux<MovieInfo> getAllMovieInfos(@RequestParam(value = "year", required = false) Integer year) {

        if(year != null) {
            return moviesInfoService.getMovieInfoByYear(year).log();
        }
        return moviesInfoService.getAllMovieInfos().log();
    }

    @GetMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable String id) {

        return moviesInfoService.getMovieInfoById(id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @PostMapping("/movieinfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {

        return moviesInfoService.addMovieInfo(movieInfo).log();
    }

    @PutMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody MovieInfo updatedMovieInfo,
                                                          @PathVariable String id) {

        return moviesInfoService.updateMovieInfo(updatedMovieInfo, id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @DeleteMapping("/movieinfos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfo(@PathVariable String id) {

        return moviesInfoService.deleteMovieInfo(id);
    }
}
