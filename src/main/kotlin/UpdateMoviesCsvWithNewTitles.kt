package net.collectn.tools.movies

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import info.movito.themoviedbapi.TmdbApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UpdateMoviesCsvWithNewTitles : CliktCommand() {
    private val tmdbApiKey by option(help = "TMDB API Key", envvar = TMDB_API_KEY_ENV_VAR_NAME).required()
    private val titles: List<String> by option(
        "-t",
        "--title",
        help = "Movie Title",
    ).multiple()
    private val outputJsonToStdOut: Boolean by option("--output-json", help = "Prints JSON of movies added or updated to STDOUT")
        .flag("--no-output-json", default = false)
    private val includeLogging: Boolean by option("--include-logging", help = "Include logging output")
        .flag("--quiet", "-q", default = true)

    override fun run() {
        updateMoviesCsvWithNewTitles(TmdbApi(tmdbApiKey), titles, outputJsonToStdOut, includeLogging)
    }
}

internal fun updateMoviesCsvWithNewTitles(
    tmdbApi: TmdbApi,
    titles: List<String>,
    outputJsonToStdOut: Boolean,
    includeLogging: Boolean,
): MoviesCsvUpdateResult {
    val movieByTmdbId = readMoviesFromCsv().associateBy { it.tmdbId }.toMutableMap()
    val moviesUpdated = mutableListOf<Movie>()
    val moviesAdded = mutableListOf<Movie>()
    convertTitlesToMovies(
        tmdbApi = tmdbApi,
        titles = titles,
        outputJsonToStdOut = false, // no need to output json from here
        includeLogging = includeLogging,
    ).forEach { movie ->
        if (movieByTmdbId.containsKey(movie.tmdbId)) {
            moviesUpdated.add(movie)
        } else {
            moviesAdded.add(movie)
        }
        movieByTmdbId[movie.tmdbId] = movie
    }
    val rows = mutableListOf(Movie.getCsvRowHeaders())
    movieByTmdbId.values.sortedBy { it.title }.mapTo(rows) { it.toRow() }
    writeCsvMovies(rows)
    val result = MoviesCsvUpdateResult(moviesUpdated = moviesUpdated.toList(), moviesAdded = moviesAdded.toList())
    if (outputJsonToStdOut) {
        println(Json.encodeToString(result))
    }
    return result
}

@Serializable
internal data class MoviesCsvUpdateResult(
    val moviesUpdated: List<Movie>,
    val moviesAdded: List<Movie>,
)
