package dev.iosfeel.sonora.core.media.mapper

import android.net.Uri
import androidx.media3.common.Player
import dev.iosfeel.sonora.core.model.RepeatMode
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.core.model.toDomainRepeatMode
import dev.iosfeel.sonora.core.model.toMedia3
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SongMediaItemMapperTest {

    @Test
    fun repeatModeConversionsMatchMedia3Constants() {
        assertEquals(Player.REPEAT_MODE_OFF, RepeatMode.Off.toMedia3())
        assertEquals(Player.REPEAT_MODE_ALL, RepeatMode.All.toMedia3())
        assertEquals(Player.REPEAT_MODE_ONE, RepeatMode.One.toMedia3())

        assertEquals(RepeatMode.Off, Player.REPEAT_MODE_OFF.toDomainRepeatMode())
        assertEquals(RepeatMode.All, Player.REPEAT_MODE_ALL.toDomainRepeatMode())
        assertEquals(RepeatMode.One, Player.REPEAT_MODE_ONE.toDomainRepeatMode())
    }
}
