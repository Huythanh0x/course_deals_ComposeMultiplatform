package com.thanh0x.coursedeals.desktop.maestro

import java.io.File

private const val DEFAULT_WAIT_TIMEOUT_MS = 10_000L
private const val INDENT_STEP = 2
private const val MIN_QUOTED_LENGTH = 2

data class MaestroFlowDocument(
    val appId: String? = null,
    val commands: List<MaestroCommand>,
)

sealed interface MaestroSelector {
    data class ByText(val text: String) : MaestroSelector
    data class ById(val id: String) : MaestroSelector
}

sealed interface MaestroCommand {
    data class LaunchApp(
        val clearState: Boolean = false,
        val stopApp: Boolean = false,
        val arguments: Map<String, String> = emptyMap(),
    ) : MaestroCommand

    data class TapOn(
        val selector: MaestroSelector,
        val optional: Boolean = false,
    ) : MaestroCommand

    data class InputText(val text: String) : MaestroCommand

    data object EraseText : MaestroCommand

    data class AssertVisible(
        val selector: MaestroSelector,
        val optional: Boolean = false,
    ) : MaestroCommand

    data class AssertNotVisible(
        val selector: MaestroSelector,
        val optional: Boolean = false,
    ) : MaestroCommand

    data class ExtendedWaitUntil(
        val visible: MaestroSelector? = null,
        val notVisible: MaestroSelector? = null,
        val timeoutMs: Long = DEFAULT_WAIT_TIMEOUT_MS,
        val optional: Boolean = false,
    ) : MaestroCommand

    data class RunFlowWhen(
        val whenVisible: MaestroSelector? = null,
        val whenNotVisible: MaestroSelector? = null,
        val commands: List<MaestroCommand>,
    ) : MaestroCommand

    data class ScrollUntilVisible(
        val element: MaestroSelector,
        val direction: String = "DOWN",
        val timeoutMs: Long = DEFAULT_WAIT_TIMEOUT_MS,
    ) : MaestroCommand

    data class Swipe(
        val direction: String? = null,
        val optional: Boolean = true,
    ) : MaestroCommand

    data class TakeScreenshot(val name: String) : MaestroCommand

    data object Back : MaestroCommand
}

private data class YamlLine(
    val indent: Int,
    val content: String,
)

object DesktopMaestroFlowParser {

    fun parseFile(file: File): MaestroFlowDocument {
        require(file.exists() && file.isFile) {
            "Maestro flow file does not exist: ${file.absolutePath}"
        }
        return parse(file.readText())
    }

    fun parse(yamlContent: String): MaestroFlowDocument {
        val rawLines = yamlContent.lines()
        val separatorIndex = rawLines.indexOfFirst { it.trim() == "---" }

        val headerLines = if (separatorIndex >= 0) rawLines.subList(0, separatorIndex) else emptyList()
        val bodyRawLines = if (separatorIndex >= 0) {
            rawLines.subList(separatorIndex + 1, rawLines.size)
        } else {
            rawLines
        }

        val appId = headerLines
            .map { stripComment(it).trim() }
            .firstOrNull { it.startsWith("appId:") }
            ?.substringAfter("appId:")
            ?.let(::unquote)
            ?.takeIf { it.isNotEmpty() }

        val cleanedBody = bodyRawLines.mapNotNull { raw ->
            val withoutComment = stripComment(raw)
            val trimmed = withoutComment.trim()
            if (trimmed.isEmpty() || trimmed == "---") {
                null
            } else {
                val indent = withoutComment.indexOfFirst { !it.isWhitespace() }
                YamlLine(indent = indent, content = trimmed)
            }
        }

        val commands = parseCommandList(cleanedBody, 0, cleanedBody.size)
        return MaestroFlowDocument(appId = appId, commands = commands)
    }

    private fun parseCommandList(
        lines: List<YamlLine>,
        startIndex: Int,
        endIndex: Int,
    ): List<MaestroCommand> {
        if (startIndex >= endIndex) return emptyList()
        val commands = mutableListOf<MaestroCommand>()
        var index = startIndex

        while (index < endIndex) {
            val line = lines[index]
            if (!line.content.startsWith("-")) {
                index++
            } else {
                val nextItemIndex = findNextSiblingIndex(lines, index, endIndex)
                parseSingleCommandBlock(lines, index, nextItemIndex)?.let(commands::add)
                index = nextItemIndex
            }
        }

        return commands
    }

    private fun findNextSiblingIndex(
        lines: List<YamlLine>,
        currentIndex: Int,
        endIndex: Int,
    ): Int {
        val itemIndent = lines[currentIndex].indent
        var idx = currentIndex + 1
        while (idx < endIndex && !isSiblingOrParentItem(lines[idx], itemIndent)) {
            idx++
        }
        return idx
    }

    private fun isSiblingOrParentItem(candidate: YamlLine, itemIndent: Int): Boolean =
        candidate.indent < itemIndent ||
            (candidate.indent == itemIndent && candidate.content.startsWith("-"))

    private fun parseSingleCommandBlock(
        lines: List<YamlLine>,
        startIndex: Int,
        endIndex: Int,
    ): MaestroCommand? {
        val firstContent = lines[startIndex].content.removePrefix("-").trim()
        if (firstContent.isEmpty()) return null

        val key = firstContent.substringBefore(":").trim()
        val inlineValue = if (":" in firstContent) {
            unquote(firstContent.substringAfter(":"))
        } else {
            ""
        }
        val childLines = lines.subList(startIndex + 1, endIndex)
        val props = parseFlatProperties(childLines)

        return when (key) {
            "launchApp" -> parseLaunchApp(childLines, props)
            "tapOn" -> parseTapOn(inlineValue, props)
            "inputText" -> MaestroCommand.InputText(inlineValue.ifEmpty { props["text"].orEmpty() })
            "eraseText" -> MaestroCommand.EraseText
            "assertVisible" -> parseAssertVisible(inlineValue, props)
            "assertNotVisible" -> parseAssertNotVisible(inlineValue, props)
            "extendedWaitUntil" -> parseExtendedWaitUntil(childLines, props)
            "runFlow" -> parseRunFlow(childLines)
            "scrollUntilVisible" -> parseScrollUntilVisible(props)
            "swipe" -> MaestroCommand.Swipe(
                direction = props["direction"],
                optional = props["optional"]?.toBooleanStrictOrNull() ?: true,
            )
            "takeScreenshot" -> {
                val screenshotName = inlineValue.ifEmpty { props["path"] ?: props["name"].orEmpty() }
                if (screenshotName.isNotEmpty()) MaestroCommand.TakeScreenshot(screenshotName) else null
            }
            "back" -> MaestroCommand.Back
            else -> null
        }
    }

    private fun parseLaunchApp(
        childLines: List<YamlLine>,
        props: Map<String, String>,
    ): MaestroCommand.LaunchApp {
        val clearState = props["clearState"]?.toBooleanStrictOrNull() ?: false
        val stopApp = props["stopApp"]?.toBooleanStrictOrNull() ?: false
        val arguments = parseNestedArguments(childLines)
        return MaestroCommand.LaunchApp(
            clearState = clearState,
            stopApp = stopApp,
            arguments = arguments,
        )
    }

    private fun parseNestedArguments(childLines: List<YamlLine>): Map<String, String> {
        val argsHeaderIndex = childLines.indexOfFirst { it.content.startsWith("arguments:") }
        if (argsHeaderIndex < 0) return emptyMap()
        val argsIndent = childLines[argsHeaderIndex].indent
        val argumentLines = childLines.subList(argsHeaderIndex + 1, childLines.size)
            .takeWhile { it.indent > argsIndent }
        return parseFlatProperties(argumentLines)
    }

    private fun parseTapOn(
        inlineValue: String,
        props: Map<String, String>,
    ): MaestroCommand.TapOn? {
        val selector = resolveSelector(inlineValue, props) ?: return null
        val optional = props["optional"]?.toBooleanStrictOrNull() ?: false
        return MaestroCommand.TapOn(selector = selector, optional = optional)
    }

    private fun parseAssertVisible(
        inlineValue: String,
        props: Map<String, String>,
    ): MaestroCommand.AssertVisible? {
        val selector = resolveSelector(inlineValue, props) ?: return null
        val optional = props["optional"]?.toBooleanStrictOrNull() ?: false
        return MaestroCommand.AssertVisible(selector = selector, optional = optional)
    }

    private fun parseAssertNotVisible(
        inlineValue: String,
        props: Map<String, String>,
    ): MaestroCommand.AssertNotVisible? {
        val selector = resolveSelector(inlineValue, props) ?: return null
        val optional = props["optional"]?.toBooleanStrictOrNull() ?: false
        return MaestroCommand.AssertNotVisible(selector = selector, optional = optional)
    }

    private fun parseExtendedWaitUntil(
        childLines: List<YamlLine>,
        props: Map<String, String>,
    ): MaestroCommand.ExtendedWaitUntil {
        val visibleSelector = resolveNestedOrInlineSelector(childLines, props, "visible")
        val notVisibleSelector = resolveNestedOrInlineSelector(childLines, props, "notVisible")
        val timeoutMs = props["timeout"]?.toLongOrNull() ?: DEFAULT_WAIT_TIMEOUT_MS
        val optional = props["optional"]?.toBooleanStrictOrNull() ?: false
        return MaestroCommand.ExtendedWaitUntil(
            visible = visibleSelector,
            notVisible = notVisibleSelector,
            timeoutMs = timeoutMs,
            optional = optional,
        )
    }

    private fun parseRunFlow(childLines: List<YamlLine>): MaestroCommand.RunFlowWhen? {
        if (childLines.isEmpty()) return null
        var whenVisible: MaestroSelector? = null
        var whenNotVisible: MaestroSelector? = null

        val whenIndex = childLines.indexOfFirst { it.content.startsWith("when:") }
        val commandsIndex = childLines.indexOfFirst { it.content.startsWith("commands:") }

        if (whenIndex >= 0) {
            val whenIndent = childLines[whenIndex].indent
            val whenEnd = if (commandsIndex > whenIndex) commandsIndex else childLines.size
            val whenLines = childLines.subList(whenIndex + 1, whenEnd)
                .takeWhile { it.indent > whenIndent }
            val whenProps = parseFlatProperties(whenLines)
            whenVisible = resolveNestedOrInlineSelector(whenLines, whenProps, "visible")
            whenNotVisible = resolveNestedOrInlineSelector(whenLines, whenProps, "notVisible")
        }

        val nestedCommands = if (commandsIndex >= 0) {
            parseCommandList(childLines, commandsIndex + 1, childLines.size)
        } else {
            emptyList()
        }

        return MaestroCommand.RunFlowWhen(
            whenVisible = whenVisible,
            whenNotVisible = whenNotVisible,
            commands = nestedCommands,
        )
    }

    private fun parseScrollUntilVisible(
        props: Map<String, String>,
    ): MaestroCommand.ScrollUntilVisible? {
        val elementText = props["element"] ?: props["text"]
        val elementId = props["id"]
        val selector = when {
            !elementId.isNullOrEmpty() -> MaestroSelector.ById(elementId)
            !elementText.isNullOrEmpty() -> MaestroSelector.ByText(elementText)
            else -> null
        } ?: return null
        val direction = props["direction"] ?: "DOWN"
        val timeoutMs = props["timeout"]?.toLongOrNull() ?: DEFAULT_WAIT_TIMEOUT_MS
        return MaestroCommand.ScrollUntilVisible(
            element = selector,
            direction = direction,
            timeoutMs = timeoutMs,
        )
    }

    private fun resolveSelector(
        inlineValue: String,
        props: Map<String, String>,
    ): MaestroSelector? {
        if (inlineValue.isNotEmpty()) {
            return MaestroSelector.ByText(inlineValue)
        }
        val id = props["id"]
        if (!id.isNullOrEmpty()) {
            return MaestroSelector.ById(id)
        }
        val text = props["text"]
        if (!text.isNullOrEmpty()) {
            return MaestroSelector.ByText(text)
        }
        return null
    }

    private fun resolveNestedOrInlineSelector(
        lines: List<YamlLine>,
        props: Map<String, String>,
        key: String,
    ): MaestroSelector? {
        val directValue = props[key]
        if (!directValue.isNullOrEmpty()) {
            return MaestroSelector.ByText(directValue)
        }
        val keyLineIndex = lines.indexOfFirst { it.content.startsWith("$key:") }
        if (keyLineIndex < 0) return null
        val keyIndent = lines[keyLineIndex].indent
        val nestedLines = lines.subList(keyLineIndex + 1, lines.size)
            .takeWhile { it.indent > keyIndent }
        val nestedProps = parseFlatProperties(nestedLines)
        return resolveSelector("", nestedProps)
    }

    private fun parseFlatProperties(lines: List<YamlLine>): Map<String, String> {
        if (lines.isEmpty()) return emptyMap()
        val minIndent = lines.minOf { it.indent }
        val result = LinkedHashMap<String, String>()
        for (line in lines) {
            val isDirectProperty = line.indent <= minIndent + INDENT_STEP &&
                !line.content.startsWith("-") &&
                ":" in line.content
            if (isDirectProperty) {
                val propKey = line.content.substringBefore(":").trim()
                val propVal = unquote(line.content.substringAfter(":"))
                if (propKey.isNotEmpty() && propVal.isNotEmpty()) {
                    result[propKey] = propVal
                }
            }
        }
        return result
    }

    private fun stripComment(rawLine: String): String {
        var inSingleQuote = false
        var inDoubleQuote = false
        for (i in rawLine.indices) {
            val ch = rawLine[i]
            when {
                ch == '\'' && !inDoubleQuote -> inSingleQuote = !inSingleQuote
                ch == '"' && !inSingleQuote -> inDoubleQuote = !inDoubleQuote
                ch == '#' && !inSingleQuote && !inDoubleQuote -> {
                    if (i == 0 || rawLine[i - 1].isWhitespace()) {
                        return rawLine.substring(0, i)
                    }
                }
            }
        }
        return rawLine
    }

    private fun unquote(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.length < MIN_QUOTED_LENGTH) return trimmed
        val first = trimmed.first()
        val last = trimmed.last()
        val isQuoted = (first == '"' && last == '"') || (first == '\'' && last == '\'')
        return if (isQuoted) trimmed.substring(1, trimmed.length - 1) else trimmed
    }
}
