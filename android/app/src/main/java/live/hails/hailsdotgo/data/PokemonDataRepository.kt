package live.hails.hailsdotgo.data

import android.util.Log
import live.hails.hailsdotgo.api.ApiClient
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt

object PokemonDataRepository {
    private const val TAG = "PokemonDataRepository"

    private data class Stats(val attack: Int, val defense: Int, val stamina: Int)

    @Volatile private var pokemonMap: Map<String, Stats> = emptyMap()
    @Volatile private var cpmByLevel: Map<Double, Double> = emptyMap()

    suspend fun refresh() {
        runCatching { ApiClient.iv.getGameData() }
            .onSuccess { data ->
                pokemonMap = data.pokemon
                    .filter { it.pokemonName != null && it.baseAttack != null && it.baseDefense != null && it.baseStamina != null }
                    .associate { p ->
                        p.pokemonName!!.lowercase() to Stats(p.baseAttack!!, p.baseDefense!!, p.baseStamina!!)
                    }
                cpmByLevel = data.cpMultipliers.associate { it.level to it.multiplier }
                Log.d(TAG, "Loaded ${pokemonMap.size} Pokémon, ${cpmByLevel.size} CP multipliers")
            }
            .onFailure { Log.w(TAG, "Failed to load game data: ${it.message}") }
    }

    // Max CP at all-15 IVs and the highest level the trainer can power to (trainerLevel + 2, capped at 51).
    fun maxCP(name: String, trainerLevel: Int): Int? {
        val s   = pokemonMap[name.lowercase()] ?: return null
        val lvl = (trainerLevel + 2).toDouble().coerceAtMost(51.0)
        val cpm = cpmByLevel[lvl]
            ?: cpmByLevel.entries.minByOrNull { abs(it.key - lvl) }?.value
            ?: return null
        val atk = (s.attack  + 15).toDouble()
        val def = (s.defense + 15).toDouble()
        val sta = (s.stamina + 15).toDouble()
        val result = floor(atk * sqrt(def) * sqrt(sta) * cpm * cpm / 10.0).toInt()
        Log.d(TAG, "maxCP('$name', trainerLvl=$trainerLevel→pokeLvl=$lvl): " +
            "atk=${s.attack} def=${s.defense} sta=${s.stamina} cpm=$cpm → $result")
        return result
    }
}
