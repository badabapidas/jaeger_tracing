package com.sag.bada.jaeger_tracing.example1;

import com.google.common.collect.ImmutableMap;

import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Span;
import io.opentracing.Tracer;
import lib.Tracing;

public class HelloWithTracer {

	private final Tracer tracer;

	private HelloWithTracer(Tracer tracer) {
		this.tracer = tracer;
	}

	/**
	 * 
	 * A trace is a directed acyclic graph of spans. A span is a logical
	 * representation of some work done in your application. Each span has these
	 * minimum attributes: an operation name, a start time, and a finish time.
	 * 
	 * Let's create a trace that consists of just a single span. To do that we need
	 * an instance of the io.opentracing.Tracer.
	 * 
	 * @param helloTo
	 */
	private void sayHello(String helloTo) {

		/**
		 * 1. a tracer instance is used to create a span builder via buildSpan(). 2.
		 * each span is given an operation name, "say-hello" in this case 3. builder is
		 * used to create a span via start() 4. the start and end timestamps of the span
		 * will be captured automatically by the tracer implementation
		 */
		Span span = tracer.buildSpan("say-hello").start();

		/**
		 * In the case of Hello Bapi, the string "Bapi" is a good candidate for a span
		 * tag, since it applies to the whole span and not to a particular moment in
		 * time
		 */
		span.setTag("hello-to", helloTo);

		/**
		 * Our hello program is so simple that it's difficult to find a relevant example
		 * of a log, but let's try. Right now we're formatting the helloStr and then
		 * printing it. Both of these operations take certain time, so we can log their
		 * completion
		 * 
		 * 
		 * The OpenTracing Specification also recommends all log statements to contain
		 * an event field that describes the overall event being logged, with other
		 * attributes of the event provided as additional fields.
		 */
		String helloStr = String.format("Hello, %s!", helloTo);
		span.log(ImmutableMap.of("event", "string-format", "value", helloStr));

		System.out.println(helloStr);
		span.log(ImmutableMap.of("event", "println"));

		// each span must be finished by calling its finish() function
		span.finish();
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("Expecting one argument");
		}

		String helloTo = args[0];
		/**
		 * Note that we are passing a string hello-world to the init method. It is used
		 * to mark all spans emitted by the tracer as originating from a hello-world
		 * service.
		 */
		try (JaegerTracer tracer = Tracing.init("hello-world")) {
			new HelloWithTracer(tracer).sayHello(helloTo);
		}
	}
}
