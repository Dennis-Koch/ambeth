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

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.koch.ambeth.log.ILogger;

@Provider
public class ExceptionHandler extends AbstractServiceREST
		implements ExceptionMapper<RuntimeException> {
	@Override
	public Response toResponse(RuntimeException e) {
		ILogger log = getLog();
		if (e instanceof WebApplicationException) {
			ErrorItem errorItem = new ErrorItem(((WebApplicationException) e).getResponse().getStatus(),
					e.getClass().getName(), e.getMessage());
			return Response.fromResponse(((WebApplicationException) e).getResponse()).entity(errorItem)
					.build();
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		log.error(e);
		return Response.status(Status.INTERNAL_SERVER_ERROR)
				.entity(e.getMessage() + "\n" + sw.toString()).type("text/plain").build();
	}
}
