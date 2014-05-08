package de.osthus.ambeth.webservice;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LoggerFactory;

@Provider
public class ExceptionHandler implements ExceptionMapper<RuntimeException>
{
	private static final ILogger log = LoggerFactory.getLogger(ExceptionHandler.class);

	@Override
	public Response toResponse(RuntimeException e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		log.error(e);
		return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage() + "\n" + sw.toString()).type("text/plain").build();
	}
}
