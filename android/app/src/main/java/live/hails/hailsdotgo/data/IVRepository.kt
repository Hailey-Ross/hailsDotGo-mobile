package live.hails.hailsdotgo.data

import com.google.gson.Gson
import live.hails.hailsdotgo.api.ApiClient
import live.hails.hailsdotgo.data.model.IVRequest
import live.hails.hailsdotgo.data.model.IVResponse
import live.hails.hailsdotgo.data.model.PokemonBoxEntry
import live.hails.hailsdotgo.data.model.PokemonBoxResponse
import live.hails.hailsdotgo.data.model.SavePokemonRequest
import java.time.Instant

object IVRepository {
    private val gson = Gson()

    suspend fun calculate(request: IVRequest): Result<IVResponse> = runCatching {
        ApiClient.iv.calculate(request)
    }.recoverCatching { e ->
        if (e is retrofit2.HttpException) {
            val body = e.response()?.errorBody()?.string()
            val msg = try { gson.fromJson(body, ApiError::class.java).error } catch (_: Exception) { null }
            throw Exception(msg ?: "Server error (${e.code()})")
        }
        throw e
    }

    private data class ApiError(val error: String?)

    suspend fun listBox(): Result<PokemonBoxResponse> = runCatching {
        ApiClient.iv.listBox()
    }

    suspend fun savePokemon(request: SavePokemonRequest): Result<PokemonBoxEntry> = runCatching {
        ApiClient.iv.savePokemon(request)
    }

    suspend fun deletePokemon(id: Long): Result<Unit> = runCatching {
        ApiClient.iv.deletePokemon(id)
        Unit
    }

    fun buildSaveRequest(entry: PokemonBoxEntry, candidates: IVResponse): SavePokemonRequest =
        SavePokemonRequest(
            pokemonName  = entry.pokemonName,
            form         = entry.form,
            cp           = entry.cp,
            level        = entry.level,
            atkIv        = entry.atkIv,
            defIv        = entry.defIv,
            staIv        = entry.staIv,
            ivCandidates = candidates.candidates,
            caughtAt     = Instant.now().toString(),
        )
}
