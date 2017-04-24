package com.koch.ambeth.server.rest;

/*-
 * #%L
 * jambeth-server-rest
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.StreamingOutput;

import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.merge.service.IMergeService;
import com.koch.ambeth.merge.transfer.EntityMetaDataTransfer;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.rest.Constants;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.model.IMethodDescription;

@Path("/MergeService")
@Consumes({Constants.AMBETH_MEDIA_TYPE})
@Produces({Constants.AMBETH_MEDIA_TYPE})
public class MergeServiceREST extends AbstractServiceREST {
	protected IMergeService getMergeService() {
		return getService(IMergeService.class);
	}

	@GET
	@Path("createMetaDataDOT")
	public StreamingOutput createMetaDataDOT(@Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		try {
			preServiceCall();
			String dot = getMergeService().createMetaDataDOT();
			return createResult(dot, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, response);
		}
		finally {
			postServiceCall();
		}
	}

	@POST
	@Path("merge")
	public StreamingOutput merge(InputStream is, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		try {
			preServiceCall();
			Object[] args = getArguments(is, request);
			IOriCollection result =
					getMergeService().merge((ICUDResult) args[0], (IMethodDescription) args[1]);
			return createResult(result, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, response);
		}
		finally {
			postServiceCall();
		}
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("getMetaData")
	public StreamingOutput getMetaData(InputStream is, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		try {
			preServiceCall();
			Object[] args = getArguments(is, request);
			List<IEntityMetaData> result =
					getService(IEntityMetaDataProvider.class).getMetaData((List<Class<?>>) args[0]);

			ArrayList<EntityMetaDataTransfer> emdTransfer = new ArrayList<>(result.size());
			for (int a = 0, size = result.size(); a < size; a++) {
				IEntityMetaData source = result.get(a);
				EntityMetaDataTransfer target = getService(IConversionHelper.class)
						.convertValueToType(EntityMetaDataTransfer.class, source);
				emdTransfer.add(target);
			}
			return createResult(emdTransfer, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, response);
		}
		finally {
			postServiceCall();
		}
	}
}
