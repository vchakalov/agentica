package com.agentica.agents.runner;

import com.google.adk.agents.BaseAgent;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.memory.InMemoryMemoryService;
import com.google.adk.plugins.BasePlugin;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.BaseSessionService;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Custom ADK runner that uses a shared session service.
 * Unlike InMemoryRunner which creates its own session service,
 * this runner accepts an external session service to allow session sharing
 * across multiple runner instances.
 */
public class AgenticaRunner extends Runner {

    public AgenticaRunner(BaseAgent agent, String appName, BaseSessionService sessionService) {

        this(agent, appName, sessionService, ImmutableList.of());
    }

    public AgenticaRunner(
            BaseAgent agent,
            String appName,
            BaseSessionService sessionService,
            List<BasePlugin> plugins) {

        super(
            agent,
            appName,
            new InMemoryArtifactService(),
            sessionService,
            new InMemoryMemoryService(),
            plugins
        );
    }

}
