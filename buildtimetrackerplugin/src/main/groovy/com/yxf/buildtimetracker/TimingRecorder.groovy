package com.yxf.buildtimetracker

import com.yxf.buildtimetracker.utils.Clock
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState

class Timing {
    long ms //耗时
    String path //task路径
    boolean success //是否执行成功
    boolean didWork //该task是否有事情要做
    boolean skipped //该task是否被跳过执行

    Timing(long ms, String path, boolean success, boolean didWork, boolean skipped) {
        this.ms = ms
        this.path = path
        this.success = success
        this.didWork = didWork
        this.skipped = skipped
    }
}

class TimingRecorder extends BuildAndTaskExecutionListenerAdapter implements TaskExecutionListener {
    private Clock clock
    private List<Timing> timings = []
    private BuildTimeTrackerPlugin plugin

    TimingRecorder(BuildTimeTrackerPlugin plugin) {
        this.plugin = plugin
    }

    @Override
    void beforeExecute(Task task) {
        System.out.println("beforeExecute: " + task.name);
        clock = new Clock()
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        System.out.println("afterExecute: " + task.name + ", path: " + task.getPath());
        timings << new Timing(
                clock.getTimeInMs(),
                task.getPath(),
                taskState.getFailure() == null,
                taskState.getDidWork(),
                taskState.getSkipped()
        )
    }

    @Override
    void buildFinished(BuildResult result) {
        System.out.println("buildFinished" + plugin.reporters.size());
        plugin.reporters.each { it ->
            it.run(timings)
            it.onBuildResult(result)
        }
    }

    List<String> getTasks() {
        timings*.path
    }

    Timing getTiming(String path) {
        timings.find { it.path == path }
    }
}
