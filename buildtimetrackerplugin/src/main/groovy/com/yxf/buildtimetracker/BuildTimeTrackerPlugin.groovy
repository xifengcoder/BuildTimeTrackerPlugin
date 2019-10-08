package com.yxf.buildtimetracker

import com.yxf.buildtimetracker.AbstractBuildTimeTrackerReporter
import com.yxf.buildtimetracker.TimingRecorder
import com.yxf.buildtimetracker.reporter.CSVReporter
import com.yxf.buildtimetracker.reporter.CSVSummaryReporter
import com.yxf.buildtimetracker.reporter.JSONReporter
import com.yxf.buildtimetracker.reporter.SummaryReporter
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger

class BuildTimeTrackerPlugin implements Plugin<Project> {
    def REPORTERS = [
            summary   : SummaryReporter,
            csv       : CSVReporter,
            csvSummary: CSVSummaryReporter,
            json      : JSONReporter
    ]

    Logger logger

    NamedDomainObjectCollection<ReporterExtension> reporterExtensions

    @Override
    void apply(Project project) {
        System.out.println("BuildTimeTrackerPlugin");
        this.logger = project.logger
        project.extensions.create("buildtimetracker", BuildTimeTrackerExtension)
        //为ReporterExtension对象集合创建一个Container.
        reporterExtensions = project.buildtimetracker.extensions.reporters = project.container(ReporterExtension)
        project.gradle.addBuildListener(new TimingRecorder(this))
    }

    /**
     * @return
     */
    List<AbstractBuildTimeTrackerReporter> getReporters() {
        reporterExtensions.collect { ext ->
            System.out.println("getReporters ext.name = " + ext.name);
            if (REPORTERS.containsKey(ext.name)) {
                return REPORTERS.get(ext.name).newInstance(ext.options, logger)
            }
        }.findAll { ext -> ext != null }
    }
}

class BuildTimeTrackerExtension {
    // Not in use at the moment.
}

class ReporterExtension {
    final String name
    final Map<String, String> options = [:]

    ReporterExtension(String name) {
        this.name = name
    }

    @Override
    String toString() {
        return name
    }

    def methodMissing(String name, args) {
        // I'm feeling really, really naughty.
        if (args.length == 1) {
            options[name] = args[0].toString()
        } else {
            throw new MissingMethodException(name, this.class, args)
        }
    }
}
