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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.StreamingOutput;

import com.koch.ambeth.event.model.IEventItem;
import com.koch.ambeth.event.service.IEventService;
import com.koch.ambeth.service.rest.Constants;

@Path("/EventService")
@Consumes({Constants.AMBETH_MEDIA_TYPE})
@Produces({Constants.AMBETH_MEDIA_TYPE})
public class EventServiceREST extends AbstractServiceREST {
	protected IEventService getEventService() {
		return getService(IEventService.class);
	}

	@POST
	@Path("pollEvents")
	public StreamingOutput pollEvents(InputStream is, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		try {
			preServiceCall();
			Object[] args = getArguments(is, request);
			List<IEventItem> result = getEventService().pollEvents(((Long) args[0]).longValue(),
					((Long) args[1]).longValue(), ((Long) args[2]).longValue());
			return createResult(result, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, response);
		}
		finally {
			postServiceCall();
		}
	}

	@GET
	@Path("getCurrentEventSequence")
	public StreamingOutput getCurrentEventSequence(@Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		try {
			preServiceCall();
			long result = getEventService().getCurrentEventSequence();
			return createResult(result, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, response);
		}
		finally {
			postServiceCall();
		}
	}

	@GET
	@Path("getCurrentServerSession")
	public StreamingOutput getCurrentServerSession(@Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		try {
			preServiceCall();
			long result = getEventService().getCurrentServerSession();
			return createResult(result, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, response);
		}
		finally {
			postServiceCall();
		}
	}
}
