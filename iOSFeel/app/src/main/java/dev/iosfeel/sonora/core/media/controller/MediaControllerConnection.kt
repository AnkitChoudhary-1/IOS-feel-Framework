package dev.iosfeel.sonora.core.media.controller

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import dev.iosfeel.sonora.core.media.service.SonoraPlaybackService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MediaControllerConnection(
    private val context: Context
) {
    private var controller: MediaController? = null

    val isConnected: Boolean
        get() = controller != null

    suspend fun connect(): MediaController {
        controller?.let { return it }

        val token = SessionToken(
            context,
            ComponentName(context, SonoraPlaybackService::class.java)
        )

        val future: ListenableFuture<MediaController> = MediaController.Builder(context, token)
            .buildAsync()

        return future.await().also {
            controller = it
        }
    }

    fun release() {
        controller?.release()
        controller = null
    }
}

private suspend fun <T> ListenableFuture<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addListener(
        {
            try {
                continuation.resume(get())
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        },
        Dispatchers.Main.asExecutor()
    )

    continuation.invokeOnCancellation {
        cancel(true)
    }
}
