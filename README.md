# jaeger_tracing

A work thorough with Jaeger tracing tool

## Installing

This repository uses Maven to manage dependencies. To install all dependencies, run:

```
cd opentracing-tutorial/java
mvn package
```


All subsequent commands in the tutorials should be executed relative to this `java` directory.

## Lessons

* [Lesson 01 - Hello World](./src/main/java/com/sag/bada/jaeger_tracing/example1)
  * Instantiate a Tracer
  * Create a simple trace
  * Annotate the trace
* [Lesson 02 - Context and Tracing Functions](./src/main/java/com/sag/bada/jaeger_tracing/example2)
  * Trace individual functions
  * Combine multiple spans into a single trace
  * Propagate the in-process context
* [Lesson 03 - Tracing RPC Requests](./src/main/java/com/sag/bada/jaeger_tracing/example3)
  * Trace a transaction across more than one microservice
  * Pass the context between processes using `Inject` and `Extract`
  * Apply OpenTracing-recommended tags
* [Lesson 04 - Baggage](./src/main/java/com/sag/bada/jaeger_tracing/example4)
  * Understand distributed context propagation
  * Use baggage to pass data through the call graph
* [Extra Credit](./src/main/java/extracredit)
  * Use existing open source instrumentation
