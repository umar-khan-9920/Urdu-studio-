package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ProjectStorage
import com.example.data.StudioPresets
import com.example.urdu.PhoneticUrduKeyboard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Urdu Studio`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Urdu Studio", appName)
    }

    @Test
    fun `json serialization roundtrip preserves preset and layers`() {
        val starter = StudioPresets.createStarterDocument()
        val json = ProjectStorage.toJson(starter)
        val restored = ProjectStorage.fromJson(json)
        assertEquals(starter.preset, restored.preset)
        assertEquals(starter.layers.size, restored.layers.size)
        assertEquals(starter.layers.first().name, restored.layers.first().name)
    }

    @Test
    fun `phonetic keyboard maps english chars to urdu`() {
        val mapped = PhoneticUrduKeyboard.transliterateDelta("", "ardw")
        assertEquals("اردو", mapped)
        assertTrue(PhoneticUrduKeyboard.row1.isNotEmpty())
    }
}
