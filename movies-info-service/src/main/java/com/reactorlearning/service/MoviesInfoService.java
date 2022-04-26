package com.reactorlearning.service;

import com.reactorlearning.domain.MovieInfo;
import com.reactorlearning.repository.MovieInfoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MoviesInfoService {

    private MovieInfoRepository movieInfoRepository;

    public MoviesInfoService(MovieInfoRepository movieInfoRepository) {
        this.movieInfoRepository = movieInfoRepository;
    }

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {

        return movieInfoRepository.save(movieInfo);
    }

    public Flux<MovieInfo> getAllMovieInfos() {

        return movieInfoRepository.findAll();
    }

    public Mono<MovieInfo> getMovieInfoById(String id) {

        return movieInfoRepository.findById(id);
    }

    public Mono<MovieInfo> updateMovieInfo(MovieInfo updatedMovieInfo, String id) {

        return movieInfoRepository.findById(id)
                .flatMap(movieInfo -> {
                    movieInfo.setName(updatedMovieInfo.getName());
                    movieInfo.setCast(updatedMovieInfo.getCast());
                    movieInfo.setYear(updatedMovieInfo.getYear());
                    movieInfo.setRelease_date(updatedMovieInfo.getRelease_date());
                    return movieInfoRepository.save(movieInfo);
                });
    }

    public Mono<Void> deleteMovieInfo(String id) {

        return movieInfoRepository.deleteById(id);
    }

    public Flux<MovieInfo> getMovieInfoByYear(Integer year) {

        return movieInfoRepository.findByYear(year);
    }

    public Mono<MovieInfo> getMovieInfoByName(String name) {

        return movieInfoRepository.findByName(name);
    }
}
