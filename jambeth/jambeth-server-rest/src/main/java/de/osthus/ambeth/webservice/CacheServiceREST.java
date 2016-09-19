package de.osthus.ambeth.webservice;

import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.cache.model.IServiceResult;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.IServiceDescription;
import de.osthus.ambeth.service.ICacheService;

@Path("/CacheService")
@Consumes({ MediaType.TEXT_PLAIN })
@Produces({ MediaType.TEXT_PLAIN })
public class CacheServiceREST extends AbstractServiceREST
{
	protected ICacheService getCacheService()
	{
		return getServiceContext().getService(CacheModule.EXTERNAL_CACHE_SERVICE, ICacheService.class);
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("GetEntities")
	public StreamingOutput getEntities(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		try
		{
			preServiceCall();
			Object[] args = getArguments(is, request);
			List<ILoadContainer> result = getCacheService().getEntities((List<IObjRef>) args[0]);
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

	@SuppressWarnings("unchecked")
	@POST
	@Path("GetRelations")
	public StreamingOutput getRelations(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		try
		{
			preServiceCall();
			Object[] args = getArguments(is, request);
			List<IObjRelationResult> result = getCacheService().getRelations((List<IObjRelation>) args[0]);
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

	@POST
	@Path("GetORIsForServiceRequest")
	public StreamingOutput getORIsForServiceRequest(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		try
		{
			preServiceCall();
			Object[] args = getArguments(is, request);
			IServiceResult result = getCacheService().getORIsForServiceRequest((IServiceDescription) args[0]);
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
