package live.hails.hailsdotgo.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenStore {
    private const val PREFS_FILE        = "hdg_secure_prefs"
    private const val KEY_TOKEN         = "auth_token"
    private const val KEY_USERNAME      = "username"
    private const val KEY_ROLE          = "role"
    private const val KEY_TRAINER_LEVEL = "trainer_level"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun saveToken(token: String)          = prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
    fun loadToken(): String?              = prefs?.getString(KEY_TOKEN, null)
    fun saveUsername(name: String)        = prefs?.edit()?.putString(KEY_USERNAME, name)?.apply()
    fun loadUsername(): String?           = prefs?.getString(KEY_USERNAME, null)
    fun saveRole(role: String)            = prefs?.edit()?.putString(KEY_ROLE, role)?.apply()
    fun loadRole(): String?               = prefs?.getString(KEY_ROLE, null)
    fun saveTrainerLevel(level: Int)      = prefs?.edit()?.putInt(KEY_TRAINER_LEVEL, level)?.apply()
    fun loadTrainerLevel(): Int           = (prefs?.getInt(KEY_TRAINER_LEVEL, 0) ?: 0).let { if (it == 0) 40 else it }
    fun clear()                           = prefs?.edit()?.clear()?.apply()
}
