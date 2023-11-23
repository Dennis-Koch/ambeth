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

import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.cache.service.ICacheService;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.IServiceDescription;
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
import java.util.List;

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
    public StreamingOutput getEntities(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, is, args -> getCacheService().getEntities((List<IObjRef>) args[0]));
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("getRelations")
    public StreamingOutput getRelations(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, is, args -> getCacheService().getRelations((List<IObjRelation>) args[0]));
    }

    @POST
    @Path("getORIsForServiceRequest")
    public StreamingOutput getORIsForServiceRequest(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, is, args -> getCacheService().getORIsForServiceRequest((IServiceDescription) args[0]));
    }
}
