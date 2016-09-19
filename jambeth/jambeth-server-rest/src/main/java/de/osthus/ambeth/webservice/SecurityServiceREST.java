package de.osthus.ambeth.webservice;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.model.IServiceDescription;
import de.osthus.ambeth.service.ISecurityService;

@Path("/SecurityService")
@Consumes({ MediaType.TEXT_PLAIN })
@Produces({ MediaType.TEXT_PLAIN })
public class SecurityServiceREST extends AbstractServiceREST
{
	protected ISecurityService getSecurityService()
	{
		return getService(ISecurityService.class);
	}

	@POST
	@Path("CallServiceInSecurityScope")
	public StreamingOutput callServiceInSecurityScope(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		try
		{
			preServiceCall();
			Object[] args = getArguments(is, request);
			Object result = getSecurityService().callServiceInSecurityScope((ISecurityScope[]) args[0], (IServiceDescription) args[1]);
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
