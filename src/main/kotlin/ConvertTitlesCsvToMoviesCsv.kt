package net.collectn.tools.movies

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import info.movito.themoviedbapi.TmdbApi
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

class ConvertTitlesCsvToMoviesCsv : CliktCommand() {
    private val tmdbApiKey by option(help = "TMDB API Key", envvar = TMDB_API_KEY_ENV_VAR_NAME).required()
    private val outputJsonToStdOut by option("--output-json", help = "Prints JSON array of movies to STDOUT")
        .flag("--no-output-json", default = false)
    private val includeLogging: Boolean by option("--include-logging", help = "Include logging output")
        .flag("--quiet", "-q", default = true)

    override fun run() {
        convertTitlesCsvToMoviesCsv(TmdbApi(tmdbApiKey), outputJsonToStdOut, includeLogging)
    }
}

/**
 * Writes-over existing CSV!
 */
internal fun convertTitlesCsvToMoviesCsv(
    tmdbApi: TmdbApi,
    outputJsonToStdOut: Boolean,
    includeLogging: Boolean,
) {
    val rows: MutableList<List<String>> = mutableListOf()
    rows.add(Movie.getCsvRowHeaders())
    val titles = readTitlesFromCsv()
    val movies =
        titles.map { title ->
            val movie = tmdbApi.getMovieByTitle(title, includeLogging)
            logger.debug { movie.toString() }
            rows.add(movie.toRow())
            movie
        }
    writeCsvMovies(rows)
    if (outputJsonToStdOut) {
        println(Json.encodeToString(movies))
    }
}
