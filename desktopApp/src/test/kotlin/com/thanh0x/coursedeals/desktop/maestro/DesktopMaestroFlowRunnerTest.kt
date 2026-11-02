package com.thanh0x.coursedeals.desktop.maestro

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import com.thanh0x.coursedeals.core.common.AppVersion
import com.thanh0x.coursedeals.core.logging.LogFileManager
import com.thanh0x.coursedeals.desktop.installDesktopPlatformActions
import com.thanh0x.coursedeals.desktop.installDesktopRenderers
import com.thanh0x.coursedeals.shared.App
import com.thanh0x.coursedeals.shared.AppScreen
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO

class DesktopMaestroFlowRunnerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @After
    fun tearDown() {
        AppVersion.overrideVersionCode = null
    }

    @Test
    fun `happyPath parser accurately parses existing repo Maestro YAML flows`() {
        val repoRoot = resolveRepoRoot()
        val authFlowFile = File(repoRoot, ".maestro/flows/02-auth.yaml")
        val homeFlowFile = File(repoRoot, ".maestro/flows/03-home-feed.yaml")
        val evidenceFlowFile = File(repoRoot, ".maestro/flows/07-auth-e2e-evidence.yaml")
        val forceUpdateFlowFile = File(repoRoot, ".maestro/flows/156-force-update-gate.yaml")

        assertTrue("Expected 02-auth.yaml to exist", authFlowFile.exists())
        assertTrue("Expected 03-home-feed.yaml to exist", homeFlowFile.exists())
        assertTrue("Expected 07-auth-e2e-evidence.yaml to exist", evidenceFlowFile.exists())
        assertTrue("Expected 156-force-update-gate.yaml to exist", forceUpdateFlowFile.exists())

        val authDoc = DesktopMaestroFlowParser.parseFile(authFlowFile)
        assertEquals("\${APP_ID}", authDoc.appId)
        assertTrue(authDoc.commands.first() is MaestroCommand.LaunchApp)
        assertTrue(authDoc.commands.any { it is MaestroCommand.RunFlowWhen })
        assertTrue(authDoc.commands.any { it is MaestroCommand.ExtendedWaitUntil })
        assertTrue(authDoc.commands.any { it is MaestroCommand.AssertNotVisible })

        val evidenceDoc = DesktopMaestroFlowParser.parseFile(evidenceFlowFile)
        val screenshots = evidenceDoc.commands.filterIsInstance<MaestroCommand.TakeScreenshot>()
        assertEquals(5, screenshots.size)
        assertEquals("android_01_login_screen", screenshots[0].name)
        assertEquals(
            "desktop_01_login_screen",
            normalizeDesktopScreenshotName(screenshots[0].name),
        )

        val forceUpdateDoc = DesktopMaestroFlowParser.parseFile(forceUpdateFlowFile)
        val launchCommands = forceUpdateDoc.commands.filterIsInstance<MaestroCommand.LaunchApp>()
        assertEquals(2, launchCommands.size)
        assertEquals("100", launchCommands[0].arguments["override_version_code"])
        assertEquals("105", launchCommands[1].arguments["override_version_code"])
    }

    @Test
    fun `happyPath runner executes multi-step Maestro YAML flow and exports PNG screenshots`() {
        val tempScreenshotDir = Files.createTempDirectory("desktop-maestro-test-").toFile()
        try {
            composeTestRule.setContent {
                MaterialTheme {
                    SampleInteractiveAuthFlowHarness()
                }
            }

            val yamlFlow = """
                appId: com.thanh0x.coursedeals.desktop
                ---
                - launchApp:
                    clearState: true
                - extendedWaitUntil:
                    visible: "Continue with Email"
                    timeout: 5000
                - assertVisible: "Email"
                - assertNotVisible: "Password"
                - takeScreenshot: android_01_login_screen
                - tapOn: "Email"
                - inputText: "brandnewuser@test.com"
                - tapOn: "Continue with Email"
                - extendedWaitUntil:
                    visible: "Verify Your Email"
                    timeout: 5000
                - assertVisible: "brandnewuser@test.com"
                - takeScreenshot: android_02_otp_screen
                - tapOn: "6-digit OTP"
                - inputText: "000000"
                - tapOn: "Verify OTP"
                - extendedWaitUntil:
                    visible: "HOME"
                    timeout: 5000
                - assertVisible: "HOME"
                - tapOn: "PROFILE"
                - assertVisible: "Authenticated User: brandnewuser@test.com"
                - takeScreenshot: android_03_profile_screen
            """.trimIndent()

            val runner = DesktopMaestroFlowRunner(
                rule = composeTestRule,
                screenshotDir = tempScreenshotDir,
            )
            val result = runner.runFlowYaml(yamlFlow)

            assertEquals(3, result.capturedScreenshots.size)
            val expectedNames = listOf(
                "desktop_01_login_screen.png",
                "desktop_02_otp_screen.png",
                "desktop_03_profile_screen.png",
            )
            for ((index, expectedName) in expectedNames.withIndex()) {
                val file = result.capturedScreenshots[index]
                assertEquals(expectedName, file.name)
                assertTrue("Screenshot file must exist: ${file.absolutePath}", file.exists())
                assertTrue("Screenshot file must be non-empty", file.length() > 0L)
                val decodedImage = ImageIO.read(file)
                assertNotNull("Decoded PNG must not be null", decodedImage)
                assertTrue("PNG width must be > 0", decodedImage.width > 0)
                assertTrue("PNG height must be > 0", decodedImage.height > 0)
            }
        } finally {
            tempScreenshotDir.deleteRecursively()
        }
    }

    @Test
    fun `failedPath conditional runFlow and optional commands skip cleanly without throwing`() {
        composeTestRule.setContent {
            MaterialTheme {
                SampleInteractiveAuthFlowHarness()
            }
        }

        val yamlFlow = """
            appId: com.thanh0x.coursedeals.desktop
            ---
            - launchApp:
                clearState: false
            # Unknown YAML commands or comments should be ignored cleanly
            - unknownFutureCommand:
                foo: bar
            - runFlow:
                when:
                  visible: "DefinitelyNotOnScreenElement"
                commands:
                  - assertVisible: "ThisShouldNeverExecute"
            - tapOn:
                text: "OptionalMissingButton"
                optional: true
            - extendedWaitUntil:
                visible: "OptionalMissingBanner"
                timeout: 150
                optional: true
            - assertVisible: "Continue with Email"
        """.trimIndent()

        val runner = DesktopMaestroFlowRunner(rule = composeTestRule)
        val result = runner.runFlowYaml(yamlFlow)
        assertTrue(result.executedCommandsCount >= 5)
    }

    @Test
    fun `errorPath missing required node on assertVisible or extendedWaitUntil throws descriptive AssertionError`() {
        composeTestRule.setContent {
            MaterialTheme {
                SampleInteractiveAuthFlowHarness()
            }
        }

        val runner = DesktopMaestroFlowRunner(rule = composeTestRule)
        var caughtAssertion: AssertionError? = null
        try {
            runner.runFlowYaml(
                """
                ---
                - assertVisible: "NonExistentWidgetXYZ999"
                """.trimIndent()
            )
        } catch (error: AssertionError) {
            caughtAssertion = error
        }

        assertNotNull("Expected AssertionError when required node is missing", caughtAssertion)
        assertTrue(
            "AssertionError message should mention missing selector",
            caughtAssertion?.message?.contains("NonExistentWidgetXYZ999") == true,
        )
    }

    @Test
    fun runConfiguredMaestroFlowFromCli() {
        val flowPathProp = System.getProperty("maestro.flow")?.trim().orEmpty()
        if (flowPathProp.isEmpty()) {
            return
        }

        val repoRoot = resolveRepoRoot()
        val flowFile = File(flowPathProp).let { candidate ->
            if (candidate.isAbsolute) candidate else File(repoRoot, flowPathProp)
        }
        require(flowFile.exists()) {
            "Configured Maestro flow file not found: ${flowFile.absolutePath}"
        }

        val screenshotDirPath = System.getProperty("maestro.screenshotDir")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: "/tmp/desktop-maestro-screenshots"
        val screenshotDir = File(screenshotDirPath)
        screenshotDir.mkdirs()

        val useLiveBackend = System.getProperty("maestro.useLiveBackend") == "true"
        if (useLiveBackend) {
            LogFileManager.init(null)
            installDesktopPlatformActions()
            installDesktopRenderers()
        } else {
            installFullAppDesktopTestKoin()
        }

        var launchEpoch by mutableIntStateOf(0)
        var currentInitialScreen by mutableStateOf<AppScreen>(AppScreen.Login)
        var hasLaunchedOnce = false

        composeTestRule.setContent {
            key(launchEpoch) {
                App(initialScreen = currentInitialScreen)
            }
        }

        val runner = DesktopMaestroFlowRunner(
            rule = composeTestRule,
            screenshotDir = screenshotDir,
            onLaunchApp = { launchCmd ->
                val versionCode = launchCmd.arguments["override_version_code"]?.toIntOrNull()
                if (versionCode != null) {
                    AppVersion.overrideVersionCode = versionCode
                    currentInitialScreen = AppScreen.Splash
                    launchEpoch++
                } else if (hasLaunchedOnce && (launchCmd.stopApp || launchCmd.clearState)) {
                    AppVersion.overrideVersionCode = null
                    currentInitialScreen = AppScreen.Login
                    launchEpoch++
                }
                hasLaunchedOnce = true
            },
        )

        runner.runFlowFile(flowFile)
    }

    private fun resolveRepoRoot(): File {
        val currentDir = File(System.getProperty("user.dir") ?: ".")
        return if (File(currentDir, ".maestro/flows").exists()) {
            currentDir
        } else {
            currentDir.parentFile ?: currentDir
        }
    }
}

@Composable
private fun SampleInteractiveAuthFlowHarness() {
    var step by remember { mutableStateOf("LOGIN") }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    Column {
        when (step) {
            "LOGIN" -> {
                Text(text = "Sign In to Course Deals")
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.testTag("Email"),
                )
                Button(onClick = { step = "OTP" }) {
                    Text("Continue with Email")
                }
                Button(onClick = { step = "HOME" }) {
                    Text("Access as Guest")
                }
            }

            "OTP" -> {
                Text(text = "Verify Your Email")
                Text(text = email)
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = { Text("6-digit OTP") },
                    modifier = Modifier.testTag("6-digit OTP"),
                )
                Button(onClick = { step = "HOME" }) {
                    Text("Verify OTP")
                }
            }

            "HOME" -> {
                Text(text = "HOME")
                Text(text = "COURSE")
                Button(onClick = { step = "PROFILE" }) {
                    Text("PROFILE")
                }
            }

            "PROFILE" -> {
                Text(text = "PROFILE")
                Text(text = "Authenticated User: $email")
            }
        }
    }
}
