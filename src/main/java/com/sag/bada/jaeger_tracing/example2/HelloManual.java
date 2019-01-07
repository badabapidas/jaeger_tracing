package com.sag.bada.jaeger_tracing.example2;

import com.google.common.collect.ImmutableMap;

import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Span;
import io.opentracing.Tracer;
import lib.Tracing;

public class HelloManual {

	private final Tracer tracer;

	private HelloManual(Tracer tracer) {
		this.tracer = tracer;
	}

	private void sayHello(String helloTo) {
		Span span = tracer.buildSpan("say-hello").start();
		span.setTag("hello-to", helloTo);

		String helloStr = formatString(span, helloTo);
		printHello(span, helloStr);

		span.finish();
	}

	/**
	 * If we run this code we can see we got three spans, but there is a problem
	 * here. The first hexadecimal segment of the output represents Jaeger trace ID,
	 * yet they are all different. If we search for those IDs in the UI each one
	 * will represent a stand-alone trace with a single span. That's not what we
	 * wanted!
	 * 
	 * What we really wanted was to establish causal relationship between the two
	 * new spans to the root span started in main(). We can do that by passing an
	 * additional option asChildOf to the span builder:
	 * 
	 * 
	 */
	private String formatString(Span rootSpan, String helloTo) {
		Span span = tracer.buildSpan("formatString").asChildOf(rootSpan).start();
		try {
			String helloStr = String.format("Hello, %s!", helloTo);
			span.log(ImmutableMap.of("event", "string-format", "value", helloStr));
			return helloStr;
		} finally {
			span.finish();
		}
	}

	private void printHello(Span rootSpan, String helloStr) {
		Span span = tracer.buildSpan("printHello").asChildOf(rootSpan).start();
		try {
			System.out.println(helloStr);
			span.log(ImmutableMap.of("event", "println"));
		} finally {
			span.finish();
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("Expecting one argument");
		}

		String helloTo = args[0];
		try (JaegerTracer tracer = Tracing.init("hello-world")) {
			new HelloManual(tracer).sayHello(helloTo);
		}
	}
}