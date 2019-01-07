package com.sag.bada.jaeger_tracing.example2;

import com.google.common.collect.ImmutableMap;

import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import lib.Tracing;

public class HelloActive {

	private final Tracer tracer;

	private HelloActive(Tracer tracer) {
		this.tracer = tracer;
	}

	private void sayHello(String helloTo) {
		try (Scope scope = tracer.buildSpan("say-hello").startActive(true)) {
			scope.span().setTag("hello-to", helloTo);

			String helloStr = formatString(helloTo);
			printHello(helloStr);
		}
	}

	/**
	 * In example 1 we wrote a program that creates a trace that consists of a
	 * single span. That single span combined two operations performed by the
	 * program, formatting the output string and printing it. Let's move those
	 * operations into stand-alone functions first
	 * 
	 * Note: we have wrapped each function into its own span formatString and
	 * printHello.
	 * 
	 *
	 */
//	private String formatString(String helloTo) {
//		Span span = tracer.buildSpan("formatString").start();
//		try {
//			String helloStr = String.format("Hello, %s!", helloTo);
//			span.log(ImmutableMap.of("event", "string-format", "value", helloStr));
//			return helloStr;
//		} finally {
//			span.finish();
//		}
//	}
//
//	private void printHello(String helloStr) {
//		Span span = tracer.buildSpan("printHello").start();
//		try {
//			System.out.println(helloStr);
//			span.log(ImmutableMap.of("event", "println"));
//		} finally {
//			span.finish();
//		}
//	}

	/**
	 * Propagate the in-process context
	 * 
	 * OpenTracing API for Java provides a better way. Using thread-locals and the
	 * notion of an "active span", we can avoid passing the span through our code
	 * and just access it via tracer
	 * 
	 * In the below code we're making the following changes:
	 * 
	 * 1. We use startActive() method of the span builder instead of start(), which
	 * makes the span "active" by storing it in a thread-local storage. 
	 * 2. startActive() returns a Scope object instead of a Span. Scope is a container
	 * of the currently active span. We access the active span via scope.span().
	 * Once the scope is closed, the previous scope becomes current, thus
	 * re-activating previously active span in the current thread. 
	 * 3. Scope is auto-closable, which allows us to use try-with-resource syntax. 
	 * 4. The boolean parameter in startActive(true) tells the Scope that once it is closed it
	 * should finish the span it represents.
	 * 5. startActive() automatically creates a ChildOf reference to the previously active span, so that we don't have to use
	 * asChildOf() builder method explicitly.
	 * 
	 */

	private String formatString(String helloTo) {
		try (Scope scope = tracer.buildSpan("formatString").startActive(true)) {
			String helloStr = String.format("Hello, %s!", helloTo);
			scope.span().log(ImmutableMap.of("event", "string-format", "value", helloStr));
			return helloStr;
		}
	}

	private void printHello(String helloStr) {
		try (Scope scope = tracer.buildSpan("printHello").startActive(true)) {
			System.out.println(helloStr);
			scope.span().log(ImmutableMap.of("event", "println"));
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("Expecting one argument");
		}

		String helloTo = args[0];
		try (JaegerTracer tracer = Tracing.init("hello-world")) {
			new HelloActive(tracer).sayHello(helloTo);
		}
	}
}