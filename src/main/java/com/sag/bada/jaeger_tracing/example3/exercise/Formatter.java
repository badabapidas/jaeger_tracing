
/**
 * Formatter.java is a Dropwizard-based HTTP server that responds to a request like GET 'http://localhost:8081/format?helloTo=Bapi' and returns "Hello, Bapi!" string
 */
package com.sag.bada.jaeger_tracing.example3.exercise;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

public class Formatter extends Application<Configuration> {

	@Path("/format")
	@Produces(MediaType.TEXT_PLAIN)
	public class FormatterResource {

		@GET
		public String format(@QueryParam("helloTo") String helloTo) {
			String helloStr = String.format("Hello, %s!", helloTo);
			return helloStr;
		}
	}

	@Override
	public void run(Configuration configuration, Environment environment) throws Exception {
		environment.jersey().register(new FormatterResource());
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("dw.server.applicationConnectors[0].port", "8081");
		System.setProperty("dw.server.adminConnectors[0].port", "9081");
		new Formatter().run(args);
	}
}
