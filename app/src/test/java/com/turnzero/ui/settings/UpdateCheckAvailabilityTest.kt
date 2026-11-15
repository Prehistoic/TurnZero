package com.turnzero.ui.settings

import com.turnzero.update.UpdateManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckAvailabilityTest {

    @Test
    fun `idle status allows a check`() {
        assertTrue(canCheckForUpdates(UpdateManager.UpdateStatus.Idle))
    }

    @Test
    fun `checking status blocks a check`() {
        assertFalse(canCheckForUpdates(UpdateManager.UpdateStatus.Checking))
    }

    @Test
    fun `downloading status blocks a check`() {
        assertFalse(canCheckForUpdates(UpdateManager.UpdateStatus.Downloading(42)))
    }

    @Test
    fun `found update allows a check`() {
        assertTrue(canCheckForUpdates(UpdateManager.UpdateStatus.UpdateAvailable("0.21.1", "")))
    }

    @Test
    fun `ready to install allows a check`() {
        assertTrue(canCheckForUpdates(UpdateManager.UpdateStatus.ReadyToInstall(java.io.File("x"))))
    }
}
