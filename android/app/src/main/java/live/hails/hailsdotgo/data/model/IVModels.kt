package live.hails.hailsdotgo.data.model

import com.google.gson.annotations.SerializedName

data class IVRequest(
    @SerializedName("pokemon_name") val pokemonName: String,
    val cp: Int,
    val hp: Int,
    @SerializedName("dust_cost") val dustCost: Int,
    @SerializedName("trainer_level") val trainerLevel: Int,
    @SerializedName("top_stat") val topStat: String? = null,
    @SerializedName("appraisal_bars") val appraisalBars: Int? = null,
)

data class IVCandidate(
    @SerializedName("atk_iv") val atkIv: Int,
    @SerializedName("def_iv") val defIv: Int,
    @SerializedName("sta_iv") val staIv: Int,
    val level: Double,
    val cp: Int,
    val hp: Int,
    @SerializedName("iv_pct") val ivPct: Double,
)

data class PokemonInfo(
    val name: String                              = "",
    @SerializedName("pokemon_name") val pokemonName: String? = null,
    @SerializedName("pokemon_id")   val pokemonId  : Int?    = null,
    @SerializedName("base_attack")  val baseAttack : Int?    = null,
    @SerializedName("base_defense") val baseDefense: Int?    = null,
    @SerializedName("base_stamina") val baseStamina: Int?    = null,
    val form       : String?                      = null,
    val types      : List<String>?                = null,
) {
    val displayName: String get() = (pokemonName ?: name).replaceFirstChar { it.uppercaseChar() }
}

data class IVResponse(
    val candidates: List<IVCandidate>? = null,
    val count: Int                     = 0,
    val pokemon: PokemonInfo           = PokemonInfo(),
    val definitive: Boolean            = false,
)

data class SavePokemonRequest(
    @SerializedName("pokemon_name") val pokemonName: String,
    val form: String = "",
    val cp: Int,
    val level: Double,
    @SerializedName("atk_iv") val atkIv: Int,
    @SerializedName("def_iv") val defIv: Int,
    @SerializedName("sta_iv") val staIv: Int,
    @SerializedName("iv_candidates") val ivCandidates: List<IVCandidate>? = null,
    @SerializedName("caught_at") val caughtAt: String,
    val note: String = "",
)

data class PokemonBoxEntry(
    val id: Long,
    @SerializedName("pokemon_name") val pokemonName: String,
    val form: String,
    val cp: Int,
    val level: Double,
    @SerializedName("atk_iv") val atkIv: Int,
    @SerializedName("def_iv") val defIv: Int,
    @SerializedName("sta_iv") val staIv: Int,
    @SerializedName("iv_pct") val ivPct: Double?,
    @SerializedName("caught_at") val caughtAt: String,
    val note: String,
)

data class PokemonBoxResponse(
    val pokemon: List<PokemonBoxEntry>,
    val total: Int,
)

data class CpMultiplier(
    val level: Double,
    val multiplier: Double,
)

data class GameDataResponse(
    val pokemon: List<PokemonInfo> = emptyList(),
    @SerializedName("cpMultipliers") val cpMultipliers: List<CpMultiplier> = emptyList(),
)
