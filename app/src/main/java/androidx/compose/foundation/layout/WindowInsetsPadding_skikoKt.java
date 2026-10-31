package androidx.compose.foundation.layout;

import androidx.compose.ui.Modifier;

public final class WindowInsetsPadding_skikoKt {
    private WindowInsetsPadding_skikoKt() {}

    public static Modifier statusBarsPadding(Modifier modifier) {
        return WindowInsetsPadding_androidKt.statusBarsPadding(modifier);
    }

    public static Modifier navigationBarsPadding(Modifier modifier) {
        return WindowInsetsPadding_androidKt.navigationBarsPadding(modifier);
    }

    public static Modifier systemBarsPadding(Modifier modifier) {
        return WindowInsetsPadding_androidKt.systemBarsPadding(modifier);
    }

    public static Modifier imePadding(Modifier modifier) {
        return WindowInsetsPadding_androidKt.imePadding(modifier);
    }

    public static Modifier safeDrawingPadding(Modifier modifier) {
        return WindowInsetsPadding_androidKt.safeDrawingPadding(modifier);
    }
}
