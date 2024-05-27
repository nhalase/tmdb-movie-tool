package net.collectn.tools.movies

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File

val csvWriter =
    csvWriter {
        lineTerminator = "\n"
        outputLastLineTerminator = false
        charset = Charsets.UTF_8.name()
    }

val csvReader =
    csvReader {
        charset = Charsets.UTF_8.name()
    }

internal const val MOVIE_TITLES_CSV: String = "movie_titles.csv"

internal fun readTitlesFromCsv(filename: String = MOVIE_TITLES_CSV): List<String> {
    return csvReader.readAllWithHeader(File(filename).requireExistsAsFile())
        .map { it[TITLE_HEADER]!! }
        .distinct()
        .sorted()
}

internal const val MOVIES_CSV: String = "movies.csv"

internal fun writeCsvMovies(
    rows: List<List<String>>,
    filename: String = MOVIES_CSV,
) {
    csvWriter.writeAll(rows, File(filename).outputStream())
}

internal fun readMoviesFromCsv(filename: String = MOVIES_CSV): List<Movie> {
    return csvReader.readAllWithHeader(File(filename).requireExistsAsFile()).map { movieFromRow(it) }
}
