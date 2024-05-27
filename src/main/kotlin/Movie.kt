package net.collectn.tools.movies

import info.movito.themoviedbapi.model.MovieDb
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.collectn.tools.movies.Movie.Collection
import net.collectn.tools.movies.Movie.ProductionCompany
import net.collectn.tools.movies.Movie.ProductionCountry
import net.collectn.tools.movies.Movie.SpokenLanguage
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

@Serializable
data class Movie(
    val title: String,
    val originalTitle: String,
    val originalLanguage: String,
    val tmdbId: String,
    val imdbId: String,
    val runtime: Int,
    val genres: List<String>,
    @Serializable(with = LocalDateIso8601Serializer::class)
    val releaseDate: LocalDate,
    val revenue: Long,
    val budget: Long,
    val status: String,
    val tagline: String,
    val overview: String,
    val productionCompanies: List<ProductionCompany>,
    val productionCountries: List<ProductionCountry>,
    val spokenLanguages: List<SpokenLanguage>,
    val belongsToCollection: Collection?,
    val isVideo: Boolean,
    val isAdult: Boolean,
) {
    @Serializable
    data class ProductionCompany(
        val name: String,
        val originCountry: String,
    )

    @Serializable
    data class ProductionCountry(
        val name: String,
        val iso31661: String,
    )

    @Serializable
    data class SpokenLanguage(
        val name: String,
        val iso31661: String,
    )

    @Serializable
    data class Collection(
        val name: String,
        val title: String,
        @Serializable(with = LocalDateIso8601Serializer::class)
        val releaseDate: LocalDate?,
    )

    companion object {
        @JvmStatic
        fun getCsvRowHeaders(): List<String> = CsvRowHeaders.entries.sortedBy { it.ordinal }.map { it.header }

        @JvmStatic
        fun getOrdinalOfHeader(header: String): Int = CsvRowHeaders.valueOf(header).ordinal
    }
}

const val TITLE_HEADER: String = "Title"

private enum class CsvRowHeaders(val header: String) {
    TITLE(TITLE_HEADER),
    ORIGINAL_TITLE("Original Title"),
    ORIGINAL_LANGUAGE("Original Language"),
    TMDB_ID("TMDB ID"),
    IMDB_ID("IMDB ID"),
    RUNTIME("Runtime"),
    GENRES("Genre(s)"),
    RELEASE_DATE("Release Date"),
    REVENUE("Revenue"),
    BUDGET("Budget"),
    STATUS("Status"),
    TAGLINE("Tagline"),
    OVERVIEW("Overview"),
    PRODUCTION_COMPANIES("Production Companies"),
    PRODUCTION_COUNTRIES("Production Countries"),
    SPOKEN_LANGUAGES("Spoken Language(s)"),
    COLLECTION_TITLE("Collection Title"),
    COLLECTION_NAME("Collection Name"),
    COLLECTION_RELEASE_DATE("Collection Release Date"),
    IS_VIDEO("Is Video"),
    IS_ADULT("Is Adult"),
}

fun MovieDb.toMovie(): Movie {
    return Movie(
        title = title,
        originalTitle = originalTitle,
        originalLanguage = originalLanguage,
        tmdbId = id.toString(),
        imdbId = imdbID.orEmpty(),
        runtime = runtime,
        genres = genres?.map { it.name } ?: emptyList(),
        releaseDate = releaseDate?.let { LocalDate.parse(it) } ?: error("invalid date(movie: $title): $releaseDate"),
        revenue = revenue,
        budget = budget,
        status = status.orEmpty(),
        tagline = tagline.orEmpty(),
        overview = overview.orEmpty(),
        productionCompanies =
            productionCompanies
                ?.map { productionCompany ->
                    ProductionCompany(
                        name = productionCompany.name,
                        originCountry = productionCompany.originCountry,
                    )
                } ?: emptyList(),
        productionCountries =
            productionCountries
                ?.map { productionCountry ->
                    ProductionCountry(
                        name = productionCountry.name,
                        iso31661 = productionCountry.isoCode,
                    )
                } ?: emptyList(),
        spokenLanguages =
            spokenLanguages
                ?.map { spokenLanguage ->
                    SpokenLanguage(
                        name = spokenLanguage.name,
                        iso31661 = spokenLanguage.isoCode,
                    )
                } ?: emptyList(),
        belongsToCollection =
            belongsToCollection
                ?.let { collection ->
                    Collection(
                        name = collection.name,
                        title = collection.title,
                        releaseDate = collection.releaseDate?.let { LocalDate.parse(it) },
                    )
                },
        isVideo =
            videos?.onEach { video ->
                logger.debug { "$title has video: ${video.name} (${video.type})" }
            }?.isNotEmpty() == true,
        isAdult = isAdult,
    )
}

fun Movie.toRow(): List<String> {
    val row = mutableListOf<String>()
    CsvRowHeaders.entries.sortedBy { it.ordinal }.forEach { header ->
        when (header) {
            CsvRowHeaders.TITLE -> row.add(title)
            CsvRowHeaders.ORIGINAL_TITLE -> row.add(originalTitle)
            CsvRowHeaders.ORIGINAL_LANGUAGE -> row.add(originalLanguage)
            CsvRowHeaders.TMDB_ID -> row.add(tmdbId)
            CsvRowHeaders.IMDB_ID -> row.add(imdbId)
            CsvRowHeaders.RUNTIME -> row.add(runtime.toString())
            CsvRowHeaders.GENRES -> row.add(Json.encodeToString(genres))
            CsvRowHeaders.RELEASE_DATE -> row.add(DateTimeFormatter.ISO_LOCAL_DATE.format(releaseDate))
            CsvRowHeaders.REVENUE -> row.add(revenue.toString())
            CsvRowHeaders.BUDGET -> row.add(budget.toString())
            CsvRowHeaders.STATUS -> row.add(status)
            CsvRowHeaders.TAGLINE -> row.add(tagline)
            CsvRowHeaders.OVERVIEW -> row.add(overview)
            CsvRowHeaders.PRODUCTION_COMPANIES -> row.add(Json.encodeToString(productionCompanies))
            CsvRowHeaders.PRODUCTION_COUNTRIES -> row.add(Json.encodeToString(productionCountries))
            CsvRowHeaders.SPOKEN_LANGUAGES -> row.add(Json.encodeToString(spokenLanguages))
            CsvRowHeaders.COLLECTION_TITLE -> row.add(belongsToCollection?.title.orEmpty())
            CsvRowHeaders.COLLECTION_NAME -> row.add(belongsToCollection?.name.orEmpty())
            CsvRowHeaders.IS_VIDEO -> row.add(isVideo.toString())
            CsvRowHeaders.IS_ADULT -> row.add(isAdult.toString())
            CsvRowHeaders.COLLECTION_RELEASE_DATE ->
                row.add(
                    belongsToCollection?.releaseDate?.let {
                        DateTimeFormatter.ISO_LOCAL_DATE.format(it)
                    }.orEmpty(),
                )
        }
    }
    return row.toList()
}

fun movieFromRow(row: Map<String, String>): Movie {
    lateinit var title: String
    lateinit var originalTitle: String
    lateinit var originalLanguage: String
    lateinit var tmdbId: String
    lateinit var imdbId: String
    var runtime: Int? = null
    lateinit var genres: List<String>
    lateinit var releaseDate: LocalDate
    var revenue: Long? = null
    var budget: Long? = null
    lateinit var status: String
    lateinit var tagline: String
    lateinit var overview: String
    lateinit var productionCompanies: List<ProductionCompany>
    lateinit var productionCountries: List<ProductionCountry>
    lateinit var spokenLanguages: List<SpokenLanguage>
    var isVideo: Boolean? = null
    var isAdult: Boolean? = null
    var collectionTitle: String? = null
    var collectionName: String? = null
    var collectionReleaseDate: LocalDate? = null
    CsvRowHeaders.entries.forEach { entry ->
        when (entry) {
            CsvRowHeaders.TITLE -> {
                title = row[entry.header] ?: error("no ${entry.header}")
            }

            CsvRowHeaders.ORIGINAL_TITLE -> {
                originalTitle = row[entry.header] ?: error("no ${entry.header}")
            }

            CsvRowHeaders.ORIGINAL_LANGUAGE -> {
                originalLanguage = row[entry.header] ?: error("no ${entry.header}")
            }

            CsvRowHeaders.TMDB_ID -> {
                tmdbId = row[entry.header] ?: error("no ${entry.header}")
            }

            CsvRowHeaders.IMDB_ID -> {
                imdbId = row[entry.header] ?: error("no ${entry.header}")
            }

            CsvRowHeaders.RUNTIME -> {
                runtime = row[entry.header]?.toInt() ?: error("no ${entry.header}")
            }

            CsvRowHeaders.GENRES -> {
                genres = row[entry.header]?.let { it.ifBlank { "[]" } }?.let { Json.decodeFromString(it) }
                    ?: error("no ${entry.header}")
            }

            CsvRowHeaders.RELEASE_DATE -> {
                releaseDate = row[entry.header]?.let { LocalDate.parse(it) } ?: error("no ${entry.header}")
            }

            CsvRowHeaders.REVENUE -> {
                revenue = row[entry.header]?.toLong() ?: error("no ${entry.header}")
            }

            CsvRowHeaders.BUDGET -> {
                budget = row[entry.header]?.toLong() ?: error("no ${entry.header}")
            }

            CsvRowHeaders.STATUS -> {
                status = row[entry.header] ?: error("no ${entry.header}")
            }

            CsvRowHeaders.TAGLINE -> {
                tagline = row[entry.header] ?: error("no ${entry.header}")
            }

            CsvRowHeaders.OVERVIEW -> {
                overview = row[entry.header] ?: error("no ${entry.header}")
            }

            CsvRowHeaders.PRODUCTION_COMPANIES -> {
                productionCompanies = row[entry.header]?.let { it.ifBlank { "[]" } }?.let { Json.decodeFromString(it) }
                    ?: error("no ${entry.header}")
            }

            CsvRowHeaders.PRODUCTION_COUNTRIES -> {
                productionCountries = row[entry.header]?.let { it.ifBlank { "[]" } }?.let { Json.decodeFromString(it) }
                    ?: error("no ${entry.header}")
            }

            CsvRowHeaders.SPOKEN_LANGUAGES -> {
                spokenLanguages = row[entry.header]?.let { it.ifBlank { "[]" } }?.let { Json.decodeFromString(it) }
                    ?: error("no ${entry.header}")
            }

            CsvRowHeaders.IS_VIDEO -> {
                isVideo = row[entry.header]?.toBoolean() ?: error("no ${entry.header}")
            }

            CsvRowHeaders.IS_ADULT -> {
                isAdult = row[entry.header]?.toBoolean() ?: error("no ${entry.header}")
            }

            CsvRowHeaders.COLLECTION_TITLE -> {
                collectionTitle = row[entry.header]?.let { it.ifBlank { null } }
            }

            CsvRowHeaders.COLLECTION_NAME -> {
                collectionName = row[entry.header]?.let { it.ifBlank { null } }
            }

            CsvRowHeaders.COLLECTION_RELEASE_DATE -> {
                collectionReleaseDate =
                    row[entry.header]
                        ?.let { it.ifBlank { null } }
                        ?.let { LocalDate.parse(it) }
            }
        }
    }
    val belongsToCollection =
        if (collectionTitle != null || collectionName != null || collectionReleaseDate != null) {
            Collection(
                title = collectionTitle.orEmpty(),
                name = collectionName.orEmpty(),
                releaseDate = collectionReleaseDate,
            )
        } else {
            null
        }
    return Movie(
        title = title,
        originalTitle = originalTitle,
        originalLanguage = originalLanguage,
        tmdbId = tmdbId,
        imdbId = imdbId,
        runtime = runtime ?: error("no runtime value"),
        genres = genres,
        releaseDate = releaseDate,
        revenue = revenue ?: error("no revenue value"),
        budget = budget ?: error("no budget value"),
        status = status,
        tagline = tagline,
        overview = overview,
        productionCompanies = productionCompanies,
        productionCountries = productionCountries,
        spokenLanguages = spokenLanguages,
        belongsToCollection = belongsToCollection,
        isVideo = isVideo ?: error("no isVideo value"),
        isAdult = isAdult ?: error("no isAdult value"),
    )
}
