package edu.vandy.recommender.movies.client;

import edu.vandy.recommender.movies.common.model.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static edu.vandy.recommender.movies.common.Constants.EndPoint.*;
import static edu.vandy.recommender.movies.common.Constants.EndPoint.Params.QUERIES_PARAM;

/**
 * This class provides proxies to various endpoints in the 'movies'
 * microservice.
 */
@Component
public class MoviesSyncProxy {
    /**
     * The Spring-injected {@link RestTemplate}.
     */
    @Autowired
    private RestTemplate mMoviesRestTemplate;

    /**
     * @return A {@link List} of {@link Movie} objects
     */
    public List<Movie> getMovies() {

        String uri = UriComponentsBuilder
                .fromPath(GET_ALL_MOVIES)
                .build()
                .toUriString();



        return WebUtils.makeGetRequestList(mMoviesRestTemplate, uri, Movie[].class);
    }

    /**
     * Search for movie titles in the database containing the given
     * query {@link String}.
     *
     * @param regex_query The search query in regular expression form
     * @return A {@link List} of {@link Movie} objects that match the
     * query
     */
    public List<Movie> searchMovies(String regex_query) {

        String uri = UriComponentsBuilder
                .fromPath(GET_SEARCH + "/" + WebUtils.encodeQuery(regex_query))
                .build()
                .toUriString();


        return WebUtils.makeGetRequestList(mMoviesRestTemplate, uri, Movie[].class);
    }

    /**
     * Search for movie titles in the database containing the given
     * {@link List} of queries.
     *
     * @param regex_queries The {@link List} queries to search for
     *                      in regular expression form
     * @return A {@link List} of {@link Movie} objects that match the
     * queries
     */
    public List<Movie> searchMovies(List<String> regex_queries) {

        String uri = UriComponentsBuilder
                .fromPath(GET_SEARCHES)
                .queryParam(QUERIES_PARAM, WebUtils.list2String(WebUtils.encodeQueries(regex_queries)))
                .build()
                .toUriString();


        return WebUtils.makeGetRequestList(mMoviesRestTemplate, uri, Movie[].class);
    }
}

