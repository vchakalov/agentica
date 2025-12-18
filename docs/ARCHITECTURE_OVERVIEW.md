┌─────────────────────────────────────────────────────────────────────────┐
│                           EXTERNAL SOURCES                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐ │
│  │ Shopify  │  │  Stripe  │  │   Gmail  │  │ WhatsApp │  │  Cron   │ │
│  │ Webhooks │  │ Webhooks │  │  Events  │  │ Messages │  │  Tasks  │ │
│  └─────┬────┘  └─────┬────┘  └─────┬────┘  └─────┬────┘  └────┬────┘ │
│        │             │              │             │             │      │
└────────┼─────────────┼──────────────┼─────────────┼─────────────┼──────┘
│             │              │             │             │
└─────────────┴──────────────┴─────────────┴─────────────┘
↓
┌────────────────────────────────────────────────────────┐
│                     HOOKDECK                           │
│          (Webhook Normalization Service)               │
│  • Receives all webhooks                              │
│  • Normalizes format                                  │
│  • Adds metadata (tenant_id, event_type)             │
└────────────────────┬───────────────────────────────────┘
↓
┌─────────────────────────────────────────────────────────────────────────┐
│                         AGENTICA PLATFORM                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                   API LAYER (Spring Boot)                        │  │
│  │  ┌────────────────┐  ┌────────────────┐  ┌──────────────────┐  │  │
│  │  │   Webhook      │  │   Approval     │  │    Workflow      │  │  │
│  │  │  Controller    │  │   Controller   │  │   Controller     │  │  │
│  │  └───────┬────────┘  └───────┬────────┘  └────────┬─────────┘  │  │
│  └──────────┼────────────────────┼────────────────────┼────────────┘  │
│             │                    │                    │               │
│             ↓                    ↓                    ↓               │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                     SERVICE LAYER                                │  │
│  │                                                                  │  │
│  │  ┌──────────────┐   ┌───────────────┐   ┌──────────────────┐  │  │
│  │  │    Event     │   │   Workflow    │   │    Approval      │  │  │
│  │  │   Service    │──→│    Service    │──→│    Service       │  │  │
│  │  └──────────────┘   └───────┬───────┘   └──────────────────┘  │  │
│  │                              │                                  │  │
│  └──────────────────────────────┼──────────────────────────────────┘  │
│                                 ↓                                     │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                  GOOGLE ADK AGENT LAYER                          │  │
│  │                                                                  │  │
│  │  ┌────────────────┐                                             │  │
│  │  │ Filter Agent   │  → Determines if event is actionable        │  │
│  │  └───────┬────────┘                                             │  │
│  │          │                                                       │  │
│  │          ↓                                                       │  │
│  │  ┌────────────────────┐                                         │  │
│  │  │ Orchestrator Agent │ → Creates execution plan                │  │
│  │  └───────┬────────────┘                                         │  │
│  │          │                                                       │  │
│  │          ↓                                                       │  │
│  │  ┌──────────────────────────────────────────────┐              │  │
│  │  │        Specialized Agents                    │              │  │
│  │  │  ┌───────────┐ ┌───────────┐ ┌────────────┐ │              │  │
│  │  │  │ Support   │ │ Finance   │ │ Marketing  │ │              │  │
│  │  │  │  Agent    │ │  Agent    │ │   Agent    │ │              │  │
│  │  │  └───────────┘ └───────────┘ └────────────┘ │              │  │
│  │  └──────────────────────────────────────────────┘              │  │
│  │                                                                  │  │
│  │  ADK Session State (per workflow):                              │  │
│  │  • event_id, event_type, event_payload                          │  │
│  │  • is_actionable, priority, category                            │  │
│  │  • workflow_plan, current_step, workflow_status                 │  │
│  │                                                                  │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                 ↓                                     │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    WORKFLOW EXECUTOR                             │  │
│  │  • Executes approved workflows                                   │  │
│  │  • Coordinates agent steps                                       │  │
│  │  • Updates workflow status                                       │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                 ↓                                     │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                  INTEGRATION LAYER                               │  │
│  │  ┌──────────┐  ┌─────────┐  ┌───────┐  ┌─────────────┐         │  │
│  │  │ Shopify  │  │ Stripe  │  │ Email │  │  WhatsApp   │         │  │
│  │  │  Client  │  │ Client  │  │Client │  │   Client    │         │  │
│  │  └──────────┘  └─────────┘  └───────┘  └─────────────┘         │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                 ↓                                     │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                  PERSISTENCE LAYER                               │  │
│  │                                                                  │  │
│  │  ┌────────────────────────────────────────────────────────────┐ │  │
│  │  │              PostgreSQL Database                           │ │  │
│  │  │                                                            │ │  │
│  │  │  ┌─────────┐ ┌──────────────┐ ┌────────────────┐        │ │  │
│  │  │  │ tenants │ │    events    │ │workflow_       │        │ │  │
│  │  │  │         │ │              │ │executions      │        │ │  │
│  │  │  └─────────┘ └──────────────┘ └────────────────┘        │ │  │
│  │  │  ┌─────────┐ ┌──────────────┐ ┌────────────────┐        │ │  │
│  │  │  │  users  │ │approval_tasks│ │ agent_configs  │        │ │  │
│  │  │  └─────────┘ └──────────────┘ └────────────────┘        │ │  │
│  │  │                                                            │ │  │
│  │  │  All tables have: tenant_id (multi-tenant isolation)      │ │  │
│  │  └────────────────────────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │               GOOGLE CLOUD SERVICES                              │  │
│  │  • Vertex AI (Gemini models)                                     │  │
│  │  • Vertex AI Session Service (ADK state persistence)             │  │
│  │  • Cloud SQL (managed PostgreSQL)                                │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
↓
┌─────────────────────────────────────────────────────────────────────────┐
│                          FRONTEND (Next.js)                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                 │
│  │  Dashboard   │  │  Approvals   │  │  Workflows   │                 │
│  │    View      │  │    Board     │  │   Monitor    │                 │
│  └──────────────┘  └──────────────┘  └──────────────┘                 │
└─────────────────────────────────────────────────────────────────────────┘


===========================================================================================

┌───────────────────────────────────────────────────────────────────────┐
│                        EVENT LIFECYCLE                                │
└───────────────────────────────────────────────────────────────────────┘

INGESTION PHASE
═══════════════

External Event
↓
Hookdeck (normalizes)
↓
POST /api/webhooks/hookdeck
↓
WebhookController
↓
EventService.ingest()
↓
┌─────────────────────────┐
│  Save to events table   │
│  status: PENDING        │
│  tenant_id: xxx         │
└───────────┬─────────────┘
↓

PROCESSING PHASE
════════════════

@Async WorkflowService.processEvent()
↓
┌─────────────────────────────────────┐
│ 1. Create ADK Session               │
│    - session_id: generated          │
│    - user_id: tenant_id             │
│    - state: {event_id, payload}     │
└───────────┬─────────────────────────┘
↓
┌─────────────────────────────────────┐
│ 2. Run Filter Agent                 │
│    Input: event data                │
│    Output: is_actionable (bool)     │
│    Updates session state            │
└───────────┬─────────────────────────┘
↓
[actionable?]
│
NO ←────┴────→ YES
↓             ↓
Mark event   ┌────────────────────────────┐
as filtered  │ 3. Run Orchestrator Agent  │
│    Input: event + context  │
│    Output: execution_plan  │
│    {                       │
│      steps: [              │
│        {agent, action},    │
│        {agent, action}     │
│      ]                     │
│    }                       │
└─────────┬──────────────────┘
↓
┌─────────────────────────────┐
│ 4. Save WorkflowExecution   │
│    - adk_session_id         │
│    - execution_plan         │
│    - status: AWAITING_      │
│              APPROVAL       │
└─────────┬───────────────────┘
↓
┌─────────────────────────────┐
│ 5. Create ApprovalTask      │
│    - workflow_execution_id  │
│    - proposed_actions       │
│    - status: PENDING        │
└─────────────────────────────┘
↓

APPROVAL PHASE
══════════════

┌──────────────────────────────────────┐
│  WorkflowScheduler                   │
│  @Scheduled(fixedDelay = 30000)      │
│  Polls for approved tasks            │
└──────────┬───────────────────────────┘
↓
[checks approval_tasks table every 30s]
↓
┌──────────────────────────────────────┐
│  Human Reviews in UI                 │
│  • Views proposed plan               │
│  • Can modify steps                  │
│  • Approves or Rejects               │
└──────────┬───────────────────────────┘
↓
POST /api/approvals/{id}/approve
↓
ApprovalService.approve()
↓
┌──────────────────────────────────────┐
│  Update approval_task                │
│  status: APPROVED                    │
│  approved_at: timestamp              │
└──────────────────────────────────────┘
↓

EXECUTION PHASE
═══════════════

WorkflowScheduler detects approval
↓
WorkflowExecutor.executeApproved()
↓
┌──────────────────────────────────────┐
│  1. Resume ADK Session               │
│     Load session by adk_session_id   │
└──────────┬───────────────────────────┘
↓
┌──────────────────────────────────────┐
│  2. Update session state             │
│     workflow_status: APPROVED        │
│     approved_at: timestamp           │
└──────────┬───────────────────────────┘
↓
┌──────────────────────────────────────┐
│  3. Execute Each Step                │
│     For each step in plan:           │
│       • Load specialized agent       │
│       • Run with step config         │
│       • Agent calls integrations     │
│       • Update session state         │
└──────────┬───────────────────────────┘
↓

Example Step Execution:

Step 1: Support Agent sends email
↓
┌──────────────────────────────────────┐
│  SupportAgent                        │
│  • Generates email content (AI)      │
│  • Calls EmailTool                   │
│       ↓                              │
│  EmailClient.send()                  │
│       ↓                              │
│  Gmail API / SMTP                    │
└──────────┬───────────────────────────┘
↓

Step 2: Finance Agent adds funds
↓
┌──────────────────────────────────────┐
│  FinanceAgent                        │
│  • Calculates amount needed          │
│  • Calls IntegrationTool             │
│       ↓                              │
│  StripeClient.addFunds()             │
│       ↓                              │
│  Stripe API                          │
└──────────┬───────────────────────────┘
↓

All steps complete
↓
┌──────────────────────────────────────┐
│  4. Update WorkflowExecution         │
│     status: COMPLETED                │
│     completed_at: timestamp          │
│     result: {summary of actions}     │
└──────────────────────────────────────┘
↓

COMPLETION
══════════

Workflow complete ✓
Event processed ✓
User can view results in dashboard


=================================================================

┌─────────────────────────────────────────────────────────────┐
│                   MODULE ARCHITECTURE                       │
└─────────────────────────────────────────────────────────────┘

                    ┌──────────────────┐
                    │ agentica-common  │
                    │   (utilities)    │
                    └────────┬─────────┘
                             ↑
                             │ depends on
                             │
                    ┌────────┴─────────┐
                    │  agentica-core   │
                    │ (domain models)  │
                    └────────┬─────────┘
                             ↑
                    ┌────────┴────────┬────────────────┐
                    │                 │                │
          ┌─────────┴──────┐  ┌──────┴──────┐  ┌─────┴──────────┐
          │  agentica-     │  │  agentica-  │  │   agentica-    │
          │ infrastructure │  │   agents    │  │  integrations  │
          │  (persistence) │  │  (ADK)      │  │  (external)    │
          └────────┬───────┘  └──────┬──────┘  └────────┬───────┘
                   ↑                 ↑                   ↑
                   └────────┬────────┴──────────┬────────┘
                            │                   │
                   ┌────────┴────────┐          │
                   │  agentica-      │          │
                   │  workflows      │          │
                   │ (orchestration) │          │
                   └────────┬────────┘          │
                            ↑                   │
                            └────────┬──────────┘
                                     │
                            ┌────────┴─────────┐
                            │  agentica-api    │
                            │  (Spring Boot)   │
                            │   [Main App]     │
                            └──────────────────┘
                                     ↑
                                     │ REST API
                                     │
                            ┌────────┴─────────┐
                            │  agentica-web    │
                            │   (Next.js)      │
                            └──────────────────┘

=================================================================

┌─────────────────────────────────────────────────────────────┐
│            LANGGRAPH4J WORKFLOW ORCHESTRATION               │
└─────────────────────────────────────────────────────────────┘

OVERVIEW
════════

The workflow orchestration layer uses LangGraph4j to execute dynamic workflows
designed by the Orchestrator LLM. This enables fully LLM-driven workflow design
with mid-execution replanning capabilities.

ARCHITECTURE FLOW
═════════════════

Event Arrives
      ↓
┌──────────────────────────────────────────────────────────────┐
│  ORCHESTRATOR LLM (Structured Output)                        │
│                                                              │
│  Input: Event details + Available agents list                │
│  Output: WorkflowPlan JSON (validated against schema)        │
│                                                              │
│  The LLM designs a complete workflow:                        │
│    {                                                         │
│      "workflowName": "Handle Customer Complaint",            │
│      "nodes": [                                              │
│        { "id": "analyze", "agentType": "MARKETING" },        │
│        { "id": "respond", "agentType": "FACEBOOK" },         │
│        { "id": "review",  "agentType": "REVIEW" }            │
│      ],                                                      │
│      "edges": [                                              │
│        { "from": "START", "to": "analyze", "type": "DIRECT" }│
│        { "from": "analyze", "to": "respond", "type": "DIRECT"}│
│        { "from": "respond", "to": "review", "type": "DIRECT" }│
│        { "from": "review", "to": "END", "type": "DIRECT" }   │
│      ],                                                      │
│      "entryPoint": "analyze"                                 │
│    }                                                         │
└────────────────────────┬─────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────────────┐
│  WORKFLOW PLAN VALIDATOR                                     │
│                                                              │
│  • Validates required fields (workflowId, nodes, edges)      │
│  • Checks entry point exists in nodes                        │
│  • Validates all nodes have valid agent types                │
│  • Ensures all edge references are valid                     │
│  • Verifies graph connectivity (reachable from entry)        │
└────────────────────────┬─────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────────────┐
│  WORKFLOW BUILDER                                            │
│                                                              │
│  Converts WorkflowPlan → LangGraph4j StateGraph              │
│                                                              │
│  For each node in plan:                                      │
│    1. Look up AgentNode from AgentNodeRegistry               │
│    2. Create async node action wrapping agent execution      │
│    3. Add escalation routing (if needsReplan → orchestrator) │
│                                                              │
│  For each edge in plan:                                      │
│    1. DIRECT edges → graph.addEdge(from, to)                 │
│    2. CONDITIONAL edges → graph.addConditionalEdges(...)     │
│                                                              │
│  Always adds: orchestrator_replan node for escalation        │
└────────────────────────┬─────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────────────┐
│  DYNAMIC WORKFLOW EXECUTOR                                   │
│                                                              │
│  Executes the compiled graph with replan support:            │
│                                                              │
│  while (replanCount <= maxIterations) {                      │
│      graph = workflowBuilder.build(currentPlan)              │
│                                                              │
│      for (nodeOutput : graph.stream(state)) {                │
│                                                              │
│          if (state.hasReplanDecision()) {                    │
│              // Agent escalated - orchestrator responded     │
│              currentPlan = applyReplan(decision)             │
│              break; // Rebuild graph                         │
│          }                                                   │
│                                                              │
│          if (currentNode == END) {                           │
│              return ExecutionResult.completed()              │
│          }                                                   │
│      }                                                       │
│  }                                                           │
└──────────────────────────────────────────────────────────────┘


LANGGRAPH4J EXECUTION MODEL
═══════════════════════════

┌─────────────────────────────────────────────────────────────────────────────┐
│                    COMPILED LANGGRAPH4J WORKFLOW                             │
│                                                                              │
│   ┌─────────┐    ┌───────────┐    ┌──────────┐    ┌────────┐               │
│   │Marketing│───→│ Facebook  │───→│  Review  │───→│  END   │               │
│   │  Node   │    │   Node    │    │   Node   │    │        │               │
│   └────┬────┘    └─────┬─────┘    └────┬─────┘    └────────┘               │
│        │               │               │                                     │
│        │ needsReplan?  │ needsReplan?  │ needsReplan?                       │
│        └───────────────┴───────────────┘                                     │
│                        │                                                     │
│                        ↓                                                     │
│              ┌──────────────────┐                                           │
│              │   ORCHESTRATOR   │ ← Replan node (always present)            │
│              │     REPLAN       │                                           │
│              └────────┬─────────┘                                           │
│                       │                                                      │
│                       ↓                                                      │
│              ReplanDecision:                                                 │
│              • MODIFY_PLAN → Replace entire workflow                        │
│              • ADD_NODES → Extend current workflow                          │
│              • RETRY_WITH_GUIDANCE → Retry node with hints                  │
│              • ABORT → Stop execution                                        │
│                       │                                                      │
│                       ↓                                                      │
│              Rebuild graph with new plan, resume execution                   │
└──────────────────────────────────────────────────────────────────────────────┘


AGENT NODE ARCHITECTURE
═══════════════════════

Two agent patterns exist for different purposes:

┌─────────────────────────────────────────────────────────────────────────────┐
│  AGENT PATTERN                │  PURPOSE                                     │
├───────────────────────────────┼──────────────────────────────────────────────┤
│  *AgentNode                   │  LangGraph4j workflow nodes                  │
│  (MarketingAgentNode,         │  • State management (AgenticaState)          │
│   FacebookAgentNode,          │  • Escalation support (needsReplan)          │
│   SupportAgentNode,           │  • Prompt building from event context        │
│   ReviewAgentNode)            │  • Registered in AgentNodeRegistry           │
├───────────────────────────────┼──────────────────────────────────────────────┤
│  *AgentImpl                   │  Agents with external tools                  │
│  (FacebookAgentImpl,          │  • MCP tool connections                      │
│   OrchestratorAgentImpl)      │  • Complex initialization                    │
│                               │  • Reusable outside workflow context         │
└─────────────────────────────────────────────────────────────────────────────┘

Key Relationships:
• FacebookAgentNode DELEGATES TO FacebookAgentImpl (for MCP tool access)
• MarketingAgentNode is STANDALONE (no external tools needed)
• SupportAgentNode is STANDALONE (no external tools needed)
• ReviewAgentNode is STANDALONE (always escalates for human approval)


AGENTICA STATE (LANGGRAPH4J)
════════════════════════════

┌─────────────────────────────────────────────────────────────────────────────┐
│  AgenticaState extends AgentState                                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Event Context:                                                              │
│  • event           : Event          ← Original event being processed         │
│  • eventId         : String         ← Event ID for correlation               │
│                                                                              │
│  Workflow Tracking:                                                          │
│  • currentPlan     : WorkflowPlan   ← Current workflow plan                  │
│  • planVersion     : int            ← Version number for audit               │
│  • currentNodeId   : String         ← Currently executing node               │
│  • nodeOutputs     : Map<String, Object>  ← Outputs from completed nodes     │
│                                                                              │
│  Escalation State:                                                           │
│  • needsReplan        : boolean     ← Flag to route to orchestrator          │
│  • escalationReason   : String      ← Why escalation was triggered           │
│  • escalationContext  : String      ← Additional context for orchestrator    │
│  • resumeFromNode     : String      ← Which node triggered escalation        │
│                                                                              │
│  Replan Result:                                                              │
│  • replanDecision     : ReplanDecision ← Orchestrator's replan decision      │
│                                                                              │
│  Audit Trail (Appender Channels):                                            │
│  • planHistory        : List<WorkflowPlan>   ← All plan versions             │
│  • executedActions    : List<String>         ← Actions taken                 │
│  • messages           : List<Object>         ← Accumulated messages          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘


KEY CLASSES (agentica-workflows)
════════════════════════════════

┌─────────────────────────────────────────────────────────────────────────────┐
│  WorkflowBuilder                                                             │
│  Location: agentica-workflows/builder/WorkflowBuilder.java                   │
│                                                                              │
│  Responsibilities:                                                           │
│  • Converts WorkflowPlan → CompiledGraph<AgenticaState>                      │
│  • Adds agent nodes from AgentNodeRegistry                                   │
│  • Adds direct and conditional edges from plan                               │
│  • Adds escalation routing to orchestrator_replan node                       │
│  • Creates the replan node (calls OrchestratorAgent.replan())                │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  DynamicWorkflowExecutor                                                     │
│  Location: agentica-workflows/executor/DynamicWorkflowExecutor.java          │
│                                                                              │
│  Responsibilities:                                                           │
│  • Executes workflow with dynamic replanning support                         │
│  • Handles mid-workflow escalation and plan modifications                    │
│  • Enforces max replan iterations (configurable)                             │
│  • Returns ExecutionResult with status and plan history                      │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  WorkflowPlanValidator                                                       │
│  Location: agentica-workflows/validator/WorkflowPlanValidator.java           │
│                                                                              │
│  Responsibilities:                                                           │
│  • Validates WorkflowPlan before execution                                   │
│  • Checks required fields, entry point, node types                           │
│  • Validates edge references and graph connectivity                          │
│  • Throws WorkflowValidationException on failure                             │
└─────────────────────────────────────────────────────────────────────────────┘


KEY CLASSES (agentica-agents)
═════════════════════════════

┌─────────────────────────────────────────────────────────────────────────────┐
│  AgentNodeRegistry                                                           │
│  Location: agentica-agents/registry/AgentNodeRegistry.java                   │
│                                                                              │
│  Responsibilities:                                                           │
│  • Maps AgentType enum → AgentNode implementations                           │
│  • Auto-discovers AgentNode beans via Spring injection                       │
│  • Provides get(AgentType) for WorkflowBuilder                               │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  AgentNode Interface                                                         │
│  Location: agentica-agents/node/AgentNode.java                               │
│                                                                              │
│  Methods:                                                                    │
│  • execute(state, instruction, config) → AgentNodeResult                     │
│  • getName() → String                                                        │
│  • getDescription() → String                                                 │
│                                                                              │
│  AgentNodeResult:                                                            │
│  • success(output) - Normal completion                                       │
│  • escalate(reason, context) - Trigger replan                                │
│  • fromException(e) - Error handling                                         │
└─────────────────────────────────────────────────────────────────────────────┘


KEY CLASSES (agentica-core)
═══════════════════════════

┌─────────────────────────────────────────────────────────────────────────────┐
│  WorkflowPlan                                                                │
│  Location: agentica-core/workflow/WorkflowPlan.java                          │
│                                                                              │
│  Fields:                                                                     │
│  • workflowId     : String                                                   │
│  • workflowName   : String                                                   │
│  • description    : String                                                   │
│  • nodes          : List<WorkflowNode>                                       │
│  • edges          : List<WorkflowEdge>                                       │
│  • entryPoint     : String                                                   │
│  • initialState   : Map<String, Object>                                      │
│  • version        : int                                                      │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  ReplanDecision                                                              │
│  Location: agentica-core/workflow/ReplanDecision.java                        │
│                                                                              │
│  Actions:                                                                    │
│  • MODIFY_PLAN       : Replace entire workflow with new plan                 │
│  • ADD_NODES         : Add new nodes/edges to existing plan                  │
│  • RETRY_WITH_GUIDANCE: Retry current node with additional context           │
│  • ABORT             : Stop workflow execution                               │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  AgenticaState                                                               │
│  Location: agentica-core/state/AgenticaState.java                            │
│                                                                              │
│  Note: Placed in agentica-core to avoid circular dependency between          │
│        agentica-agents and agentica-workflows modules.                       │
│                                                                              │
│  LangGraph4j Schema (appender channels only):                                │
│  • KEY_MESSAGES           → Channels.appender()                              │
│  • KEY_PLAN_HISTORY       → Channels.appender()                              │
│  • KEY_EXECUTED_ACTIONS   → Channels.appender()                              │
└─────────────────────────────────────────────────────────────────────────────┘


CONFIGURATION
═════════════

application.yml:
```yaml
agentica:
  workflow:
    max-replan-iterations: 5     # Max times orchestrator can replan
    plan-history-enabled: true   # Store all plan versions for audit
```

=================================================================


