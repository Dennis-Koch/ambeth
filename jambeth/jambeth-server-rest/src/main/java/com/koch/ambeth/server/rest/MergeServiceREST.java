package com.koch.ambeth.server.rest;

import java.io.InputStream;
import java.util.ArrayList;
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

import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.merge.service.IMergeService;
import com.koch.ambeth.merge.transfer.EntityMetaDataTransfer;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.model.IMethodDescription;

@Path("/MergeService")
@Consumes({ MediaType.TEXT_PLAIN })
@Produces({ MediaType.TEXT_PLAIN })
public class MergeServiceREST extends AbstractServiceREST
{
	protected IMergeService getMergeService()
	{
		return getService(IMergeService.class);
	}

	@POST
	@Path("Merge")
	public StreamingOutput merge(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		try
		{
			preServiceCall();
			Object[] args = getArguments(is, request);
			IOriCollection result = getMergeService().merge((ICUDResult) args[0], (IMethodDescription) args[1]);
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
	@Path("GetMetaData")
	public StreamingOutput getMetaData(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		try
		{
			preServiceCall();
			Object[] args = getArguments(is, request);
			List<IEntityMetaData> result = getService(IEntityMetaDataProvider.class).getMetaData((List<Class<?>>) args[0]);

			ArrayList<EntityMetaDataTransfer> emdTransfer = new ArrayList<EntityMetaDataTransfer>(result.size());
			for (int a = 0, size = result.size(); a < size; a++)
			{
				IEntityMetaData source = result.get(a);
				EntityMetaDataTransfer target = getService(IConversionHelper.class).convertValueToType(EntityMetaDataTransfer.class, source);
				emdTransfer.add(target);
			}
			return createResult(emdTransfer, response);
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
