package de.osthus.ambeth.webservice;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.sun.jersey.spi.resource.Singleton;

import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.model.IServiceDescription;
import de.osthus.ambeth.service.ISecurityService;

@Path("/SecurityService")
@Consumes({ MediaType.TEXT_PLAIN })
@Produces({ MediaType.TEXT_PLAIN })
@Singleton
public class SecurityServiceREST extends AbstractServiceREST
{
	protected ISecurityService getSecurityService()
	{
		return getService(ISecurityService.class);
	}

	@POST
	@Path("CallServiceInSecurityScope")
	public StreamingOutput callServiceInSecurityScope(InputStream is)
	{
		try
		{
			preServiceCall();
			Object[] args = getArguments(is);
			Object result = getSecurityService().callServiceInSecurityScope((ISecurityScope[]) args[0], (IServiceDescription) args[1]);
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
