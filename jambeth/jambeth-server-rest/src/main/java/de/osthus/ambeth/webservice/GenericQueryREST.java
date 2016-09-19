package de.osthus.ambeth.webservice;

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

import de.osthus.ambeth.mapping.IMapperService;
import de.osthus.ambeth.mapping.IMapperServiceFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.query.squery.QueryBuilderBean;
import de.osthus.ambeth.query.squery.QueryUtils;
import de.osthus.ambeth.util.IConversionHelper;

@Path("/GenericQueryService")
// @Consumes({ MediaType.TEXT_PLAIN })
// @Produces({ MediaType.TEXT_PLAIN })
public class GenericQueryREST extends AbstractServiceREST
{
	@Override
	protected void writeContent(OutputStream os, Object result)
	{
		// TODO: write JSON
		super.writeContent(os, result);
	}

	@Override
	protected Object[] getArguments(InputStream is, HttpServletRequest request)
	{
		// TODO: read JSON
		return super.getArguments(is, request);
	}

	@GET
	@Path("{subResources:.*}")
	public StreamingOutput get(@Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		try
		{
			preServiceCall();

			String contextPath = request.getPathInfo();
			String[] path = contextPath.split("/");

			IEntityMetaDataProvider entityMetaDataProvider = getService(IEntityMetaDataProvider.class);

			String valueObjectTypeName = path[2];

			String query = path[3];

			IValueObjectConfig config = entityMetaDataProvider.getValueObjectConfig(valueObjectTypeName);
			if (config == null)
			{
				throw new BadRequestException("Entity type '" + valueObjectTypeName + "' not known");
			}
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(config.getEntityType());

			IConversionHelper conversionHelper = getService(IConversionHelper.class);
			IQueryBuilderFactory queryBuilderFactory = getService(IQueryBuilderFactory.class);

			Class<?> entityType = metaData.getEntityType();

			QueryBuilderBean<?> queryBuilderBean = QueryUtils.buildQuery(query, entityType);

			Object result = queryBuilderBean.createQueryBuilder(queryBuilderFactory, conversionHelper, new Object[0], Object.class);

			IMapperServiceFactory mapperServiceFactory = getService(IMapperServiceFactory.class);

			IMapperService mapperService = mapperServiceFactory.create();
			try
			{

				Object valueObject = result instanceof List ? mapperService.mapToValueObjectList((List<?>) result, config.getValueType()) : mapperService
						.mapToValueObject(result, config.getValueType());
				return createResult(valueObject, response);
			}
			finally
			{
				mapperService.dispose();
			}
		}
		catch (WebApplicationException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			return createExceptionResult(e, response);
		}
		finally
		{
			postServiceCall();
		}
	}
}
