package de.osthus.ambeth.webservice;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import de.osthus.ambeth.model.IServiceDescription;
import de.osthus.ambeth.service.IProcessService;

@Path("/ProcessService")
@Consumes({ MediaType.TEXT_PLAIN })
@Produces({ MediaType.TEXT_PLAIN })
public class ProcessServiceREST extends AbstractServiceREST
{
	protected IProcessService getProcessService()
	{
		return getServiceContext().getService(IProcessService.class);
	}

	@POST
	@Path("InvokeService")
	public StreamingOutput invokeService(InputStream is)
	{
		try
		{
			preServiceCall();
			Object[] args = getArguments(is);
			Object result = getProcessService().invokeService((IServiceDescription) args[0]);
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
