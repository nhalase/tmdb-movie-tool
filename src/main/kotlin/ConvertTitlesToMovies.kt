package net.collectn.tools.movies

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import info.movito.themoviedbapi.TmdbApi
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

class ConvertTitlesToMovies : CliktCommand() {
    private val tmdbApiKey by option(help = "TMDB API Key", envvar = TMDB_API_KEY_ENV_VAR_NAME).required()
    private val titles: List<String> by option(
        "-t",
        "--title",
        help = "Movie Title",
    ).multiple()
    private val outputJsonToStdOut: Boolean by option("--output-json", help = "Prints JSON array of movies to STDOUT")
        .flag("--no-output-json", default = false)
    private val includeLogging: Boolean by option("--include-logging", help = "Include logging output")
        .flag("--quiet", "-q", default = true)

    override fun run() {
        convertTitlesToMovies(
            tmdbApi = TmdbApi(tmdbApiKey),
            titles = titles,
            outputJsonToStdOut = outputJsonToStdOut,
            includeLogging = includeLogging,
        )
    }
}

internal fun convertTitlesToMovies(
    tmdbApi: TmdbApi,
    titles: List<String>,
    outputJsonToStdOut: Boolean,
    includeLogging: Boolean,
): List<Movie> {
    val movies: MutableList<Movie> = mutableListOf()
    titles.forEach { title ->
        val movie = tmdbApi.getMovieByTitle(title, includeLogging)
        if (includeLogging) {
            logger.debug { movie.toString() }
        }
        movies.add(movie)
    }
    if (outputJsonToStdOut) {
        println(Json.encodeToString(movies))
    }
    return movies.toList()
}
