@file:Suppress("ktlint:standard:filename")

package net.collectn.tools.movies

import com.github.ajalt.clikt.core.PrintMessage
import info.movito.themoviedbapi.TmdbApi
import info.movito.themoviedbapi.model.core.MovieResultsPage
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

internal fun TmdbApi.getMovieByTitle(
    title: String,
    includeLogging: Boolean,
): Movie {
    if (includeLogging) {
        logger.info { "Fetching TMDB movie details for \"$title\"" }
    }
    val moviesResultPage = searchMovies(title)
    val tmdbId =
        if (moviesResultPage.totalResults != 1) {
            if (includeLogging) {
                logger.warn { "TMDB search for \"$title\" resulted in ${moviesResultPage.totalResults} results" }
            }
            moviesResultPage.parseDesiredMovie(title, includeLogging)
        } else {
            moviesResultPage.results[0]!!.id
        }
    return movies.getMovie(tmdbId, DEFAULT_TMDB_LANG).toMovie()
}

private fun MovieResultsPage.parseDesiredMovie(
    title: String,
    includeLogging: Boolean,
): Int {
    val tmdbId =
        when (title) {
            "2 Fast 2 Furious" -> 584
            "300" -> 1271
            "A Clockwork Orange" -> 185
            "A View to a Kill" -> 707
            "Akira" -> 149
            "Alien" -> 348
            "Alien: Resurrection" -> 8078
            "Aliens" -> 679
            "Arena" -> 44796
            "Barbarella" -> 8069
            "Barbie" -> 346698
            "Batman" -> 268
            "Blade" -> 36647
            "Brazil" -> 68
            "Casino Royale" -> 36557
            "Charade" -> 4808
            "City Hunter" -> 11198
            "Clue" -> 15196
            "Crime Story" -> 18857
            "Die Hard" -> 562
            "Django" -> 10772
            "Dolls" -> 24341
            "Dragon Lord" -> 16407
            "Dune (2021)" -> 438631
            "RoboCop (1987)" -> 5548
            "Space Jam (1996)" -> 2300
            "Total Recall (1990)" -> 861
            "Elf" -> 10719
            "Escape from New York" -> 1103
            "Fargo" -> 275
            "Fight Club" -> 550
            "Flash Gordon" -> 3604
            "For Your Eyes Only" -> 699
            "Ghostbusters" -> 620
            "Godzilla" -> 124905
            "Grease" -> 621
            "Hackers" -> 10428
            "Her" -> 152601
            "Home Alone" -> 771
            "Joker" -> 475557
            "Kill Bill: Vol 1" -> 24
            "King Kong" -> 254
            "Labyrinth" -> 13597
            "License to Kill" -> 709
            "Man of Steel" -> 49521
            "Mean Girls" -> 10625
            "Mission: Impossible" -> 954
            "Moonraker" -> 698
            "Mortal Kombat (1995)" -> 9312
            "Mortal Kombat (2021)" -> 460465
            "Office Space" -> 1542
            "Operation Condor" -> 10975
            "Pitch Black" -> 2787
            "Prometheus" -> 70981
            "Reservoir Dogs" -> 500
            "Resident Evil" -> 1576
            "Scarface" -> 111
            "Scrooged" -> 9647
            "Skyscraper" -> 447200
            "Snatch" -> 107
            "Spawn" -> 10336
            "Spectre" -> 206647
            "Speed" -> 1637
            "Stargate" -> 2164
            "The Bourne Identity" -> 2501
            "The Fast and the Furious" -> 9799
            "The Fugitive" -> 5503
            "The Godfather" -> 238
            "The Imitation Game" -> 205596
            "The Maltese Falcon" -> 963
            "The Mummy" -> 564
            "The Protector" -> 45408
            "The Running Man" -> 865
            "The Shining" -> 694
            "The Thing" -> 1091
            "They Live" -> 8337
            "Titanic" -> 597
            "Top Gun" -> 744
            "Torso" -> 31624
            "Tremors" -> 9362
            "Twinkle Twinkle Lucky Stars" -> 18707
            "Vertigo" -> 426
            "Watchmen" -> 13183
            "Weird Science" -> 11814
            "Weird: The Al Yankovic Story" -> 928344
            "Wonder Woman" -> 297762
            "You Only Live Twice" -> 667
            "Super Mario Bros." -> 9607
            "Ghostbusters: Frozen Empire" -> 967847
            else -> {
                val movie = results.singleOrNull { it.title == title }
                movie?.id ?: throw PrintMessage("Unhandled ambiguous movie title: $title")
            }
        }
    if (includeLogging) {
        logger.info { "Selected TMDB ID $tmdbId for \"$title\"" }
    }
    return tmdbId
}
