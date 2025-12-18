package com.agentica.infrastructure.service;

import com.agentica.agents.filter.FilterAgent;
import com.agentica.core.domain.Event;
import org.springframework.stereotype.Component;

@Component
public class FilterServiceImpl {


  public FilterAgent.FilterResult filterEvent(final Event event) {

    if (isPredefined(event)) {

      return FilterAgent.FilterResult.actionable(
          "Event matches predefined actionable criteria.",
          "predefined",
          10
      );
    }

    return FilterAgent.FilterResult.actionable(
        "Event requires action.",
        "general",
        5
    );
  }

  public boolean isPredefined(final Event event) {

    // include only fb comments for now
    if (event.eventType().startsWith("facebook.comment.")) {

      return true;
    }

    return false;
  }
}
