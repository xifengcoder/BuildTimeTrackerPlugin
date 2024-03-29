package com.yxf.buildtimetracker.reporter

import au.com.bytecode.opencsv.CSVReader
import com.yxf.buildtimetracker.AbstractBuildTimeTrackerReporter
import com.yxf.buildtimetracker.DateUtils
import com.yxf.buildtimetracker.ReporterConfigurationError
import com.yxf.buildtimetracker.Timing
import org.gradle.api.logging.Logger
import org.ocpsoft.prettytime.PrettyTime

class CSVSummaryReporter extends AbstractBuildTimeTrackerReporter {
    DateUtils dateUtils

    CSVSummaryReporter(Map<String, String> options, Logger logger) {
        super(options, logger)
        dateUtils = new DateUtils()
    }

    @Override
    def run(List<Timing> timings) {
        def csv = getOption("csv", "")
        def csvFile = new File(csv)

        if (csv.isEmpty()) {
            throw new ReporterConfigurationError(
                    ReporterConfigurationError.ErrorType.REQUIRED,
                    this.getClass().getSimpleName(),
                    "csv"
            )
        }

        if (!csvFile.exists() || !csvFile.isFile()) {
            throw new ReporterConfigurationError(
                    ReporterConfigurationError.ErrorType.INVALID,
                    this.getClass().getSimpleName(),
                    "csv",
                    "$csv either doesn't exist or is not a valid file"
            )
        }

        printReport(new CSVReader(new BufferedReader(new FileReader(csvFile))))
    }

    void printReport(CSVReader reader) {
        try {
            def lines = reader.readAll()
            if (lines.size() == 0) return

            logger.lifecycle "== CSV Build Time Summary =="

            Map<Long, Long> times = lines.findAll { it[0] != 'timestamp' }.groupBy {
                it[0]
            }.collectEntries {
                k, v -> [Long.valueOf(k), v.collect { Long.valueOf(it[6]) }.sum()]
            }

            printToday(times)
            printTotal(times)
        } finally {
            reader.close()
        }
    }

    void printTotal(Map<Long, Long> times) {
        long total = times.collect { it.value }.sum()
        def prettyTime = new PrettyTime()
        def first = new Date((Long) times.keySet().min())
        logger.lifecycle "Total build time: " + FormattingUtils.formatDuration(total)
        logger.lifecycle "(measured since " + prettyTime.format(first) + ")"
    }

    void printToday(Map<Long, Long> times) {
        def midnight = dateUtils.localMidnightUTCTimestamp
        long today = times.collect { it.key >= midnight ? it.value : 0 }.sum()
        logger.lifecycle "Build time today: " + FormattingUtils.formatDuration(today)
    }
}
