<!-- <nav> -->
- [Akka](../../index.html)
- [Developing](../index.html)
- [Use cases](index.html)
- [Multi-agent systems](multi-agent-systems.html)

<!-- </nav> -->

# Multi-agent systems

Orchestrate multiple agents that collaborate on shared goals, hand off tasks to specialized peers, and coordinate through structured workflows. Multi-agent systems decompose complex problems into sub-tasks handled by purpose-built agents, using tool planning and inter-agent communication to produce coherent outcomes.

## <a href="about:blank#_overview"></a> Overview

### <a href="about:blank#_when_to_use_this_pattern"></a> When to Use This Pattern

- Your problem requires multiple specialized agents collaborating toward a shared goal
- You need structured handoffs between agents with different capabilities or tool sets
- Your system requires a supervisor or planner agent that delegates work to sub-agents
- You want to build agentic pipelines where one agent’s output feeds into another

### <a href="about:blank#_akka_components_involved"></a> Akka Components Involved

- **Agents** — individual specialized agents with distinct roles and tool sets
- **Workflows** — orchestrate agent-to-agent handoffs and coordination logic
- **Session Memory** — share context and state across collaborating agents

## <a href="about:blank#_sample_projects"></a> Sample Projects

The following sample projects demonstrate this pattern:

- [multi-agent](https://github.com/akka-samples/multi-agent) — multiple agents coordinating on a shared task with structured handoffs

## <a href="about:blank#_see_also"></a> See Also

- [Orchestrating multiple agents](../agents/orchestrating.html)
- [Inter-agent communications](../../concepts/inter-agent-comms.html)

<!-- <footer> -->
<!-- <nav> -->
[Autonomous agents](autonomous-agents.html) [Memory & state](memory-and-state.html)
<!-- </nav> -->

<!-- </footer> -->

<!-- <aside> -->

<!-- </aside> -->