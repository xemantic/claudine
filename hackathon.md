# Claudine: meta-cognitive and self-modifying AI agent

An AI agent, which can think about own thinking, and modify/extend its own code.

## Description

Problem: we have thinking/reasoning LLMs, but do we have thinking AI agents?

We need agents which can:

- self-reflect
- modify own code
- write own prompts
- extend itself with new tools

## GitHub Repository

https://github.com/xemantic/claudine

## Demo Video Link

## Team

June Wang <yujuinwang@gmail.com>

Shenoa Chee <shenoachee@gmail.com>

Robert John <trojrobert@gmail.com>

Kazik Pogoda <morisil@xemantic.com>

## Story

During the hackathon Claudine:

- modified itself adding ElevenLabs [TranscribeAudio](src/commonMain/kotlin/tool/TranscribeAudio.kt) tool.
- an order management system (React, 3 Java microservices/Kubernetes): estimated cost 2-3 man months, generation time: 30m, compute: ~$2
- Berlin real estate pricing simulation model

And many more.

## Video script

### Clear identification of the problem you're solving

Problem: we have thinking/reasoning LLMs, but do we have thinking AI agents?

We need agents which can:

- self-reflect
- modify own code
- write own prompts
- extend itself with new tools

Then they can achieve much more by:

- conducting personal research (legal advice, flight booking - pricing simulation, food orders, etc.)
- generate higher quality code (complete flawless, tested software products)
- automate scientific research (science is the next frontier)

### Detailed explanation of your AI solution

- Built with Anthropic API (custom agentic framework)
- Kotlin multiplaform - separate small Unix binary for every platform
- Model Context Protocol compatible (we are contributing to MCP)

### Demonstration of key features with a live walkthrough



https://xemantic.com/ai/claudine/video
