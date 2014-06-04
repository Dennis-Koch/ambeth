package de.osthus.ambeth.webservice;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.sun.jersey.spi.resource.Singleton;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.service.IPrivilegeService;
import de.osthus.ambeth.privilege.transfer.PrivilegeResult;

@Path("/PrivilegeService")
@Consumes({ MediaType.TEXT_PLAIN })
@Produces({ MediaType.TEXT_PLAIN })
@Singleton
public class PrivilegeServiceREST extends AbstractServiceREST
{
	protected IPrivilegeService getPrivilegeService()
	{
		return getService(IPrivilegeService.class);
	}

	@POST
	@Path("GetPrivileges")
	public StreamingOutput getPrivileges(InputStream is)
	{
		try
		{
			preServiceCall();
			Object[] args = getArguments(is);
			List<PrivilegeResult> result = getPrivilegeService().getPrivileges((IObjRef[]) args[0], (ISecurityScope[]) args[1]);
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