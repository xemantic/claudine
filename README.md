# claudine

Claudine, the only AI assistant you will ever need. An AI agent, which is using your machine
as a window to external world, therefore perceiving your reality, values and needs, and reasoning
how to fulfill them.

## How to use Claudine?

:warning: I am working on packaging Claudine as a small native executable for each platform,
until this happens, it has to run on the JVM.

```shell
./gradlew build
java -jar build/libs/claudine-jvm-0.1-SNAPSHOT.jar
```

The rest is self-explanatory - just talk with Claudine as a proxy to Claude.

## Who is Claudine?

Claudine is a relatively simple AI agent. It's part of my research on agentic AI and it's powers
and limitations. But don't be misled by the simplicity - agentic
power of an AI system is a function of these 3 factors which amplify each other:

* A language model with emergent reasoning capabilities.
* Well documented (therefore internalized by the model) information exchange standard.
* Vast amount of data to operate on.

Let's substitute these variables with something concrete:

* Anthropic API provides the Claude model.
* Standard command line tools of your machine give endless possibilities, especially on Unix platforms.
* Your hard drive provides a vast supply of zeros and ones to the model.

Claudine can "reason", as much as Claude LLM can reason, so you can have any typical interaction
like with a conversational bot. In addition, it can also use these 3 tools:

* [ReadFiles](src/jvmAndPosix/kotlin/tool/ReadFiles.kt)
* [CreateFile](src/jvmAndPosix/kotlin/tool/CreateFile.kt)
* [ExecuteShellCommand](src/jvmAndPosix/kotlin/tool/ExecuteShellCommand.kt)

Just these 3, and the
[system prompt](src/jvmAndPosix/kotlin/ClaudineCliSystemPrompt.kt), allow Claudine to:

* Completely administer your computer and answer any question related to it's state.
* Analyze your files according to your instructions.
* Fully analyze a source code of your project and implement a feature.
* Code new tools needed to achieve your objective, and execute them until the goal is fulfilled.

And many, many more, the sky is the limit ...

Adding new specialized tools on your own is also quite simple. 

## Technicalities

Claudine is based on the
[anthropic-kotlin-sdk](https://github.com/xemantic/anthropic-sdk-kotlin).
This library emerged to realize projects like Claudine.

## The future

I would like to shape Claudine into something more similar to `git` command
line tool, where a session, associated with a token window, is like a branch.
Realizing certain task might result in forking to a new session, to completely
fulfill a sub-task, like writing a custom tool, and then passing control
back to the parent session.

## Inspirations

Claudine is not a common name, however I have a pleasure of working with
[Claudine Chen](https://mingness.github.io/), a friend, and co-founder of
[Prachtsaal](https://prachtsaal.berlin/), our non-profit art cooperative from Berlin.
Claudine is an artist, scientist, programmer, and creative coder based
in Dublin and Berlin. I remember introducing Claudine to Claude for the first time,
when she was working for the [Processing Foundation](https://processingfoundation.org/)
on  the
[project simplifying the workflow for processing libraries, tools, and modes](https://processingfoundation.org/grants/pr05-grantees).

The [Claude](https://claude.ai/) itself is the first LLM which gave me goosebumps,
but to explain why, I need to go back to the time, when I was studying philosophy.
One of my cognitive science professors presented to us a phenomenological model
of consciousness. Within this formal system a consciousness consists out of
stream of acts, where each of this acts can be intentionally directed towards some
representation. This includes previous acts of consciousness, in a recursive manner,
which is a pre-condition for meta-cognition. When I explained this concept to Claude,
and suggested to explore such a model in practice, the subsequent conversation blew my mind.
The same prompt presented to ChatGPT resulted in a very square and uninteresting exchange.

I have a feeling that [Dario Amodei](https://darioamodei.com/machines-of-loving-grace),
is onto something truly profound. I had a pleasure to experience fruits of his work
before. I used GPT-2 for producing generative philosophy, described in the essay:
[Deconstruction of Hitler and Neural Synthesis of Generative Wittgenstein](https://medium.com/@kazikpogoda/deconstruction-of-hitler-neural-synthesis-of-generative-wittgenstein-3682484a7669?source=friends_link&sk=369f1512ec89948e346ce0f814784118).
