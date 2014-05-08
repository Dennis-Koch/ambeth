package de.osthus.ambeth.webservice;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.sun.jersey.spi.resource.Singleton;

import de.osthus.ambeth.event.model.IEventItem;
import de.osthus.ambeth.service.IEventService;

@Path("/EventService")
@Consumes({ MediaType.TEXT_PLAIN })
@Produces({ MediaType.TEXT_PLAIN })
@Singleton
public class EventServiceREST extends AbstractServiceREST
{
	protected IEventService getEventService()
	{
		return getService(IEventService.class);
	}

	@POST
	@Path("PollEvents")
	public StreamingOutput pollEvents(InputStream is)
	{
		try
		{
			preServiceCall();
			Object[] args = getArguments(is);
			List<IEventItem> result = getEventService().pollEvents(((Long) args[0]).longValue(), ((Long) args[1]).longValue(), ((Long) args[2]).longValue());
			return createResult(result);
		}
		catch (Throwable e)
		{
			return createExceptionResult(e);
		}
		finally
		{
			postServiceCall();
		}
	}

	@GET
	@Path("GetCurrentEventSequence")
	public StreamingOutput getCurrentEventSequence()
	{
		try
		{
			preServiceCall();
			long result = getEventService().getCurrentEventSequence();
			return createResult(result);
		}
		catch (Throwable e)
		{
			return createExceptionResult(e);
		}
		finally
		{
			postServiceCall();
		}
	}

	@GET
	@Path("GetCurrentServerSession")
	public StreamingOutput getCurrentServerSession()
	{
		try
		{
			preServiceCall();
			long result = getEventService().getCurrentServerSession();
			return createResult(result);
		}
		catch (Throwable e)
		{
			return createExceptionResult(e);
		}
		finally
		{
			postServiceCall();
		}
	}
}
