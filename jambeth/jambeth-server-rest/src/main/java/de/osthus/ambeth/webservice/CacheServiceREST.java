package de.osthus.ambeth.webservice;

import java.io.InputStream;
import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
@Singleton
public class CacheServiceREST extends AbstractServiceREST
{
	protected ICacheService getCacheService()
	{
		return getServiceContext().getService(CacheModule.EXTERNAL_CACHE_SERVICE, ICacheService.class);
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("GetEntities")
	public StreamingOutput getEntities(InputStream is)
	{
		try
		{
			preServiceCall();
			Object[] args = getArguments(is);
			List<ILoadContainer> result = getCacheService().getEntities((List<IObjRef>) args[0]);
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

	@SuppressWarnings("unchecked")
	@POST
	@Path("GetRelations")
	public StreamingOutput getRelations(InputStream is)
	{
		try
		{
			preServiceCall();
			Object[] args = getArguments(is);
			List<IObjRelationResult> result = getCacheService().getRelations((List<IObjRelation>) args[0]);
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

	@POST
	@Path("GetORIsForServiceRequest")
	public StreamingOutput getORIsForServiceRequest(InputStream is)
	{
		try
		{
			preServiceCall();
			Object[] args = getArguments(is);
			IServiceResult result = getCacheService().getORIsForServiceRequest((IServiceDescription) args[0]);
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
