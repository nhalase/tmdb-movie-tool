@file:Suppress("ktlint:standard:filename")

package net.collectn.tools.movies

import info.movito.themoviedbapi.TmdbApi
import info.movito.themoviedbapi.model.core.MovieResultsPage

internal const val DEFAULT_TMDB_LANG: String = "en-US"

internal fun TmdbApi.searchMovies(
    query: String,
    searchYear: Int? = null,
    language: String? = DEFAULT_TMDB_LANG,
    includeAdult: Boolean = false,
    page: Int? = null,
): MovieResultsPage {
    return search.searchMovie(query, searchYear, language, includeAdult, page)
}
