/**
 * Instrumenting the Client
 * 
 * 
 * Inter-Process Context Propagation
 * 
 * Since the only change we made in the Hello.java app was to replace two operations with HTTP calls, the tracing story remains the same - we get a trace with three spans, 
 * all from hello-world service. But now we have two more microservices participating in the transaction and we want to see them in the trace as well. In order to continue 
 * the trace over the process boundaries and RPC calls, we need a way to propagate the span context over the wire. The OpenTracing API provides two functions in the Tracer 
 * interface to do that, inject(spanContext, format, carrier) and extract(format, carrier).
 * 
 * 
 */

package com.sag.bada.jaeger_tracing.example3.solution;

import java.io.IOException;

import com.google.common.collect.ImmutableMap;

import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import lib.Tracing;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Hello {

	private final Tracer tracer;
	private final OkHttpClient client;

	private Hello(Tracer tracer) {
		this.tracer = tracer;
		this.client = new OkHttpClient();
	}

	/**
	 * In this case the carrier is HTTP request headers object, which we adapt to
	 * the carrier API by wrapping in RequestBuilderCarrier helper class.
	 * 
	 * Notice that we also add a couple additional tags to the span with some
	 * metadata about the HTTP request, and we mark the span with a span.kind=client
	 * tag, as recommended by the OpenTracing Semantic Conventions. There are other
	 * tags we could add.
	 */
	private String getHttp(int port, String path, String param, String value) {
		try {
			HttpUrl url = new HttpUrl.Builder().scheme("http").host("localhost").port(port).addPathSegment(path)
					.addQueryParameter(param, value).build();
			Request.Builder requestBuilder = new Request.Builder().url(url);

			Tags.SPAN_KIND.set(tracer.activeSpan(), Tags.SPAN_KIND_CLIENT);
			Tags.HTTP_METHOD.set(tracer.activeSpan(), "GET");
			Tags.HTTP_URL.set(tracer.activeSpan(), url.toString());
			tracer.inject(tracer.activeSpan().context(), Format.Builtin.HTTP_HEADERS,
					new RequestBuilderCarrier(requestBuilder));

			Request request = requestBuilder.build();
			Response response = client.newCall(request).execute();
			if (response.code() != 200) {
				throw new RuntimeException("Bad HTTP result: " + response);
			}
			return response.body().string();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void sayHello(String helloTo) {
		try (Scope scope = tracer.buildSpan("say-hello").startActive(true)) {
			scope.span().setTag("hello-to", helloTo);

			String helloStr = formatString(helloTo);
			printHello(helloStr);
		}
	}

	/**
	 * In the Hello#formatString() function we already create a child span. In order
	 * to pass its context over the HTTP request we need to call tracer.inject
	 * before building the HTTP request in Hello#getHttp()
	 */
	private String formatString(String helloTo) {
		try (Scope scope = tracer.buildSpan("formatString").startActive(true)) {
			String helloStr = getHttp(8081, "format", "helloTo", helloTo);
			scope.span().log(ImmutableMap.of("event", "string-format", "value", helloStr));
			return helloStr;
		}
	}

	private void printHello(String helloStr) {
		try (Scope scope = tracer.buildSpan("printHello").startActive(true)) {
			getHttp(8082, "publish", "helloStr", helloStr);
			scope.span().log(ImmutableMap.of("event", "println"));
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("Expecting one argument");
		}

		String helloTo = args[0];
		try (JaegerTracer tracer = Tracing.init("hello-world")) {
			new Hello(tracer).sayHello(helloTo);
		}
	}
}
