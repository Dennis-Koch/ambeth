package de.osthus.ambeth.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.mapping.IMapperService;
import de.osthus.ambeth.mapping.IMapperServiceFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.service.ISecurityService;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ListUtil;

@Path("/GenericEntityService")
@Consumes({ MediaType.TEXT_PLAIN })
@Produces({ MediaType.TEXT_PLAIN })
public class GenericEntityREST extends AbstractServiceREST
{
	protected ISecurityService getSecurityService()
	{
		return getService(ISecurityService.class);
	}

	@Override
	protected void writeContent(OutputStream os, Object result)
	{
		// TODO: write JSON
		super.writeContent(os, result);
	}

	@Override
	protected Object[] getArguments(InputStream is)
	{
		// TODO: read JSON
		return super.getArguments(is);
	}

	private final class NavigationResult
	{
		public final Object value;

		public final IValueObjectConfig config;

		public final IValueObjectConfig parentConfig;

		public final IEntityMetaData metaData;

		public final IEntityMetaData parentMetaData;

		public NavigationResult(Object value, IEntityMetaData parentMetaData, IEntityMetaData metaData, IValueObjectConfig parentConfig,
				IValueObjectConfig config)
		{
			super();
			this.value = value;
			this.parentMetaData = parentMetaData;
			this.metaData = metaData;
			this.parentConfig = parentConfig;
			this.config = config;
		}
	}

	private final class NavigationStep
	{
		public final Object value;

		public final IValueObjectConfig config;

		public final IEntityMetaData metaData;

		public final Member boMember;

		public NavigationStep(Object value, IEntityMetaData metaData, IValueObjectConfig config, Member boMember)
		{
			super();
			this.value = value;
			this.metaData = metaData;
			this.config = config;
			this.boMember = boMember;
		}
	}

	private enum NavigationMode
	{
		DEFAULT, TRY_ONLY, SKIP_LAST;
	}

	protected NavigationStep[] navigateTo(String[] path, NavigationMode navigationMode)
	{
		ICache cache = getService(ICache.class);
		IConversionHelper conversionHelper = getService(IConversionHelper.class);
		IEntityMetaDataProvider entityMetaDataProvider = getService(IEntityMetaDataProvider.class);

		String valueObjectTypeName = path[2];

		IValueObjectConfig config = entityMetaDataProvider.getValueObjectConfig(valueObjectTypeName);
		if (config == null)
		{
			throw new BadRequestException("Entity type '" + valueObjectTypeName + "' not known");
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(config.getEntityType());

		Object value = null;

		if (path.length > 3)
		{
			String entityId = path[3];
			Object convertedEntityId = conversionHelper.convertValueToType(metaData.getIdMember().getRealType(), entityId);
			value = cache.getObject(metaData.getEntityType(), convertedEntityId);
		}
		StringBuilder pathSB = new StringBuilder(valueObjectTypeName);

		if (value == null)
		{
			if (navigationMode == NavigationMode.TRY_ONLY)
			{
				return new NavigationStep[] { new NavigationStep(null, metaData, config, null) };
			}
			throw new NotFoundException("Entity '" + pathSB + "'  not found");
		}

		ArrayList<NavigationStep> navigationSteps = new ArrayList<NavigationStep>();

		for (int index = 4, size = path.length; index < size; index++)
		{
			String voMemberName = path[index];
			pathSB.append('.').append(voMemberName);

			String boMemberName = config.getBusinessObjectMemberName(voMemberName);
			Member member = metaData.getMemberByName(boMemberName);
			if (member == null)
			{
				throw new BadRequestException("Entity member '" + pathSB + "' not known");
			}
			navigationSteps.add(new NavigationStep(value, metaData, config, member));
			if (value == null)
			{
				if (navigationMode == NavigationMode.TRY_ONLY)
				{
					return navigationSteps.toArray(NavigationStep.class);
				}
				throw new NotFoundException("Entity '" + pathSB + "' not found");
			}
			if (metaData.isRelationMember(boMemberName))
			{
				metaData = entityMetaDataProvider.getMetaData(member.getElementType());
				List<Class<?>> availableConfigs = entityMetaDataProvider.getValueObjectTypesByEntityType(metaData.getEntityType());
				if (availableConfigs.size() == 0)
				{
					throw new BadRequestException("Entity member '" + pathSB + "' not serializable");
				}
				config = entityMetaDataProvider.getValueObjectConfig(availableConfigs.get(0));
			}
			else
			{
				metaData = null;
				config = null;
			}

			if (navigationMode == NavigationMode.SKIP_LAST && index == size - 1)
			{
				break;
			}
			value = member.getValue(value);

			if (!member.isToMany() || index + 1 < size)
			{
				continue;
			}

			// next item must be an index specification
			index++;
			int indexSpec = conversionHelper.convertValueToType(int.class, path[index]);
			pathSB.append('[').append(indexSpec).append(']');

			if (value instanceof List)
			{
				value = ((List<?>) value).get(indexSpec);
			}
			else
			{
				Iterator<?> iter = ((Collection<?>) value).iterator();
				while (true)
				{
					if (!iter.hasNext())
					{
						throw new NotFoundException("Entity '" + pathSB + "' not found");
					}
					if (indexSpec == 0)
					{
						break;
					}
					indexSpec--;
					iter.next();
				}
				value = iter.next();
			}
		}
		navigationSteps.add(new NavigationStep(value, metaData, config, null));

		return navigationSteps.toArray(NavigationStep.class);
	}

	@GET
	@Path("{subResources:.*}")
	public StreamingOutput get(@Context HttpServletRequest request)
	{
		try
		{
			preServiceCall();

			String contextPath = request.getPathInfo();
			String[] path = contextPath.split("/");

			NavigationStep[] navigationSteps = navigateTo(path, NavigationMode.DEFAULT);
			NavigationStep lastStep = navigationSteps[navigationSteps.length - 1];

			if (lastStep.value instanceof IEntityMetaDataHolder)
			{
				IMapperServiceFactory mapperServiceFactory = getService(IMapperServiceFactory.class);

				IMapperService mapperService = mapperServiceFactory.create();
				try
				{
					Object valueObject = mapperService.mapToValueObject(lastStep.value, lastStep.config.getValueType());
					return createResult(valueObject);
				}
				finally
				{
					mapperService.dispose();
				}
			}
			// simple value and not an entity
			return createResult(lastStep.value);
		}
		catch (WebApplicationException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			return createExceptionResult(e);
		}
		finally
		{
			postServiceCall();
		}
	}

	@PUT
	@Path("{subResources:.*}")
	public StreamingOutput put(@Context HttpServletRequest request, InputStream is)
	{
		try
		{
			preServiceCall();

			String contextPath = request.getPathInfo();
			String[] path = contextPath.split("/");

			NavigationStep[] navigationSteps = navigateTo(path, NavigationMode.DEFAULT);
			NavigationStep parentStep = navigationSteps.length >= 2 ? navigationSteps[navigationSteps.length - 2] : null;
			NavigationStep lastStep = navigationSteps[navigationSteps.length - 1];
			int a = 5;
			Object[] args = getArguments(is);
			if (args.length != 1 || args[0] == null)
			{
				throw new BadRequestException("No values provided to modify entity '" + Arrays.toString(path) + "'");
			}
			Object valueObject = args[0];

			IEntityMetaData metaData = lastStep.metaData != null ? lastStep.metaData : parentStep.metaData;
			IValueObjectConfig config = lastStep.metaData != null ? lastStep.config : parentStep.config;
			Object entity = lastStep.metaData != null ? lastStep.value : parentStep.value;

			IConversionHelper conversionHelper = getService(IConversionHelper.class);
			IMapperServiceFactory mapperServiceFactory = getService(IMapperServiceFactory.class);
			IMergeProcess mergeProcess = getService(IMergeProcess.class);
			ITypeInfoProvider typeInfoProvider = getService(ITypeInfoProvider.class);

			if (lastStep.metaData != null)
			{
				Object entityId = metaData.getIdMember().getValue(entity);
				String voIdMemberName = config.getValueObjectMemberName(metaData.getIdMember().getName());
				ITypeInfoItem voIdMember = typeInfoProvider.getHierarchicMember(config.getValueType(), voIdMemberName);
				voIdMember.setValue(valueObject, conversionHelper.convertValueToType(voIdMember.getRealType(), entityId));

				IMapperService mapperService = mapperServiceFactory.create();
				try
				{
					Object businessObject = mapperService.mapToBusinessObject(valueObject);
					mergeProcess.process(businessObject, null, null, null);

					valueObject = mapperService.mapToValueObject(businessObject, config.getValueType());
					return createResult(valueObject);
				}
				finally
				{
					mapperService.dispose();
				}
			}
			else
			{
				parentStep.boMember.setValue(entity, conversionHelper.convertValueToType(parentStep.boMember.getRealType(), valueObject));
				mergeProcess.process(entity, null, null, null);

				IMapperService mapperService = mapperServiceFactory.create();
				try
				{
					valueObject = mapperService.mapToValueObject(entity, config.getValueType());
					return createResult(valueObject);
				}
				finally
				{
					mapperService.dispose();
				}
			}
		}
		catch (WebApplicationException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			return createExceptionResult(e);
		}
		finally
		{
			postServiceCall();
		}
	}

	@POST
	@Path("{subResources:.*}")
	public StreamingOutput post(@Context HttpServletRequest request, InputStream is)
	{
		try
		{
			preServiceCall();

			String contextPath = request.getPathInfo();
			String[] path = contextPath.split("/");

			NavigationStep[] navigationSteps = navigateTo(path, NavigationMode.DEFAULT);
			NavigationStep parentStep = navigationSteps.length >= 2 ? navigationSteps[navigationSteps.length - 2] : null;
			NavigationStep lastStep = navigationSteps[navigationSteps.length - 1];

			Object[] args = getArguments(is);
			if (args.length != 1 || args[0] == null)
			{
				throw new BadRequestException("No values provided to create entity '" + Arrays.toString(path) + "'");
			}
			Object valueObject = args[0];

			IMapperServiceFactory mapperServiceFactory = getService(IMapperServiceFactory.class);
			IMergeProcess mergeProcess = getService(IMergeProcess.class);
			ITypeInfoProvider typeInfoProvider = getService(ITypeInfoProvider.class);

			String voIdMemberName = lastStep.config.getValueObjectMemberName(lastStep.metaData.getIdMember().getName());
			String voVersionMemberName = lastStep.config.getValueObjectMemberName(lastStep.metaData.getVersionMember().getName());
			ITypeInfoItem voIdMember = typeInfoProvider.getHierarchicMember(lastStep.config.getValueType(), voIdMemberName);
			ITypeInfoItem voVersionMember = typeInfoProvider.getHierarchicMember(lastStep.config.getValueType(), voVersionMemberName);
			voIdMember.setValue(valueObject, null);
			if (voVersionMember != null)
			{
				voVersionMember.setValue(valueObject, null);
			}

			IMapperService mapperService = mapperServiceFactory.create();
			try
			{
				Object businessObject = mapperService.mapToBusinessObject(valueObject);
				if (lastStep.value != null)
				{
					String boMemberName = parentStep.config.getBusinessObjectMemberName(path[path.length - 1]);
					Member boMember = parentStep.metaData.getMemberByName(boMemberName);
					if (boMember == null)
					{
						throw new BadRequestException("Entity member '" + parentStep.metaData.getEntityType().getName() + "." + boMemberName + "' not found");
					}
					if (parentStep.metaData.isRelationMember(boMember.getName()))
					{
						throw new BadRequestException("Entity member '" + parentStep.metaData.getEntityType().getName() + "." + boMemberName
								+ "' is not pointing to a relation");
					}
					if (boMember.isToMany())
					{
						ListUtil.fillList(boMember.getValue(parentStep.value), Arrays.asList(businessObject));
					}
					else
					{
						boMember.setValue(parentStep.value, businessObject);
					}
					mergeProcess.process(parentStep.value, null, null, null);
				}
				else
				{
					mergeProcess.process(businessObject, null, null, null);
				}

				valueObject = mapperService.mapToValueObject(businessObject, lastStep.config.getValueType());
				return createResult(valueObject);
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
			return createExceptionResult(e);
		}
		finally
		{
			postServiceCall();
		}
	}

	@DELETE
	@Path("{subResources:.*}")
	public StreamingOutput delete(@Context HttpServletRequest request, InputStream is)
	{
		try
		{
			preServiceCall();

			String contextPath = request.getPathInfo();
			String[] path = contextPath.split("/");

			NavigationStep[] navigationSteps = navigateTo(path, NavigationMode.TRY_ONLY);
			NavigationStep lastStep = navigationSteps[navigationSteps.length - 1];

			if (lastStep.value == null)
			{
				return new StreamingOutput()
				{
					@Override
					public void write(OutputStream outputstream) throws IOException, WebApplicationException
					{
						// intended blank
					}
				};
			}
			if (lastStep.config == null)
			{
				throw new BadRequestException("Instance to delete must be an entity: '" + Arrays.toString(path) + "'");
			}
			Object[] args = getArguments(is);

			IMergeProcess mergeProcess = getService(IMergeProcess.class);
			mergeProcess.process(null, lastStep.value, null, null);

			return new StreamingOutput()
			{
				@Override
				public void write(OutputStream outputstream) throws IOException, WebApplicationException
				{
					// intended blank
				}
			};
		}
		catch (WebApplicationException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			return createExceptionResult(e);
		}
		finally
		{
			postServiceCall();
		}
	}
}
