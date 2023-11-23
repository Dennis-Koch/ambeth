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

import com.koch.ambeth.security.service.IPrivilegeService;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.service.rest.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.InputStream;

@Path("/PrivilegeService")
@Consumes({ Constants.AMBETH_MEDIA_TYPE })
@Produces({ Constants.AMBETH_MEDIA_TYPE })
public class PrivilegeServiceREST extends AbstractServiceREST {
    protected IPrivilegeService getPrivilegeService() {
        return getService(IPrivilegeService.class);
    }

    @POST
    @Path("getPrivileges")
    public StreamingOutput getPrivileges(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, is, args -> getPrivilegeService().getPrivileges((IObjRef[]) args[0], (ISecurityScope[]) args[1]));
    }

    @POST
    @Path("getPrivilegesOfTypes")
    public StreamingOutput getPrivilegesOfTypes(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, is, args -> getPrivilegeService().getPrivilegesOfTypes((Class<?>[]) args[0], (ISecurityScope[]) args[1]));
    }
}
