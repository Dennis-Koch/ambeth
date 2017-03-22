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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.StreamingOutput;

import com.koch.ambeth.filter.IFilterDescriptor;
import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.mapping.IMapperService;
import com.koch.ambeth.mapping.IMapperServiceFactory;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICacheContext;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.IDisposableCache;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.filter.IFilterToQueryBuilder;
import com.koch.ambeth.query.filter.IPagingQuery;
import com.koch.ambeth.query.squery.QueryBuilderBean;
import com.koch.ambeth.query.squery.QueryUtils;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

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

	@POST
	@Path("filter")
	public StreamingOutput filter(InputStream is, @Context HttpServletRequest request,
			final @Context HttpServletResponse response) {
		try {
			preServiceCall();
			final Object[] args = getArguments(is, request);
			// we need to maintain our own explicit 1st level cache: during serialization of
			// the StreamingOutput we need a (still) valid cache handle
			// the transparently maintained 1st level cache of the framework bound to the current thread
			// would be disposed already so we have to
			// "stretch" the living phase with our own custom lifecycle handling
			// it is important that in each and every code-path (specifically
			// on all ever possible errors) the custom cache gets really disposed at the end 100%
			// consistently
			ICacheContext cacheContext = getService(ICacheContext.class);
			final IDisposableCache cache =
					getService(ICacheFactory.class).create(CacheFactoryDirective.NoDCE, "genericFilter");
			boolean success = false;
			try {
				StreamingOutput result = cacheContext.executeWithCache(cache,
						new IResultingBackgroundWorkerDelegate<StreamingOutput>() {
							@Override
							public StreamingOutput invoke() throws Throwable {
								IFilterToQueryBuilder filterToQueryBuilder =
										getService(IFilterToQueryBuilder.class);
								IPagingQuery<?> pagingQuery = filterToQueryBuilder
										.buildQuery((IFilterDescriptor<?>) args[0], (ISortDescriptor[]) args[1]);
								IPagingResponse<?> result = pagingQuery.retrieve((IPagingRequest) args[2]);
								return createResult(result, response,
										new IBackgroundWorkerParamDelegate<Throwable>() {
											@Override
											public void invoke(Throwable e) throws Throwable {
												cache.dispose(); // on success this gets called also with "e = null"
											}
										});
							}
						});
				success = true;
				return result;
			}
			finally {
				if (!success) {
					cache.dispose();
				}
			}
		}
		catch (Throwable e) {
			return createExceptionResult(e, response);
		}
		finally {
			postServiceCall();
		}
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
			Class<?> entityType;
			if (config != null) {
				entityType = config.getEntityType();
			}
			else {
				entityType =
						getService(IClassLoaderProvider.class).getClassLoader().loadClass(valueObjectTypeName);
			}
			IConversionHelper conversionHelper = getService(IConversionHelper.class);
			IQueryBuilderFactory queryBuilderFactory = getService(IQueryBuilderFactory.class);

			QueryBuilderBean<?> queryBuilderBean = QueryUtils.buildQuery(query, entityType);

			Object result = queryBuilderBean.createQueryBuilder(queryBuilderFactory, conversionHelper,
					new Object[0], Object.class);

			if (config == null) {
				return createResult(result, response);
			}
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
