package edu.vandy.recommender.movies.server;

import edu.vandy.recommender.movies.common.model.Movie;
import edu.vandy.recommender.movies.utils.FutureUtils;
import jdk.incubator.concurrent.StructuredTaskScope;
import org.apache.tomcat.util.file.Matcher;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This class defines implementation methods that are called by the
 * {@link MoviesController} that to return a {@link List} of objects
 * containing information about movies.
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning.  It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@Service
public class MoviesService {
    /**
     * This auto-wired field connects the {@link MoviesService} to the
     * {@link List} of {@link Movie} objects.
     */
    @Autowired
    List<Movie> mMovies;

    /**
     * @return A {@link List} of all the movies
     */
    public List<Movie> getMovies() {
        return mMovies;
    }

    /**
     * Search for movie titles containing the given query {@link
     * String} using the Java sequential streams framework.
     *
     * @param regexQuery The search query in regular expression form
     * @return A {@link List} of {@link Movie} objects containing the
     *         query
     */
    public List<Movie> search(String regexQuery) {
        return search(List.of(regexQuery));
    }

    /**
     * Search for movie titles containing the given query {@link
     * String} using the Java sequential streams framework.
     *
     * @param regexQueries The search queries in regular expression
     *                     form
     * @return A {@link List} of {@link Movie} objects containing the
     *         queries
     */
    public List<Movie> search(List<String> regexQueries) {

        // Convert the 'regexQueries' into a List of Pattern objects.
        List<Pattern> patternList = makePatternList(regexQueries);

        try (// Create a new StructuredTaskScope that shutdown on
             // failure.
             StructuredTaskScope.ShutdownOnFailure scope =
                     new StructuredTaskScope.ShutdownOnFailure()
             ) {

            // Call a helper method to concurrently get a List of all
            // Movie objects that match the patternList.

            List<Future<List<Movie>>> results = getMatches(patternList, scope);

            // Perform a barrier synchronization that waits for all
            // the tasks to complete.
            scope.join();
            scope.throwIfFailed();

            // Call a helper method that concatenates all matches and
            // returns a List of Movie objects that matched at least
            // one client query.
            return concatMatches(results);
        } catch (Exception exception) {
            System.out.println("Exception: " + exception.getMessage());
            throw new RuntimeException(exception);
        }
    }
    /**
     * Convert the {@link List} of {@code regexQueries} into a {@link
     * List} of compiled regular expression {@link Pattern} objects
     *
     * @param regexQueries The {@link List} of regular expression
     *                     queries
     * @return a {@link List} of compiled regular expression {@link
     *         Pattern} objects
     */
    protected List<Pattern> makePatternList
        (List<String> regexQueries) {

        return regexQueries
                .stream()
                .map(regexQuery -> URLDecoder.decode(regexQuery, StandardCharsets.UTF_8))
                .map(regexQuery -> Pattern.compile(regexQuery, Pattern.CASE_INSENSITIVE))
                .toList();
    }

    /**
     * Concurrently get a {@link List} of all {@link Movie} objects
     * that match the {@code patternList}.
     *
     * @param patternList A {@link List} of queries in compiled
     *                    regular expression form
     * @param scope The {@link StructuredTaskScope} used to {@code
     *              fork()} a virtual thread
     * @return A {@link List} of {@link Future} objects that will emit
     *         a {@link List} of {@link Movie} objects that match
     *         queries in {@code patternList}
     */
    @NotNull
    protected List<Future<List<Movie>>> getMatches
        (List<Pattern> patternList,
         StructuredTaskScope.ShutdownOnFailure scope) {

        return patternList
                .stream()
                .map(pattern -> findMatches(pattern, scope))
                .toList();

    }

    /**
     * Use the Java {@link StructuredTaskScope.ShutdownOnFailure}
     * to concurrently determine if the {@code pattern} matches
     * any {@link Movie} objects.
     *
     * @param pattern The search query in compiled regular expression
     *                form
     * @param scope The {@link StructuredTaskScope} used to {@code
     *              fork()} a virtual thread
     * @return A {@link Future} to an {@link List} of matching {@link
     *         Movie} objects
     */
    protected Future<List<Movie>> findMatches
        (Pattern pattern,
         StructuredTaskScope.ShutdownOnFailure scope) {
        return scope
                .fork(
                        () -> mMovies
                                .stream()
                                .filter(movie -> match(pattern, movie))
                                .toList()
                );
    }

    /**
     * Find a match between the {@code pattern} and the {@code movie}.
     *
     * @param pattern The query in compiled regular expression form
     * @param movie The {@link Movie} to match with
     * @return True if there's a match, else false
     */
    protected boolean match(Pattern pattern,
                          Movie movie) {

        return pattern.matcher(movie.id()).find();
    }

    /**
     * Get a {@link List} of {@link Movie} objects that matched at
     * least one client query.
     *
     * @param results A {@link List} of {@link Future} objects
     *                containing a {@link List} of {@link Movie}
     *                objects that matched client queries
     * @return A {@link List} of matching {@link Movie} objects
     */
    @NotNull
    protected List<Movie> concatMatches
        (List<Future<List<Movie>>> results) {
        // There is no null values otherwise we have to use ofNullable in the flatMap

        return FutureUtils.futures2Stream(results)
                .flatMap(Collection::stream)
                .toList();
    }
}
