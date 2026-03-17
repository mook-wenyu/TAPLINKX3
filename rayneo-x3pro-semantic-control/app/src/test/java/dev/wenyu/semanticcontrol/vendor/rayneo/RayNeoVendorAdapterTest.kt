package dev.wenyu.semanticcontrol.vendor.rayneo

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RayNeoVendorAdapterTest {

    @Test
    fun `bootstrap initializes runtime once`() {
        val runtime = RecordingMercuryRuntime()
        val adapter = RayNeoVendorAdapter(runtime)

        val result = adapter.bootstrap(FakeApplication())

        assertEquals(BootstrapResult.Initialized, result)
        assertEquals(1, runtime.initCallCount)
    }

    @Test
    fun `bootstrap captures runtime failure without crashing`() {
        val runtime = FailingMercuryRuntime(IllegalStateException("boom"))
        val adapter = RayNeoVendorAdapter(runtime)

        val result = adapter.bootstrap(FakeApplication())

        assertTrue(result is BootstrapResult.Failed)
        result as BootstrapResult.Failed
        assertEquals("IllegalStateException: boom", result.reason)
    }

    @Test
    fun `bootstrap can be skipped when runtime is unavailable`() {
        val runtime = RecordingMercuryRuntime(available = false)
        val adapter = RayNeoVendorAdapter(runtime)

        val result = adapter.bootstrap(FakeApplication())

        assertEquals(BootstrapResult.Skipped, result)
        assertEquals(0, runtime.initCallCount)
    }

    private class FakeApplication : Application()

    private class RecordingMercuryRuntime(
        private val available: Boolean = true,
    ) : MercuryRuntime {
        var initCallCount: Int = 0

        override fun isAvailable(): Boolean = available

        override fun init(application: Application) {
            initCallCount += 1
        }
    }

    private class FailingMercuryRuntime(
        private val error: Throwable,
    ) : MercuryRuntime {
        override fun isAvailable(): Boolean = true

        override fun init(application: Application) {
            throw error
        }
    }
}
