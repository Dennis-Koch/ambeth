package com.koch.ambeth.server.rest;

import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.koch.ambeth.event.model.IEventItem;
import com.koch.ambeth.event.service.IEventService;

@Path("/EventService")
@Consumes({ MediaType.TEXT_PLAIN })
@Produces({ MediaType.TEXT_PLAIN })
public class EventServiceREST extends AbstractServiceREST
{
	protected IEventService getEventService()
	{
		return getService(IEventService.class);
	}

	@POST
	@Path("PollEvents")
	public StreamingOutput pollEvents(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		try
		{
			preServiceCall();
			Object[] args = getArguments(is, request);
			List<IEventItem> result = getEventService().pollEvents(((Long) args[0]).longValue(), ((Long) args[1]).longValue(), ((Long) args[2]).longValue());
			return createResult(result, response);
		}
		catch (Throwable e)
		{
			return createExceptionResult(e, response);
		}
		finally
		{
			postServiceCall();
		}
	}

	@GET
	@Path("GetCurrentEventSequence")
	public StreamingOutput getCurrentEventSequence(@Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		try
		{
			preServiceCall();
			long result = getEventService().getCurrentEventSequence();
			return createResult(result, response);
		}
		catch (Throwable e)
		{
			return createExceptionResult(e, response);
		}
		finally
		{
			postServiceCall();
		}
	}

	@GET
	@Path("GetCurrentServerSession")
	public StreamingOutput getCurrentServerSession(@Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		try
		{
			preServiceCall();
			long result = getEventService().getCurrentServerSession();
			return createResult(result, response);
		}
		catch (Throwable e)
		{
			return createExceptionResult(e, response);
		}
		finally
		{
			postServiceCall();
		}
	}
}
