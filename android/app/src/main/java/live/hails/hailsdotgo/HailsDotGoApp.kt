package live.hails.hailsdotgo

import android.app.Application
import live.hails.hailsdotgo.data.AuthRepository
import live.hails.hailsdotgo.data.TokenStore
import live.hails.hailsdotgo.util.createNotificationChannels

class HailsDotGoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)
        AuthRepository.restoreSession()
        createNotificationChannels(this)
    }
}
