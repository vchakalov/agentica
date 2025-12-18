package com.agentica.infrastructure.event;

import com.agentica.core.domain.Event;

import org.springframework.context.ApplicationEvent;

/**
 * Spring application event published when an event is marked as actionable.
 * This event triggers the workflow orchestration process.
 */
public class ActionableEventPublished extends ApplicationEvent {

    private final Event event;

    /**
     * Creates a new ActionableEventPublished event.
     *
     * @param source the object on which the event initially occurred
     * @param event  the actionable domain event
     */
    public ActionableEventPublished(Object source, Event event) {

        super(source);

        this.event = event;
    }

    /**
     * Returns the actionable domain event.
     *
     * @return the event that was marked as actionable
     */
    public Event getEvent() {
        return event;
    }

}
