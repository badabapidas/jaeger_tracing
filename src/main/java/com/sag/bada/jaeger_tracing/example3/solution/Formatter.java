package com.sag.bada.jaeger_tracing.example3.solution;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.ImmutableMap;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import lib.Tracing;

public class Formatter extends Application<Configuration> {

	/**
	 * Instrumenting the Servers. Our servers are currently not instrumented for
	 * tracing. We need to do the following:
	 * 
	 * Create an instance of a Tracer, similar to how we did it in Hello.java
	 */
	private final Tracer tracer;

	private Formatter(Tracer tracer) {
		this.tracer = tracer;
	}

	/**
	 * Now change the FormatterResource handler method to use startServerSpan
	 * 
	 */
	@Path("/format")
	@Produces(MediaType.TEXT_PLAIN)
	public class FormatterResource {

		@GET
		public String format(@QueryParam("helloTo") String helloTo, @Context HttpHeaders httpHeaders) {
			try (Scope scope = Tracing.startServerSpan(tracer, httpHeaders, "format")) {
				String helloStr = String.format("Hello, %s!", helloTo);
				scope.span().log(ImmutableMap.of("event", "string-format", "value", helloStr));
				return helloStr;
			}
		}

	}

	@Override
	public void run(Configuration configuration, Environment environment) throws Exception {
		environment.jersey().register(new FormatterResource());
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("dw.server.applicationConnectors[0].port", "8081");
		System.setProperty("dw.server.adminConnectors[0].port", "9081");

		// Replace the call to Formatter.run() with this
		try (JaegerTracer tracer = Tracing.init("formatter")) {
			new Formatter(tracer).run(args);
		}
	}
}
