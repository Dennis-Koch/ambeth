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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.StreamingOutput;

import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.security.ICurrentUserProvider;
import com.koch.ambeth.security.ISecurityContext;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.security.server.IPasswordUtil;
import com.koch.ambeth.security.service.ISecurityService;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.service.model.IServiceDescription;
import com.koch.ambeth.service.rest.Constants;
import com.koch.ambeth.util.state.IStateRollback;

@Path("/SecurityService")
@Consumes(Constants.AMBETH_MEDIA_TYPE)
@Produces(Constants.AMBETH_MEDIA_TYPE)
public class SecurityServiceREST extends AbstractServiceREST {
	protected ISecurityService getSecurityService() {
		return getService(ISecurityService.class);
	}

	@POST
	@Path("callServiceInSecurityScope")
	public StreamingOutput callServiceInSecurityScope(InputStream is,
			@Context HttpServletRequest request, @Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			Object[] args = getArguments(is, request);
			Object result = getSecurityService().callServiceInSecurityScope((ISecurityScope[]) args[0],
					(IServiceDescription) args[1]);
			return createResult(result, request, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}

	@GET
	@Path("isSecured")
	public StreamingOutput isSecured(@Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			boolean result = getService(ISecurityActivation.class).isSecured();
			return createResult(result, request, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}

	@GET
	@Path("isAuthenticated")
	public StreamingOutput isAuthenticated(@Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			boolean authenticated = getService(ICurrentUserProvider.class).getCurrentUser() != null;
			return createResult(authenticated, request, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}

	@GET
	@Path("getHeartbeatInterval")
	public StreamingOutput getHeartbeatInterval(@Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			long heartbeatInterval = 60000; // TODO: provide bean or configuration of this
			return createResult(heartbeatInterval, request, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}

	@GET
	@Path("getCurrentUser")
	public StreamingOutput getCurrentUser(@Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			IUser result = getService(ICurrentUserProvider.class).getCurrentUser();
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
	@Path("currentUserHasActionPermission")
	public StreamingOutput hasActionPermission(InputStream is, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			Object[] args = getArguments(is, request);
			boolean result =
					getService(ICurrentUserProvider.class).currentUserHasActionPermission((String) args[0]);
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
	@Path("validatePassword")
	public StreamingOutput validatePassword(InputStream is, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			Object[] args = getArguments(is, request);
			char[] newCleartextPassword = (char[]) args[0];

			IPasswordUtil passwordUtil = getService(IPasswordUtil.class);
			rollback = passwordUtil.pushSuppressPasswordChangeRequired(rollback);
			IUser currentUser = getService(ICurrentUserProvider.class).getCurrentUser();
			passwordUtil.validatePassword(newCleartextPassword, currentUser);
			return createResult(true, request, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}

	@POST
	@Path("changePassword")
	public StreamingOutput changePassword(InputStream is, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			Object[] args = getArguments(is, request);
			char[] newCleartextPassword = (char[]) args[0];

			ISecurityContext securityContext = getService(ISecurityContextHolder.class).getContext();
			if (securityContext == null || securityContext.getAuthentication() == null) {
				throw new SecurityException("No authenticaton provided");
			}
			IPasswordUtil passwordUtil = getService(IPasswordUtil.class);
			rollback = passwordUtil.pushSuppressPasswordChangeRequired(rollback);
			IUser currentUser = getService(ICurrentUserProvider.class).getCurrentUser();
			passwordUtil.assignNewPassword(newCleartextPassword, currentUser,
					securityContext.getAuthentication().getPassword());

			getService(IMergeProcess.class).process(currentUser);
			return createResult(true, request, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}
}
