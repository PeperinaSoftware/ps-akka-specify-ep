<!-- <nav> -->
- [Akka](../index.html)
- [Getting started & Tutorials](index.html)

<!-- </nav> -->

# Tutorials

## <a href="about:blank#_get_started_with_akka"></a> Get started with Akka

The fastest way to start building with Akka is to install the Akka plugin and run a specification. No Java or Maven setup required — the plugin handles everything.

### <a href="about:blank#_step_1_install_the_akka_plugin"></a> Step 1: Install the Akka plugin

In Claude Code, install the Akka plugin from the AI Marketplace:

```none
/plugin marketplace add akka/ai-marketplace
/plugin install akka@ai-marketplace
/reload-plugins
```

|  | If you are unable to add the marketplace, clone the Akka plugin repository and add it as a local marketplace instead: |

```bash
git clone https://github.com/akka/ai-marketplace.git
```
Then in Claude Code:

```none
/plugin marketplace add /path/to/ai-marketplace
/plugin install akka@ai-marketplace
/reload-plugins
```

|  | Not using Claude Code? Akka supports 30 different AI assist tools. See [Spec-Driven Development](../sdk/spec-driven-development.html) for plugin installation instructions for your tool. |

|  | No plugin capability for your AI assist? If your AI assist tool does not support plugins, install the Akka CLI instead and use `akka specify init <dir>` to provision a new project. See [Spec-Driven Development](../sdk/spec-driven-development.html#_getting_started_with_sdd) for full instructions. |

### <a href="about:blank#_step_2_set_up_your_environment"></a> Step 2: Set up your environment

```none
/akka:setup
```
This ensures the Akka CLI is installed, Java and Maven are available, and your Akka download token is properly configured.

### <a href="about:blank#_step_3_run_your_first_specification"></a> Step 3: Run your first specification

```none
/akka.specify hello-world - A simple greeting agent that responds in different languages and remembers which languages it has used.
```
Akka will start building a specification and then guide you on which next steps you need to execute to build an implementation plan, execute implementation steps, build the service, and test it. See [Spec-Driven Development](../sdk/spec-driven-development.html) for the full SDD workflow.

## <a href="about:blank#_spec_driven_tutorials"></a> Spec-driven tutorials

These tutorials walk you through all of the Spec-Driven Development commands — from writing a specification through clarification, planning, implementation, and build. You use your AI assist to drive the entire workflow.

| Tutorial | Level |
| --- | --- |
| [Spec-first greeting Agent](spec-your-first-agent.html) — Build an Agent using all SDD commands end-to-end | Beginner |

## <a href="about:blank#_code_driven_tutorials"></a> Code-driven tutorials

These tutorials walk you step by step through building Akka systems by writing Java code. They are ideal for understanding how Akka components work under the hood.

### <a href="about:blank#_build_your_first_agent"></a> Build your first Agent

| Tutorial | Level |
| --- | --- |
| [Hello world Agent](author-your-first-service.html) — Build an Agent by hand in Java | Beginner |

### <a href="about:blank#_build_a_multi_agent_system"></a> Build a multi-agent system

Add Agents and other components step-by-step. The final application consists of dynamic orchestration of multiple Agents. A Workflow manages the user query process, handling the sequential steps of Agent selection, plan creation, execution, and summarization.

| Tutorial | Level |
| --- | --- |
| [Part 1: Activity Agent](planner-agent/activity.html) — An Agent (with session memory) that suggests real-world activities using an LLM. | Beginner |
| [Part 2: User preferences](planner-agent/preferences.html) — An Entity (long-term memory) to personalize the suggestions. | Beginner |
| [Part 3: Weather Agent](planner-agent/weather.html) — A weather forecasting Agent that uses an external service as an Agent tool. | Beginner |
| [Part 4: Orchestrate the Agents](planner-agent/team.html) — A Workflow that coordinates long-running calls across the Agents. | Intermediate |
| [Part 5: List by user](planner-agent/list.html) — A View that creates a read-only projection (i.e. a query) of all activity suggestions for a user. | Beginner |
| [Part 6: Dynamic orchestration](planner-agent/dynamic-team.html) — An Agent that creates a dynamic plan using an LLM, and a Workflow that executes the plan. | Advanced |
| [Part 7: Evaluation on changes](planner-agent/eval.html) — A Consumer that streams user preference changes to trigger an Agent. | Intermediate |

### <a href="about:blank#_build_an_ai_rag_agent"></a> Build an AI RAG Agent

Learn how to implement a Retrieval-Augmented Generation (RAG) pipeline with Akka. This series covers end-to-end design of a multi-agent system that performs LLM-assisted reasoning, indexing, and live querying.

| Tutorial | Level |
| --- | --- |
| [Part 1: The Agent](ask-akka-agent/the-agent.html) | Beginner |
| [Part 2: Build a Workflow that indexes knowledge using semantic embeddings](ask-akka-agent/indexer.html) | Intermediate |
| [Part 3: Executing RAG queries](ask-akka-agent/rag.html) | Intermediate |
| [Part 4: Adding UI Endpoints](ask-akka-agent/endpoints.html) | Advanced |

### <a href="about:blank#_build_a_shopping_cart_system"></a> Build a shopping cart system

Explore a complete e-commerce service and learn key Akka concepts by implementing a real-world system. These lessons walk you through defining Entities, handling state, processing commands, and responding to user-specific queries.

| Tutorial | Level |
| --- | --- |
| [Part 1: Build a basic shopping cart with persistent state and command handling](shopping-cart/build-and-deploy-shopping-cart.html) | Beginner |
| [Part 2: Add user-specific lookup with JWT-based authentication](shopping-cart/addview.html) | Intermediate |

## <a href="about:blank#_explore_sample_applications"></a> Explore sample applications

These runnable [code samples](samples.html) showcase common patterns and advanced architectures built with Akka. They are designed for exploration and reference rather than step-by-step instruction.

<!-- <footer> -->
<!-- <nav> -->
[Who uses Akka](../who-uses-akka.html) [Spec-first greeting agent](spec-your-first-agent.html)
<!-- </nav> -->

<!-- </footer> -->

<!-- <aside> -->

<!-- </aside> -->