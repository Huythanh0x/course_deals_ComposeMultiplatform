package androidx.compose.ui.tooling.preview

private const val UNSPECIFIED_PREVIEW_DIMENSION = -1

@Repeatable
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
annotation class Preview(
    val name: String = "",
    val group: String = "",
    val apiLevel: Int = UNSPECIFIED_PREVIEW_DIMENSION,
    val widthDp: Int = UNSPECIFIED_PREVIEW_DIMENSION,
    val heightDp: Int = UNSPECIFIED_PREVIEW_DIMENSION,
    val locale: String = "",
    val fontScale: Float = 1f,
    val showSystemUi: Boolean = false,
    val showBackground: Boolean = false,
    val backgroundColor: Long = 0,
    val uiMode: Int = 0,
    val device: String = "",
)
