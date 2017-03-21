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
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.StreamingOutput;

import com.koch.ambeth.mapping.IMapperService;
import com.koch.ambeth.mapping.IMapperServiceFactory;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.squery.QueryBuilderBean;
import com.koch.ambeth.query.squery.QueryUtils;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.IConversionHelper;

@Path("/GenericQueryService")
// @Consumes({ MediaType.TEXT_PLAIN })
// @Produces({ MediaType.TEXT_PLAIN })
public class GenericQueryREST extends AbstractServiceREST {
	@Override
	protected void writeContent(OutputStream os, Object result) {
		// TODO: write JSON
		super.writeContent(os, result);
	}

	@Override
	protected Object[] getArguments(InputStream is, HttpServletRequest request) {
		// TODO: read JSON
		return super.getArguments(is, request);
	}

	@GET
	@Path("{subResources:.*}")
	public StreamingOutput get(@Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		try {
			preServiceCall();

			String contextPath = request.getPathInfo();
			String[] path = contextPath.split("/");

			IEntityMetaDataProvider entityMetaDataProvider = getService(IEntityMetaDataProvider.class);

			String valueObjectTypeName = path[2];

			String query = path[3];

			IValueObjectConfig config = entityMetaDataProvider.getValueObjectConfig(valueObjectTypeName);
			if (config == null) {
				throw new BadRequestException("Entity type '" + valueObjectTypeName + "' not known");
			}
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(config.getEntityType());

			IConversionHelper conversionHelper = getService(IConversionHelper.class);
			IQueryBuilderFactory queryBuilderFactory = getService(IQueryBuilderFactory.class);

			Class<?> entityType = metaData.getEntityType();

			QueryBuilderBean<?> queryBuilderBean = QueryUtils.buildQuery(query, entityType);

			Object result = queryBuilderBean.createQueryBuilder(queryBuilderFactory, conversionHelper,
					new Object[0], Object.class);

			IMapperServiceFactory mapperServiceFactory = getService(IMapperServiceFactory.class);

			IMapperService mapperService = mapperServiceFactory.create();
			try {

				Object valueObject = result instanceof List
						? mapperService.mapToValueObjectList((List<?>) result, config.getValueType())
						: mapperService.mapToValueObject(result, config.getValueType());
				return createResult(valueObject, response);
			}
			finally {
				mapperService.dispose();
			}
		}
		catch (WebApplicationException e) {
			throw e;
		}
		catch (Throwable e) {
			return createExceptionResult(e, response);
		}
		finally {
			postServiceCall();
		}
	}
}
