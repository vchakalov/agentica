package com.agentica.core.enums;

/**
 * Status of an event in the processing pipeline.
 */
public enum EventStatus {

  /**
   * Event received but not yet processed.
   */
  PENDING,

  /**
   * Event is currently being processed by the filter agent.
   */
  PROCESSING,

  /**
   * Event was filtered out (no action required).
   */
  SKIPPED,

  /**
   * Event is actionable and a workflow has been created.
   */
  ACTIONABLE,

  /**
   * Event processing completed successfully.
   */
  COMPLETED,

  /**
   * Event processing failed.
   */
  FAILED

}
