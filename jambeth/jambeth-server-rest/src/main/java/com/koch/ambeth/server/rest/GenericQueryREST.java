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

import com.koch.ambeth.filter.IFilterDescriptor;
import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.filter.query.service.IGenericQueryService;
import com.koch.ambeth.mapping.IMapperServiceFactory;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.squery.QueryUtils;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.IClassCache;
import com.koch.ambeth.util.IConversionHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@Path("/GenericQueryService")
// @Consumes({ MediaType.TEXT_PLAIN })
// @Produces({ MediaType.TEXT_PLAIN })
public class GenericQueryREST extends AbstractServiceREST {
    @Override
    protected void writeContent(String contentType, OutputStream os, Object result) {
        // TODO: write JSON
        super.writeContent(contentType, os, result);
    }

    @Override
    protected Object[] getArguments(InputStream is, HttpServletRequest request) {
        // TODO: read JSON
        return super.getArguments(is, request);
    }

    @POST
    @Path("filter")
    public StreamingOutput filter(InputStream is, @Context HttpServletRequest request, final @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, is,
                args -> getService(IGenericQueryService.class).filter((IFilterDescriptor<?>) args[0], (ISortDescriptor[]) args[1], (IPagingRequest) args[2]));
    }

    @GET
    @Path("{subResources:.*}")
    public StreamingOutput get(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, () -> {
            var contextPath = request.getPathInfo();
            var path = contextPath.split("/");

            var entityMetaDataProvider = getService(IEntityMetaDataProvider.class);

            var valueObjectTypeName = path[2];

            var query = path[3];

            var config = entityMetaDataProvider.getValueObjectConfig(valueObjectTypeName);
            Class<?> entityType;
            if (config != null) {
                entityType = config.getEntityType();
            } else {
                entityType = getService(IClassCache.class).loadClass(valueObjectTypeName);
            }
            var conversionHelper = getService(IConversionHelper.class);
            var queryBuilderFactory = getService(IQueryBuilderFactory.class);

            var queryBuilderBean = QueryUtils.buildQuery(query, entityType);

            var result = queryBuilderBean.createQueryBuilder(queryBuilderFactory, conversionHelper, new Object[0], Object.class);

            if (config == null) {
                return result;
            }
            var mapperServiceFactory = getService(IMapperServiceFactory.class);

            var mapperService = mapperServiceFactory.create();
            try {
                var valueObject = result instanceof List ? mapperService.mapToValueObjectList((List<?>) result, config.getValueType()) : mapperService.mapToValueObject(result, config.getValueType());
                return valueObject;
            } finally {
                mapperService.dispose();
            }
        });
    }
}
