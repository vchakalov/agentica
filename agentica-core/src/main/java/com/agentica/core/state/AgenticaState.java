package com.agentica.core.state;

import com.agentica.core.domain.Event;
import com.agentica.core.workflow.ReplanDecision;
import com.agentica.core.workflow.WorkflowPlan;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * LangGraph4j state for Agentica workflow execution.
 * Tracks event context, workflow plan, escalation state, and execution history.
 */
public class AgenticaState extends AgentState {

    /**
     * State key for the original event.
     */
    public static final String KEY_EVENT = "event";

    /**
     * State key for the event ID.
     */
    public static final String KEY_EVENT_ID = "eventId";

    /**
     * State key for the current workflow plan.
     */
    public static final String KEY_CURRENT_PLAN = "currentPlan";

    /**
     * State key for the plan version.
     */
    public static final String KEY_PLAN_VERSION = "planVersion";

    /**
     * State key for the current node ID.
     */
    public static final String KEY_CURRENT_NODE_ID = "currentNodeId";

    /**
     * State key for node outputs map.
     */
    public static final String KEY_NODE_OUTPUTS = "nodeOutputs";

    /**
     * State key for replan needed flag.
     */
    public static final String KEY_NEEDS_REPLAN = "needsReplan";

    /**
     * State key for escalation reason.
     */
    public static final String KEY_ESCALATION_REASON = "escalationReason";

    /**
     * State key for escalation context.
     */
    public static final String KEY_ESCALATION_CONTEXT = "escalationContext";

    /**
     * State key for the node to resume from after replan.
     */
    public static final String KEY_RESUME_FROM_NODE = "resumeFromNode";

    /**
     * State key for the replan decision.
     */
    public static final String KEY_REPLAN_DECISION = "replanDecision";

    /**
     * State key for the replan LLM prompt.
     */
    public static final String KEY_REPLAN_LLM_PROMPT = "replanLlmPrompt";

    /**
     * State key for the replan LLM response.
     */
    public static final String KEY_REPLAN_LLM_RESPONSE = "replanLlmResponse";

    /**
     * State key for plan history (audit trail).
     */
    public static final String KEY_PLAN_HISTORY = "planHistory";

    /**
     * State key for executed actions (audit trail).
     */
    public static final String KEY_EXECUTED_ACTIONS = "executedActions";

    /**
     * State key for accumulated messages.
     */
    public static final String KEY_MESSAGES = "messages";

    /**
     * LangGraph4j schema definition for AgenticaState.
     * Defines channels for properties that need special handling (appenders).
     * Properties not in the schema use default "last value wins" behavior.
     */
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
        KEY_MESSAGES, Channels.appender(ArrayList::new),
        KEY_PLAN_HISTORY, Channels.appender(ArrayList::new),
        KEY_EXECUTED_ACTIONS, Channels.appender(ArrayList::new)
    );

    /**
     * Constructs a new AgenticaState with the given initial data.
     *
     * @param initData the initial state data
     */
    public AgenticaState(Map<String, Object> initData) {

        super(initData);
    }

    /**
     * Creates an AgenticaState for processing an event with a workflow plan.
     *
     * @param event the event to process
     * @param plan the initial workflow plan
     * @return a new AgenticaState initialized for the event
     */
    public static AgenticaState forEvent(Event event, WorkflowPlan plan) {

        Map<String, Object> initData = new HashMap<>();

        initData.put(KEY_EVENT, event);
        initData.put(KEY_EVENT_ID, event.id());
        initData.put(KEY_CURRENT_PLAN, plan);
        initData.put(KEY_PLAN_VERSION, plan.version());
        initData.put(KEY_NEEDS_REPLAN, false);
        initData.put(KEY_NODE_OUTPUTS, new HashMap<>());

        return new AgenticaState(initData);
    }

    /**
     * Gets the original event.
     */
    public Optional<Event> event() {

        return value(KEY_EVENT);
    }

    /**
     * Gets the event ID.
     */
    public Optional<String> eventId() {

        return value(KEY_EVENT_ID);
    }

    /**
     * Gets the current workflow plan.
     */
    public Optional<WorkflowPlan> currentPlan() {

        return value(KEY_CURRENT_PLAN);
    }

    /**
     * Gets the plan version.
     */
    public Optional<Integer> planVersion() {

        return value(KEY_PLAN_VERSION);
    }

    /**
     * Gets the current node ID.
     */
    public Optional<String> currentNodeId() {

        return value(KEY_CURRENT_NODE_ID);
    }

    /**
     * Gets the node outputs map.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> nodeOutputs() {

        return this.<Map<String, Object>>value(KEY_NODE_OUTPUTS)
            .orElse(new HashMap<>());
    }

    /**
     * Checks if replanning is needed.
     */
    public boolean needsReplan() {

        return this.<Boolean>value(KEY_NEEDS_REPLAN)
            .orElse(false);
    }

    /**
     * Gets the escalation reason.
     */
    public Optional<String> escalationReason() {

        return value(KEY_ESCALATION_REASON);
    }

    /**
     * Gets the escalation context.
     */
    public Optional<String> escalationContext() {

        return value(KEY_ESCALATION_CONTEXT);
    }

    /**
     * Gets the node to resume from after replan.
     */
    public Optional<String> resumeFromNode() {

        return value(KEY_RESUME_FROM_NODE);
    }

    /**
     * Gets the replan decision.
     */
    public Optional<ReplanDecision> replanDecision() {

        return value(KEY_REPLAN_DECISION);
    }

    /**
     * Checks if a replan decision has been made.
     */
    public boolean hasReplanDecision() {

        return replanDecision().isPresent();
    }

    /**
     * Gets the replan LLM prompt.
     */
    public Optional<String> replanLlmPrompt() {

        return value(KEY_REPLAN_LLM_PROMPT);
    }

    /**
     * Gets the replan LLM response.
     */
    public Optional<String> replanLlmResponse() {

        return value(KEY_REPLAN_LLM_RESPONSE);
    }

    /**
     * Gets the plan history.
     */
    @SuppressWarnings("unchecked")
    public List<WorkflowPlan> planHistory() {

        return this.<List<WorkflowPlan>>value(KEY_PLAN_HISTORY)
            .orElse(new ArrayList<>());
    }

    /**
     * Gets the executed actions.
     */
    @SuppressWarnings("unchecked")
    public List<String> executedActions() {

        return this.<List<String>>value(KEY_EXECUTED_ACTIONS)
            .orElse(new ArrayList<>());
    }

    /**
     * Gets the accumulated messages.
     */
    @SuppressWarnings("unchecked")
    public List<Object> messages() {

        return this.<List<Object>>value(KEY_MESSAGES)
            .orElse(new ArrayList<>());
    }

    /**
     * Creates a map of updates to clear the replan state.
     *
     * @return map of state updates to clear replan flags
     */
    public static Map<String, Object> clearReplanUpdates() {

        Map<String, Object> updates = new HashMap<>();

        updates.put(KEY_NEEDS_REPLAN, false);
        updates.put(KEY_ESCALATION_REASON, null);
        updates.put(KEY_ESCALATION_CONTEXT, null);
        updates.put(KEY_RESUME_FROM_NODE, null);
        updates.put(KEY_REPLAN_DECISION, null);

        return updates;
    }

    /**
     * Creates a map of updates to set escalation state.
     *
     * @param reason the escalation reason
     * @param context the escalation context
     * @param nodeId the node ID that triggered escalation
     * @return map of state updates for escalation
     */
    public static Map<String, Object> escalationUpdates(String reason, String context, String nodeId) {

        Map<String, Object> updates = new HashMap<>();

        updates.put(KEY_NEEDS_REPLAN, true);
        updates.put(KEY_ESCALATION_REASON, reason);
        updates.put(KEY_ESCALATION_CONTEXT, context);
        updates.put(KEY_RESUME_FROM_NODE, nodeId);

        return updates;
    }

    /**
     * Creates a map of updates to set the workflow plan.
     *
     * @param plan the new workflow plan
     * @return map of state updates for the plan
     */
    public static Map<String, Object> planUpdates(WorkflowPlan plan) {

        Map<String, Object> updates = new HashMap<>();

        updates.put(KEY_CURRENT_PLAN, plan);
        updates.put(KEY_PLAN_VERSION, plan.version());

        return updates;
    }

    /**
     * Creates a map of updates to record a node output.
     *
     * @param nodeId the node ID
     * @param output the node output
     * @return map of state updates for node output
     */
    public static Map<String, Object> nodeOutputUpdates(String nodeId, Object output) {

        Map<String, Object> nodeOutputs = new HashMap<>();

        nodeOutputs.put(nodeId, output);

        Map<String, Object> updates = new HashMap<>();

        updates.put(KEY_NODE_OUTPUTS, nodeOutputs);
        updates.put(KEY_CURRENT_NODE_ID, nodeId);

        return updates;
    }

}
