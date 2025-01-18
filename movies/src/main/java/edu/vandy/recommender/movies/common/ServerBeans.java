package edu.vandy.recommender.movies.common;

import edu.vandy.recommender.movies.common.model.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
import java.util.Map;

/**
 * This class contains a {@code Bean} annotation that can be injected
 * into classes using the Spring {@code @Autowired} annotation.
 */
@Configuration
@PropertySource("classpath:application.yml")
public class ServerBeans {
    /**
     * Loads the {@code dataset} and returns a {@link Map} of {@link
     * String} and {@link List<Double>} objects.
     *
     * @param dataset The name of the dataset containing movie-related
     *                data
     * @return A {@link Map} of {@link String} and {@link List<Double>} objects
     */
    @Bean
    public Map<String, List<Double>> movieMap
    (@Value("${app.dataset}") final String dataset) {
        return MovieDatasetReader
            .loadMovieData(dataset);
    }

    /**
     * Loads the {@code dataset} and returns a {@link List} of {@link
     * Movie} objects.
     *
     * @param dataset The name of the dataset containing movie-related
     *                data
     * @return A {@link List} of {@link Movie} objects
     */
    @Bean
    public List<Movie> movieList
        (@Value("${app.dataset}") final String dataset) {

        return MovieDatasetReader
                .loadMovieData(dataset)
                .entrySet()
                .stream()
                .map(movie -> new Movie(movie.getKey(), movie.getValue()))
                .toList();
    }
}
