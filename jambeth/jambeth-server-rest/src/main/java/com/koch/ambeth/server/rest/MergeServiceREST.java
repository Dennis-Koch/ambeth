package com.koch.ambeth.server.rest;

import java.io.IOException;

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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.StreamingOutput;

import com.koch.ambeth.dot.IDotUtil;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.merge.service.IMergeService;
import com.koch.ambeth.merge.transfer.EntityMetaDataTransfer;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.rest.Constants;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.IdentityLinkedMap;
import com.koch.ambeth.util.model.IMethodDescription;
import com.koch.ambeth.util.state.IStateRollback;

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
		var rollback = preServiceCall(request, response);
		try {
			var dot = getMergeService().createMetaDataDOT();
			return createResult(dot, request, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}

	@GET
	@Path("fim")
	@Produces("image/png")
	public StreamingOutput fim(@Context HttpServletRequest request,
			@Context final HttpServletResponse response) {
		var rollback = preServiceCall(request, response);
		try {
			var dotUtil = getService(IDotUtil.class);
			var dot = getMergeService().createMetaDataDOT();
			var pngBytes = dotUtil.writeDotAsPngBytes(dot);
			return new StreamingOutput() {
				@Override
				public void write(OutputStream output) throws IOException, WebApplicationException {
					response.setHeader(HttpHeaders.CONTENT_TYPE, "image/png");
					output.write(pngBytes);
				}
			};
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}

	@POST
	@Path("merge")
	public StreamingOutput merge(InputStream is, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		var rollback = preServiceCall(request, response);
		try {
			var args = getArguments(is, request);
			var result = getMergeService().merge((ICUDResult) args[0], (String[]) args[1],
					(IMethodDescription) args[2]);
			return createResult(result, request, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("getMetaData")
	public StreamingOutput getMetaData(InputStream is, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		var rollback = preServiceCall(request, response);
		try {
			var args = getArguments(is, request);
			var conversionHelper = getService(IConversionHelper.class);
			var result = getService(IEntityMetaDataProvider.class)
					.getMetaData((List<Class<?>>) args[0]);

			var emdTransferMap= IdentityLinkedMap.<IEntityMetaData, EntityMetaDataTransfer>create(result.size());

			var emdTransfer = new ArrayList<EntityMetaDataTransfer>(result.size());
			for (int a = 0, size = result.size(); a < size; a++) {
				var source = result.get(a);
				var target = emdTransferMap.get(source);
				if (target == null) {
					target = conversionHelper.convertValueToType(EntityMetaDataTransfer.class, source);
					emdTransferMap.put(source, target);
				}
				emdTransfer.add(target);
			}
			return createResult(emdTransfer, request, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}
}
