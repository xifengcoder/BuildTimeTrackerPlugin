package com.yxf.buildtimetracker.reporter

import com.yxf.buildtimetracker.AbstractBuildTimeTrackerReporter
import com.yxf.buildtimetracker.SysInfo
import com.yxf.buildtimetracker.Timing
import com.yxf.buildtimetracker.TrueTimeProvider
import groovy.json.JsonBuilder
import org.gradle.api.logging.Logger

import java.text.DateFormat
import java.text.SimpleDateFormat

class JSONReporter extends AbstractBuildTimeTrackerReporter {
    JSONReporter(Map<String, String> options, Logger logger) {
        super(options, logger)
    }

    @Override
    def run(List<Timing> timings) {
        long timestamp = new TrueTimeProvider().getCurrentTime()
        String output = getOption("output", "")
        boolean append = getOption("append", "false").toBoolean()
        TimeZone tz = TimeZone.getTimeZone("UTC")
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS'Z'")
        df.setTimeZone(tz)

        File file = new File(output)
        file.getParentFile()?.mkdirs()

        def info = new SysInfo()
        def osId = info.getOSIdentifier()
        def cpuId = info.getCPUIdentifier()
        def maxMem = info.getMaxMemory()
        def measurements = []

        timings.eachWithIndex { it, index ->
            measurements << [
                    timestamp: timestamp,
                    order    : index,
                    task     : it.path,
                    success  : it.success,
                    did_work : it.didWork,
                    skipped  : it.skipped,
                    ms       : it.ms,
                    date     : df.format(new Date(timestamp)),
                    cpu      : cpuId,
                    memory   : maxMem,
                    os       : osId,
            ]
        }

        def data = [
                success     : timings.every { it.success },
                count       : timings.size(),
                measurements: measurements,
        ]

        FileWriter writer = new FileWriter(file, append)
        try {
            writer.write(new JsonBuilder(data).toPrettyString())
            writer.flush()
        } finally {
            writer.close()
        }
    }
}
