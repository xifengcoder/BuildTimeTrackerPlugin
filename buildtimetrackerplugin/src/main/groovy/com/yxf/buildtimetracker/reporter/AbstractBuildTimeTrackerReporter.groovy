package com.yxf.buildtimetracker

import org.gradle.BuildResult
import org.gradle.api.logging.Logger

abstract class AbstractBuildTimeTrackerReporter {
    Map<String, String> options
    Logger logger

    AbstractBuildTimeTrackerReporter(Map<String, String> options, Logger logger) {
        this.options = options
        this.logger = logger
    }

    abstract run(List<Timing> timings)

    String getOption(String name, String defaultVal) {
        options[name] == null ? defaultVal : options[name]
    }

    void onBuildResult(BuildResult result) {}
}
