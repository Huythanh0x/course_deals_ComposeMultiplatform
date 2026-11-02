package com.thanh0x.coursedeals.desktop.maestro

import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import java.io.File
import javax.imageio.ImageIO

private const val POLL_INTERVAL_MS = 50L

data class MaestroFlowExecutionResult(
    val executedCommandsCount: Int,
    val capturedScreenshots: List<File>,
)

fun normalizeDesktopScreenshotName(rawName: String): String {
    val trimmed = rawName.trim().removeSuffix(".png")
    return when {
        trimmed.startsWith("android_") -> "desktop_" + trimmed.removePrefix("android_")
        trimmed.startsWith("ios_") -> "desktop_" + trimmed.removePrefix("ios_")
        trimmed.endsWith("_android") -> trimmed.removeSuffix("_android") + "_desktop"
        trimmed.endsWith("_ios") -> trimmed.removeSuffix("_ios") + "_desktop"
        else -> trimmed
    }
}

class DesktopMaestroFlowRunner(
    private val rule: ComposeContentTestRule,
    private val screenshotDir: File = File("/tmp/desktop-maestro-screenshots"),
    private val screenshotNameTransform: (String) -> String = ::normalizeDesktopScreenshotName,
    private val onLaunchApp: ((MaestroCommand.LaunchApp) -> Unit)? = null,
    private val onBack: (() -> Unit)? = null,
) {
    private var lastTappedSelector: MaestroSelector? = null
    private var executedCount: Int = 0
    private val screenshots = mutableListOf<File>()

    fun runFlowFile(flowFile: File): MaestroFlowExecutionResult {
        val document = DesktopMaestroFlowParser.parseFile(flowFile)
        return runFlow(document)
    }

    fun runFlowYaml(yamlContent: String): MaestroFlowExecutionResult {
        val document = DesktopMaestroFlowParser.parse(yamlContent)
        return runFlow(document)
    }

    fun runFlow(document: MaestroFlowDocument): MaestroFlowExecutionResult {
        executeCommandList(document.commands)
        return MaestroFlowExecutionResult(
            executedCommandsCount = executedCount,
            capturedScreenshots = screenshots.toList(),
        )
    }

    private fun executeCommandList(commands: List<MaestroCommand>) {
        for (command in commands) {
            executeCommand(command)
        }
    }

    fun executeCommand(command: MaestroCommand) {
        executedCount++
        rule.waitForIdle()

        when (command) {
            is MaestroCommand.LaunchApp -> {
                onLaunchApp?.invoke(command)
                rule.waitForIdle()
            }

            is MaestroCommand.TapOn -> executeTapOn(command)

            is MaestroCommand.InputText -> executeInputText(command)

            is MaestroCommand.EraseText -> executeEraseText()

            is MaestroCommand.AssertVisible -> executeAssertVisible(command)

            is MaestroCommand.AssertNotVisible -> executeAssertNotVisible(command)

            is MaestroCommand.ExtendedWaitUntil -> executeExtendedWaitUntil(command)

            is MaestroCommand.RunFlowWhen -> executeRunFlowWhen(command)

            is MaestroCommand.ScrollUntilVisible -> executeScrollUntilVisible(command)

            is MaestroCommand.Swipe -> {
                rule.waitForIdle()
            }

            is MaestroCommand.TakeScreenshot -> executeTakeScreenshot(command)

            is MaestroCommand.Back -> executeBack()
        }

        rule.waitForIdle()
    }

    private fun executeTapOn(command: MaestroCommand.TapOn) {
        val interaction = findClickableOrFirstNode(command.selector)
        if (interaction == null) {
            if (command.optional) return
            throw AssertionError("Maestro tapOn failed: could not find node for ${describeSelector(command.selector)}")
        }
        lastTappedSelector = command.selector
        runCatching {
            interaction.performScrollTo()
        }
        runCatching {
            interaction.performClick()
        }.onFailure { error ->
            if (!command.optional) {
                throw AssertionError(
                    "Maestro tapOn failed on ${describeSelector(command.selector)}: ${error.message}",
                    error,
                )
            }
        }
    }

    private fun executeInputText(command: MaestroCommand.InputText) {
        val target = findEditableTextNode()
            ?: throw AssertionError(
                "Maestro inputText('${command.text}') failed: no editable text field found " +
                    "(lastTapped=${lastTappedSelector?.let(::describeSelector)})"
            )
        target.performTextInput(command.text)
    }

    private fun executeEraseText() {
        val target = findEditableTextNode() ?: return
        runCatching {
            target.performTextClearance()
        }
    }

    private fun executeAssertVisible(command: MaestroCommand.AssertVisible) {
        val visible = isSelectorVisible(command.selector)
        if (!visible && !command.optional) {
            throw AssertionError(
                "Maestro assertVisible failed: expected ${describeSelector(command.selector)} to be visible"
            )
        }
    }

    private fun executeAssertNotVisible(command: MaestroCommand.AssertNotVisible) {
        val visible = isSelectorVisible(command.selector)
        if (visible && !command.optional) {
            throw AssertionError(
                "Maestro assertNotVisible failed: expected ${describeSelector(command.selector)} to NOT be visible"
            )
        }
    }

    private fun executeExtendedWaitUntil(command: MaestroCommand.ExtendedWaitUntil) {
        val deadline = System.currentTimeMillis() + command.timeoutMs
        while (System.currentTimeMillis() <= deadline) {
            rule.mainClock.advanceTimeBy(POLL_INTERVAL_MS)
            rule.waitForIdle()
            val visibleOk = command.visible?.let(::isSelectorVisible) ?: true
            val notVisibleOk = command.notVisible?.let { !isSelectorVisible(it) } ?: true
            if (visibleOk && notVisibleOk) {
                return
            }
            Thread.sleep(POLL_INTERVAL_MS)
        }

        if (!command.optional) {
            val targetDesc = listOfNotNull(
                command.visible?.let { "visible=${describeSelector(it)}" },
                command.notVisible?.let { "notVisible=${describeSelector(it)}" },
            ).joinToString(", ")
            throw AssertionError(
                "Maestro extendedWaitUntil timed out after ${command.timeoutMs}ms waiting for [$targetDesc]"
            )
        }
    }

    private fun executeRunFlowWhen(command: MaestroCommand.RunFlowWhen) {
        val visibleMet = command.whenVisible?.let(::isSelectorVisible) ?: true
        val notVisibleMet = command.whenNotVisible?.let { !isSelectorVisible(it) } ?: true
        if (visibleMet && notVisibleMet) {
            executeCommandList(command.commands)
        }
    }

    private fun executeScrollUntilVisible(command: MaestroCommand.ScrollUntilVisible) {
        val deadline = System.currentTimeMillis() + command.timeoutMs
        while (System.currentTimeMillis() <= deadline) {
            rule.waitForIdle()
            if (isSelectorVisible(command.element)) {
                val node = findClickableOrFirstNode(command.element)
                if (node != null) {
                    runCatching { node.performScrollTo() }
                }
                return
            }
            Thread.sleep(POLL_INTERVAL_MS)
        }
        throw AssertionError(
            "Maestro scrollUntilVisible timed out after ${command.timeoutMs}ms waiting for " +
                describeSelector(command.element)
        )
    }

    private fun executeTakeScreenshot(command: MaestroCommand.TakeScreenshot) {
        rule.waitForIdle()
        val finalName = screenshotNameTransform(command.name)
        val outputFile = File(screenshotDir, "$finalName.png")
        outputFile.parentFile?.mkdirs()
        val bitmap = rule.onAllNodes(isRoot()).onFirst().captureToImage()
        ImageIO.write(bitmap.toAwtImage(), "png", outputFile)
        screenshots.add(outputFile)
    }

    private fun executeBack() {
        if (onBack != null) {
            onBack.invoke()
            return
        }
        val backNode = findClickableOrFirstNode(MaestroSelector.ByText("Back"))
            ?: findClickableOrFirstNode(MaestroSelector.ById("back"))
        if (backNode != null) {
            runCatching { backNode.performClick() }
        }
    }

    fun isSelectorVisible(selector: MaestroSelector): Boolean {
        val matcher = matcherFor(selector)
        val mergedNodes = runCatching {
            rule.onAllNodes(matcher, useUnmergedTree = false).fetchSemanticsNodes()
        }.getOrDefault(emptyList())
        if (mergedNodes.isNotEmpty()) return true

        val unmergedNodes = runCatching {
            rule.onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes()
        }.getOrDefault(emptyList())
        return unmergedNodes.isNotEmpty()
    }

    private fun findClickableOrFirstNode(selector: MaestroSelector): SemanticsNodeInteraction? {
        return findNodeWithMatcher(exactMatcherFor(selector))
            ?: findNodeWithMatcher(matcherFor(selector))
    }

    private fun findNodeWithMatcher(matcher: SemanticsMatcher): SemanticsNodeInteraction? {
        val mergedCollection = rule.onAllNodes(matcher, useUnmergedTree = false)
        val mergedNodes = runCatching { mergedCollection.fetchSemanticsNodes() }.getOrDefault(emptyList())
        if (mergedNodes.isNotEmpty()) {
            val clickableIndex = mergedNodes.indexOfLast { node ->
                SemanticsActions.OnClick in node.config || SemanticsActions.SetText in node.config
            }
            val targetIndex = if (clickableIndex >= 0) clickableIndex else mergedNodes.lastIndex
            return mergedCollection[targetIndex]
        }

        val unmergedCollection = rule.onAllNodes(matcher, useUnmergedTree = true)
        val unmergedNodes = runCatching { unmergedCollection.fetchSemanticsNodes() }.getOrDefault(emptyList())
        if (unmergedNodes.isNotEmpty()) {
            val clickableIndex = unmergedNodes.indexOfLast { node ->
                SemanticsActions.OnClick in node.config || SemanticsActions.SetText in node.config
            }
            val targetIndex = if (clickableIndex >= 0) clickableIndex else unmergedNodes.lastIndex
            return unmergedCollection[targetIndex]
        }

        return null
    }

    private fun findEditableTextNode(): SemanticsNodeInteraction? {
        val byTappedSelector = lastTappedSelector?.let { lastSelector ->
            val exactCombined = hasSetTextAction() and exactMatcherFor(lastSelector)
            val substringCombined = hasSetTextAction() and matcherFor(lastSelector)
            lastNodeOrNull(exactCombined, useUnmergedTree = false)
                ?: lastNodeOrNull(exactCombined, useUnmergedTree = true)
                ?: lastNodeOrNull(substringCombined, useUnmergedTree = false)
                ?: lastNodeOrNull(substringCombined, useUnmergedTree = true)
        }
        return byTappedSelector
            ?: lastNodeOrNull(hasSetTextAction() and isFocused(), useUnmergedTree = false)
            ?: lastNodeOrNull(hasSetTextAction(), useUnmergedTree = false)
    }

    private fun lastNodeOrNull(
        matcher: SemanticsMatcher,
        useUnmergedTree: Boolean,
    ): SemanticsNodeInteraction? {
        val collection = rule.onAllNodes(matcher, useUnmergedTree = useUnmergedTree)
        val nodes = runCatching { collection.fetchSemanticsNodes() }.getOrDefault(emptyList())
        return if (nodes.isNotEmpty()) collection[nodes.lastIndex] else null
    }

    private fun exactMatcherFor(selector: MaestroSelector): SemanticsMatcher = when (selector) {
        is MaestroSelector.ById -> {
            hasTestTag(selector.id) or
                hasContentDescription(selector.id, substring = false) or
                hasText(selector.id, substring = false)
        }

        is MaestroSelector.ByText -> {
            hasText(selector.text, substring = false) or
                hasContentDescription(selector.text, substring = false) or
                hasTestTag(selector.text)
        }
    }

    private fun matcherFor(selector: MaestroSelector): SemanticsMatcher = when (selector) {
        is MaestroSelector.ById -> {
            hasTestTag(selector.id) or
                hasContentDescription(selector.id, substring = false) or
                hasText(selector.id, substring = false)
        }

        is MaestroSelector.ByText -> {
            hasText(selector.text, substring = true) or
                hasContentDescription(selector.text, substring = true) or
                hasTestTag(selector.text) or
                SemanticsMatcher("hasEditableText(${selector.text})") { node ->
                    val editable = node.config.getOrNull(SemanticsProperties.EditableText)
                    editable?.text?.contains(selector.text) == true
                }
        }
    }

    private fun describeSelector(selector: MaestroSelector): String = when (selector) {
        is MaestroSelector.ById -> "id='${selector.id}'"
        is MaestroSelector.ByText -> "text='${selector.text}'"
    }
}

class DesktopMaestroDsl(
    private val runner: DesktopMaestroFlowRunner,
) {
    fun launchApp(clearState: Boolean = false) {
        runner.executeCommand(MaestroCommand.LaunchApp(clearState = clearState))
    }

    fun tapOn(text: String, optional: Boolean = false) {
        runner.executeCommand(
            MaestroCommand.TapOn(selector = MaestroSelector.ByText(text), optional = optional)
        )
    }

    fun tapOnId(id: String, optional: Boolean = false) {
        runner.executeCommand(
            MaestroCommand.TapOn(selector = MaestroSelector.ById(id), optional = optional)
        )
    }

    fun inputText(text: String) {
        runner.executeCommand(MaestroCommand.InputText(text))
    }

    fun eraseText() {
        runner.executeCommand(MaestroCommand.EraseText)
    }

    fun assertVisible(text: String, optional: Boolean = false) {
        runner.executeCommand(
            MaestroCommand.AssertVisible(selector = MaestroSelector.ByText(text), optional = optional)
        )
    }

    fun assertNotVisible(text: String, optional: Boolean = false) {
        runner.executeCommand(
            MaestroCommand.AssertNotVisible(selector = MaestroSelector.ByText(text), optional = optional)
        )
    }

    fun waitForVisible(text: String, timeoutMs: Long = 10_000L, optional: Boolean = false) {
        runner.executeCommand(
            MaestroCommand.ExtendedWaitUntil(
                visible = MaestroSelector.ByText(text),
                timeoutMs = timeoutMs,
                optional = optional,
            )
        )
    }

    fun takeScreenshot(name: String) {
        runner.executeCommand(MaestroCommand.TakeScreenshot(name))
    }

    fun back() {
        runner.executeCommand(MaestroCommand.Back)
    }
}

fun ComposeContentTestRule.runDesktopMaestroDsl(
    screenshotDir: File = File("/tmp/desktop-maestro-screenshots"),
    block: DesktopMaestroDsl.() -> Unit,
) {
    val runner = DesktopMaestroFlowRunner(rule = this, screenshotDir = screenshotDir)
    DesktopMaestroDsl(runner).apply(block)
}
