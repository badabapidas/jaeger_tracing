/**
 * Publisher.java is another HTTP server that responds to requests like GET 'http://localhost:8082/publish?helloStr=hi%20there' and prints "hi there" string to stdout.
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

public class Publisher extends Application<Configuration> {

	@Path("/publish")
	@Produces(MediaType.TEXT_PLAIN)
	public class PublisherResource {

		@GET
		public String format(@QueryParam("helloStr") String helloStr) {
			System.out.println(helloStr);
			return "published";
		}
	}

	@Override
	public void run(Configuration configuration, Environment environment) throws Exception {
		environment.jersey().register(new PublisherResource());
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("dw.server.applicationConnectors[0].port", "8082");
		System.setProperty("dw.server.adminConnectors[0].port", "9082");
		new Publisher().run(args);
	}
}
