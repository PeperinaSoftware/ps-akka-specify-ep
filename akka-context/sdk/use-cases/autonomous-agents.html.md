<!-- <nav> -->
- [Akka](../../index.html)
- [Developing](../index.html)
- [Use cases](index.html)
- [Autonomous agents](autonomous-agents.html)

<!-- </nav> -->

# Autonomous agents

Build agents that operate independently to process data, make decisions, and take actions without requiring constant human oversight. Autonomous agents leverage timed actions for scheduled processing, workflows for multi-step decision pipelines, and durable state to track progress across long-running operations.

## <a href="about:blank#_overview"></a> Overview

### <a href="about:blank#_when_to_use_this_pattern"></a> When to Use This Pattern

- You need an agent that runs on a schedule to process incoming data or events
- Your system requires automated decision-making with minimal human intervention
- You want to build monitoring agents that detect anomalies and take corrective action
- You need background processing pipelines that classify, summarize, or tag content

### <a href="about:blank#_akka_components_involved"></a> Akka Components Involved

- **Agents** — autonomous decision-making logic and LLM-powered analysis
- **Timed Actions** — schedule periodic agent invocations and polling intervals
- **Workflows** — orchestrate multi-step autonomous processing pipelines

## <a href="about:blank#_sample_projects"></a> Sample Projects

The following sample projects demonstrate this pattern:

- [release-note-summarizer](https://github.com/akka-samples/changelog-agent) — agent that autonomously summarizes release notes from upstream sources
- [medical-discharge-tagging](https://github.com/akka-samples/medical-tagging-agent) — agent that classifies and tags medical discharge records
- [temperature-monitoring](https://github.com/akka-samples/temperature-monitoring-agent) — agent that monitors IoT sensor data and triggers alerts

## <a href="about:blank#_see_also"></a> See Also

- [Agents](../agents.html)
- [Timers](../timed-actions.html)
- [Implementing Workflows](../workflows.html)

<!-- <footer> -->
<!-- <nav> -->
[Conversational AI](conversational-ai.html) [Multi-agent systems](multi-agent-systems.html)
<!-- </nav> -->

<!-- </footer> -->

<!-- <aside> -->

<!-- </aside> -->