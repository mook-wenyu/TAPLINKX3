package dev.wenyu.semanticcontrol.feature.semantic.debug

data class DebugNodeSummary(
    val className: String?,
    val label: String?,
    val viewId: String?,
) {
    fun format(): String {
        return buildString {
            append(className ?: "null")
            if (!label.isNullOrBlank()) {
                append(":")
                append(label)
            }
            if (!viewId.isNullOrBlank()) {
                append("#")
                append(viewId)
            }
        }
    }
}

data class SemanticDebugSnapshot(
    val rootPackage: String?,
    val rootClass: String?,
    val accessibilityFocus: DebugNodeSummary?,
    val inputFocus: DebugNodeSummary?,
    val actionableCandidates: List<DebugNodeSummary>,
) {
    fun formatForLog(candidateLimit: Int = 5): String {
        return buildString {
            append("root=")
            append(rootPackage ?: "null")
            append("/")
            append(rootClass ?: "null")
            append(";a11yFocus=")
            append(accessibilityFocus?.format() ?: "null")
            append(";inputFocus=")
            append(inputFocus?.format() ?: "null")
            append(";candidates=")
            if (actionableCandidates.isEmpty()) {
                append("none")
            } else {
                val visibleCandidates = actionableCandidates.take(candidateLimit)
                append(
                    visibleCandidates.mapIndexed { index, candidate ->
                        "[$index]${candidate.format()}"
                    }.joinToString("|"),
                )
                val remaining = actionableCandidates.size - visibleCandidates.size
                if (remaining > 0) {
                    append("|+")
                    append(remaining)
                    append("more")
                }
            }
        }
    }
}
