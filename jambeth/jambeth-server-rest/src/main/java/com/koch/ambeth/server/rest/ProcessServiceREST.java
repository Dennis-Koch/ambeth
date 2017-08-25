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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.StreamingOutput;

import com.koch.ambeth.service.IProcessService;
import com.koch.ambeth.service.model.IServiceDescription;
import com.koch.ambeth.service.rest.Constants;
import com.koch.ambeth.util.state.IStateRollback;

@Path("/ProcessService")
@Consumes(Constants.AMBETH_MEDIA_TYPE)
@Produces(Constants.AMBETH_MEDIA_TYPE)
public class ProcessServiceREST extends AbstractServiceREST {
	protected IProcessService getProcessService() {
		return getServiceContext().getService(IProcessService.class);
	}

	@POST
	@Path("invokeService")
	public StreamingOutput invokeService(InputStream is, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			Object[] args = getArguments(is, request);
			Object result = getProcessService().invokeService((IServiceDescription) args[0]);
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
