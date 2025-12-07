package com.agentica.workflows.scheduler;

/**
 * Scheduler that polls for approved workflows and executes them.
 */
public interface WorkflowScheduler {

    /**
     * Starts the scheduler.
     */
    void start();

    /**
     * Stops the scheduler.
     */
    void stop();

    /**
     * Checks if the scheduler is running.
     */
    boolean isRunning();

    /**
     * Manually triggers a poll for approved workflows.
     */
    void pollNow();

}
