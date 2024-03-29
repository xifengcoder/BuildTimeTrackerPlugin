package com.yxf.buildtimetracker.reporter

import com.yxf.buildtimetracker.AbstractBuildTimeTrackerReporter
import com.yxf.buildtimetracker.TerminalInfo
import com.yxf.buildtimetracker.Timing
import org.gradle.BuildResult
import org.gradle.api.logging.Logger

class SummaryReporter extends AbstractBuildTimeTrackerReporter {
    def static final UNICODE_SQUARE = "▇"
    def static final ASCII_SQUARE = "▒"
    def static final FILL = " "

    String barStyle
    boolean successOutput
    boolean shortenTaskNames

    SummaryReporter(Map<String, String> options, Logger logger) {
        super(options, logger)

        barStyle = getOption("barstyle", "unicode")
        successOutput = Boolean.parseBoolean(getOption("successOutput", "true"))
        shortenTaskNames = Boolean.parseBoolean(getOption("shortenTaskNames", "true"))
    }

    @Override
    def run(List<Timing> timings) {
        if (timings.size() == 0) return

        def threshold = getOption("threshold", "50").toInteger()

        if (getOption("ordered", "false").toBoolean()) {
            timings = timings.sort(false, { it.ms })
        }

        logger.lifecycle("== Build Time Summary ==")
        formatTable(timings, threshold)
    }

    @Override
    void onBuildResult(BuildResult result) {
        if (!successOutput) {
            return;
        }

        // Separate from previous output with a new line
        logger.lifecycle("")

        if (result.failure != null) {
            logger.error("== BUILD FAILED ==")
        } else {
            logger.lifecycle("== BUILD SUCCESSFUL ==")
        }
    }

    // Thanks to @sindresorhus for the logic. https://github.com/sindresorhus/time-grunt
    def formatTable(List<Timing> timings, int threshold) {
        def total = timings.sum { t -> t.ms }
        def longestTaskName = timings.collect { it.path.length() }.max()
        def longestTiming = timings*.ms.max()
        def maxColumns = (new TerminalInfo()).getWidth(80)

        def maxBarWidth
        if (longestTaskName > maxColumns / 2) {
            maxBarWidth = (maxColumns - 20) / 2
        } else {
            maxBarWidth = maxColumns - (longestTaskName + 20)
        }

        for (timing in timings) {
            if (timing.ms >= threshold) {
                logger.lifecycle(sprintf("%s %s (%s)",
                        createBar(timing.ms / total, timing.ms / longestTiming, maxBarWidth),
                        shortenTaskNames ? shortenTaskName(timing.path, maxBarWidth) : timing.path,
                        FormattingUtils.formatDuration(timing.ms)))
            }
        }
    }

    def static shortenTaskName(String taskName, def max) {
        if (taskName.length() < max) {
            return taskName
        }

        int partLength = Math.floor((max - 3) / 2) as int
        def start = taskName.substring(0, partLength + 1)
        def end = taskName.substring(taskName.length() - partLength)

        start.trim() + '…' + end.trim()
    }

    def createBar(def fracOfTotal, def fracOfMax, def max) {
        def symbol = barStyle == "ascii" ? ASCII_SQUARE : UNICODE_SQUARE

        def roundedTotal = Math.round(fracOfTotal * 100)
        def barLength = Math.ceil(max * fracOfMax)
        def bar = FILL * (max - barLength) + symbol * (barLength - 1)
        def formatted = (roundedTotal < 10 ? " " : "") + roundedTotal
        return (barStyle != "none" ? (bar + " ") : "") + formatted + '%'
    }

}
