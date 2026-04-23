<!-- <nav> -->
- [Akka](../../index.html)
- [Getting started & Tutorials](../index.html)
- [Multi-agent planner](index.html)

<!-- </nav> -->

# Build an AI multi-agent planner

|  | **New to Akka? Start here:**

Use the [Build your first agent with Spec-Driven Development](../spec-your-first-agent.html) guide to use your AI assistant for implementing a simple agentic service, running it locally and interacting with it. |
This guide starts with creating an agent that suggests real-world activities. We will incorporate more components in separate parts of the guide, and at the end we will have a multi-agent system with dynamic planning and orchestration capabilities.

1. [Activity agent](activity.html) — An Agent (with session memory) that suggests real-world activities using an LLM.
2. [User preferences](preferences.html) — An Entity (long-term memory) to personalize the suggestions.
3. [Weather agent](weather.html) — A weather forecasting Agent that uses an external service as an agent tool.
4. [Orchestrate the agents](team.html) — A Workflow that coordinates long-running calls across the agents.
5. [List by user](list.html) — A View that creates a read-only projection (i.e. a query) of all activity suggestions for a user.
6. [Dynamic orchestration](dynamic-team.html) — An Agent that creates a dynamic plan using an LLM, and a Workflow that executes the plan.
7. [Evaluation on changes](eval.html) — A Consumer that streams user preference changes to trigger an Agent.

<!-- <footer> -->
<!-- <nav> -->
[Hello world agent](../author-your-first-service.html) [Activity agent](activity.html)
<!-- </nav> -->

<!-- </footer> -->

<!-- <aside> -->

<!-- </aside> -->