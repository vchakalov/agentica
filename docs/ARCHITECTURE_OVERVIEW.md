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



