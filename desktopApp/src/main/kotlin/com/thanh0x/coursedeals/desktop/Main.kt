package com.thanh0x.coursedeals.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.thanh0x.coursedeals.core.common.AppVersion
import com.thanh0x.coursedeals.core.logging.LogFileManager
import com.thanh0x.coursedeals.core.logging.LoggerFactory
import com.thanh0x.coursedeals.core.logging.debug
import com.thanh0x.coursedeals.core.logging.warn
import com.thanh0x.coursedeals.shared.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import javax.imageio.ImageIO

private const val WINDOW_WIDTH_DP = 430
private const val WINDOW_HEIGHT_DP = 880

fun main(args: Array<String>) {
    LogFileManager.init(null)
    val logger = LoggerFactory.getLogger("CourseDealsDesktop")

    val combinedArgs = resolveCombinedDesktopArgs(args.toList())
    val config = parseDesktopArgs(combinedArgs)
    AppVersion.overrideVersionCode = config.versionOverride

    logger.debug {
        "CourseDeals Desktop starting (screen=${config.initialScreen}, route=${config.initialRoute}, " +
            "versionOverride=${config.versionOverride})"
    }

    installDesktopPlatformActions()
    installDesktopRenderers()

    application {
        val windowState = WindowState(
            width = WINDOW_WIDTH_DP.dp,
            height = WINDOW_HEIGHT_DP.dp,
            position = WindowPosition(Alignment.Center),
        )

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Course Deals",
        ) {
            val graphicsLayer = rememberGraphicsLayer()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    },
            ) {
                App(
                    initialScreen = config.initialScreen,
                    initialRoute = config.initialRoute,
                    initialAuthMode = config.initialAuthMode,
                    initialPendingEmail = config.initialPendingEmail,
                    initialShowTermsDialog = config.initialShowTermsDialog,
                )
            }

            val screenshotPath = config.captureScreenshotPath
            if (screenshotPath != null) {
                LaunchedEffect(screenshotPath) {
                    delay(config.captureDelayMs)
                    runCatching {
                        val bitmap = graphicsLayer.toImageBitmap()
                        withContext(Dispatchers.IO) {
                            val outputFile = File(screenshotPath)
                            outputFile.parentFile?.mkdirs()
                            ImageIO.write(bitmap.toAwtImage(), "png", outputFile)
                        }
                        logger.debug { "Saved desktop screenshot to $screenshotPath" }
                    }.onFailure { throwable ->
                        logger.warn(throwable) { "Failed to capture desktop screenshot to $screenshotPath" }
                    }
                    exitApplication()
                }
            }
        }
    }
}
