package net.collectn.tools.movies

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.subcommands
import java.io.File

const val TMDB_API_KEY_ENV_VAR_NAME: String = "COLLECTN_TOOL_TMDB_API_KEY"

class BaseCommand : CliktCommand(hidden = true, name = "api") {
    override fun run() = Unit
}

fun main(args: Array<String>) =
    BaseCommand()
        .subcommands(ConvertTitlesCsvToMoviesCsv(), ConvertTitlesToMovies(), UpdateMoviesCsvWithNewTitles())
        .main(args)

internal fun File.requireExistsAsFile(): File {
    if (!exists() || !isFile) throw PrintMessage("$name does not exist or is not a file")
    return this
}
