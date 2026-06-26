package live.hails.hailsdotgo.api

import live.hails.hailsdotgo.data.model.GameDataResponse
import live.hails.hailsdotgo.data.model.IVRequest
import live.hails.hailsdotgo.data.model.IVResponse
import live.hails.hailsdotgo.data.model.PokemonBoxEntry
import live.hails.hailsdotgo.data.model.PokemonBoxResponse
import live.hails.hailsdotgo.data.model.SavePokemonRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface IVService {
    @POST("/api/mobile/v1/iv/calculate")
    suspend fun calculate(@Body request: IVRequest): IVResponse

    @GET("/api/mobile/v1/data")
    suspend fun getGameData(): GameDataResponse

    @GET("/api/mobile/v1/iv/pokemon")
    suspend fun listBox(): PokemonBoxResponse

    @POST("/api/mobile/v1/iv/pokemon")
    suspend fun savePokemon(@Body request: SavePokemonRequest): PokemonBoxEntry

    @DELETE("/api/mobile/v1/iv/pokemon/{id}")
    suspend fun deletePokemon(@Path("id") id: Long): Response<Unit>
}
