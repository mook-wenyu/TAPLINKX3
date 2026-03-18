package dev.wenyu.semanticcontrol.feature.semantic.debug

import org.junit.Assert.assertEquals
import org.junit.Test

class SemanticDebugSnapshotFormatterTest {

    @Test
    fun `formats root focus and actionable candidates`() {
        val snapshot = SemanticDebugSnapshot(
            rootPackage = "com.android.settings",
            rootClass = "android.widget.FrameLayout",
            accessibilityFocus = DebugNodeSummary(
                className = "android.widget.LinearLayout",
                label = "网络和互联网",
                viewId = "android:id/title",
            ),
            inputFocus = DebugNodeSummary(
                className = "android.widget.ScrollView",
                label = "主页滚动区",
                viewId = "com.android.settings:id/main_content_scrollable_container",
            ),
            actionableCandidates = listOf(
                DebugNodeSummary(
                    className = "android.view.ViewGroup",
                    label = "搜索设置",
                    viewId = "com.android.settings:id/search_action_bar",
                ),
                DebugNodeSummary(
                    className = "android.widget.LinearLayout",
                    label = "网络和互联网",
                    viewId = "android:id/title",
                ),
            ),
        )

        assertEquals(
            "root=com.android.settings/android.widget.FrameLayout;" +
                "a11yFocus=android.widget.LinearLayout:网络和互联网#android:id/title;" +
                "inputFocus=android.widget.ScrollView:主页滚动区#com.android.settings:id/main_content_scrollable_container;" +
                "candidates=[0]android.view.ViewGroup:搜索设置#com.android.settings:id/search_action_bar|" +
                "[1]android.widget.LinearLayout:网络和互联网#android:id/title",
            snapshot.formatForLog(),
        )
    }

    @Test
    fun `truncates candidate list after limit`() {
        val snapshot = SemanticDebugSnapshot(
            rootPackage = "pkg",
            rootClass = "Root",
            accessibilityFocus = null,
            inputFocus = null,
            actionableCandidates = (1..5).map {
                DebugNodeSummary(className = "Class$it", label = "Label$it", viewId = null)
            },
        )

        assertEquals(
            "root=pkg/Root;a11yFocus=null;inputFocus=null;" +
                "candidates=[0]Class1:Label1|[1]Class2:Label2|[2]Class3:Label3|+2more",
            snapshot.formatForLog(candidateLimit = 3),
        )
    }

    @Test
    fun `uses null markers when there is no focus or candidate`() {
        val snapshot = SemanticDebugSnapshot(
            rootPackage = "pkg",
            rootClass = "Root",
            accessibilityFocus = null,
            inputFocus = null,
            actionableCandidates = emptyList(),
        )

        assertEquals(
            "root=pkg/Root;a11yFocus=null;inputFocus=null;candidates=none",
            snapshot.formatForLog(),
        )
    }
}
