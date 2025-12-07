package com.agentica.agents.filter;

import com.agentica.core.domain.Event;

/**
 * Agent responsible for filtering incoming events to determine
 * if they require action from the system.
 */
public interface FilterAgent {

    /**
     * Evaluates an event to determine if it requires action.
     *
     * @param event the event to evaluate
     * @return a result containing the decision and reasoning
     */
    FilterResult filter(Event event);

    /**
     * Result of the filter agent evaluation.
     */
    record FilterResult(

            boolean isActionable,

            String reasoning,

            String category,

            int priority

    ) {

        /**
         * Creates a result indicating the event requires action.
         */
        public static FilterResult actionable(String reasoning, String category, int priority) {
            return new FilterResult(true, reasoning, category, priority);
        }

        /**
         * Creates a result indicating the event should be filtered out.
         */
        public static FilterResult notActionable(String reasoning) {
            return new FilterResult(false, reasoning, null, 0);
        }

    }

}
