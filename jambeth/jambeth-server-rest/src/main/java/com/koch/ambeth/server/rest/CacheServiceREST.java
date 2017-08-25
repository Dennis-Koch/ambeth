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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.StreamingOutput;

import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.cache.service.ICacheService;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.cache.model.IServiceResult;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.IServiceDescription;
import com.koch.ambeth.service.rest.Constants;
import com.koch.ambeth.util.state.IStateRollback;

@Path("/CacheService")
@Consumes({ Constants.AMBETH_MEDIA_TYPE })
@Produces({ Constants.AMBETH_MEDIA_TYPE })
public class CacheServiceREST extends AbstractServiceREST {
	protected ICacheService getCacheService() {
		return getServiceContext().getService(CacheModule.EXTERNAL_CACHE_SERVICE, ICacheService.class);
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("getEntities")
	public StreamingOutput getEntities(InputStream is, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			Object[] args = getArguments(is, request);
			List<ILoadContainer> result = getCacheService().getEntities((List<IObjRef>) args[0]);
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
	@Path("getRelations")
	public StreamingOutput getRelations(InputStream is, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			Object[] args = getArguments(is, request);
			List<IObjRelationResult> result = getCacheService()
					.getRelations((List<IObjRelation>) args[0]);
			return createResult(result, request, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}

	@POST
	@Path("getORIsForServiceRequest")
	public StreamingOutput getORIsForServiceRequest(InputStream is,
			@Context HttpServletRequest request, @Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			Object[] args = getArguments(is, request);
			IServiceResult result = getCacheService()
					.getORIsForServiceRequest((IServiceDescription) args[0]);
			return createResult(result, request, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}
}
