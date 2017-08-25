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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.core.StreamingOutput;

import com.koch.ambeth.mapping.IMapperService;
import com.koch.ambeth.mapping.IMapperServiceFactory;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.IClassCache;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.typeinfo.ITypeInfoItem;
import com.koch.ambeth.util.typeinfo.ITypeInfoProvider;

@Path("/GenericEntityService")
// @Consumes({ MediaType.TEXT_PLAIN })
// @Produces({ MediaType.TEXT_PLAIN })
public class GenericEntityREST extends AbstractServiceREST {
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

	private final class NavigationStep {
		public final Object value;

		public final IValueObjectConfig config;

		public final IEntityMetaData metaData;

		public final Member boMember;

		public NavigationStep(Object value, IEntityMetaData metaData, IValueObjectConfig config,
				Member boMember) {
			super();
			this.value = value;
			this.metaData = metaData;
			this.config = config;
			this.boMember = boMember;
		}
	}

	private enum NavigationMode {
		DEFAULT, TRY_ONLY, SKIP_LAST;
	}

	protected NavigationStep[] navigateTo(String[] path, NavigationMode navigationMode) {
		ICache cache = getService(ICache.class);
		IConversionHelper conversionHelper = getService(IConversionHelper.class);
		IEntityMetaDataProvider entityMetaDataProvider = getService(IEntityMetaDataProvider.class);

		String valueObjectTypeName = path[0];
		IEntityMetaData metaData = null;
		IClassCache classCache = getService(IClassCache.class);
		try {
			Class<?> entityType = classCache.loadClass(valueObjectTypeName);
			metaData = entityMetaDataProvider.getMetaData(entityType, true);
		}
		catch (ClassNotFoundException e) {
			// intended blank
		}

		IValueObjectConfig config = null;
		if (metaData == null) {
			config = entityMetaDataProvider.getValueObjectConfig(valueObjectTypeName);
		}
		if (config == null) {
			if (metaData == null) {
				throw new BadRequestException(
						"Type '" + valueObjectTypeName + "' neither known as an entity nor as a DTO type");
			}
		}
		if (metaData == null) {
			metaData = entityMetaDataProvider.getMetaData(config.getEntityType());
		}

		Object value = null;

		String entityId = null;

		if (path.length > 1) {
			entityId = path[1];
			Object convertedEntityId = conversionHelper
					.convertValueToType(metaData.getIdMember().getRealType(), entityId);
			value = cache.getObject(metaData.getEntityType(), convertedEntityId);
		}
		StringBuilder pathSB = new StringBuilder(valueObjectTypeName);

		if (value == null) {
			if (navigationMode == NavigationMode.TRY_ONLY) {
				return new NavigationStep[] {new NavigationStep(null, metaData, config, null)};
			}
			throw new NotFoundException("Entity '" + pathSB + "'  with id '" + entityId + "' not found");
		}

		ArrayList<NavigationStep> navigationSteps = new ArrayList<>();

		for (int index = 4, size = path.length; index < size; index++) {
			String voMemberName = path[index];
			pathSB.append('.').append(voMemberName);

			String boMemberName = config != null ? config.getBusinessObjectMemberName(voMemberName)
					: voMemberName;
			Member member = metaData.getMemberByName(boMemberName);
			if (member == null) {
				throw new BadRequestException("Entity member '" + pathSB + "' not known");
			}
			navigationSteps.add(new NavigationStep(value, metaData, config, member));
			if (value == null) {
				if (navigationMode == NavigationMode.TRY_ONLY) {
					return navigationSteps.toArray(NavigationStep.class);
				}
				throw new NotFoundException("Entity '" + pathSB + "' not found");
			}
			if (metaData.isRelationMember(boMemberName)) {
				metaData = entityMetaDataProvider.getMetaData(member.getElementType());
				if (config != null) {
					List<Class<?>> availableConfigs = entityMetaDataProvider
							.getValueObjectTypesByEntityType(metaData.getEntityType());
					if (availableConfigs.isEmpty()) {
						throw new BadRequestException("Entity member '" + pathSB + "' not serializable");
					}
					config = entityMetaDataProvider.getValueObjectConfig(availableConfigs.get(0));
				}
			}
			else {
				metaData = null;
				config = null;
			}

			if (navigationMode == NavigationMode.SKIP_LAST && index == size - 1) {
				break;
			}
			value = member.getValue(value);

			if (!member.isToMany() || index + 1 < size) {
				continue;
			}

			// next item must be an index specification
			index++;
			int indexSpec = conversionHelper.convertValueToType(int.class, path[index]);
			pathSB.append('[').append(indexSpec).append(']');

			if (value instanceof List) {
				value = ((List<?>) value).get(indexSpec);
			}
			else {
				Iterator<?> iter = ((Iterable<?>) value).iterator();
				while (true) {
					if (!iter.hasNext()) {
						throw new NotFoundException("Entity '" + pathSB + "' not found");
					}
					if (indexSpec == 0) {
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
	public StreamingOutput get(@Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			String basePath = getClass().getAnnotation(Path.class).value();
			String contextPath = request.getPathInfo();
			if (!basePath.startsWith("/")) {
				basePath = "/" + basePath;
			}
			if (!basePath.endsWith("/")) {
				basePath += "/";
			}
			if (contextPath.startsWith(basePath)) {
				contextPath = contextPath.substring(basePath.length());
			}
			String[] path = contextPath.split("/");

			NavigationStep[] navigationSteps = navigateTo(path, NavigationMode.DEFAULT);
			NavigationStep lastStep = navigationSteps[navigationSteps.length - 1];

			if (lastStep.value instanceof IEntityMetaDataHolder && lastStep.config != null) {
				IMapperServiceFactory mapperServiceFactory = getService(IMapperServiceFactory.class);

				IMapperService mapperService = mapperServiceFactory.create();
				try {
					Object valueObject = mapperService.mapToValueObject(lastStep.value,
							lastStep.config.getValueType());
					return createSynchronousResult(valueObject, request, response);
				}
				finally {
					mapperService.dispose();
				}
			}
			return createSynchronousResult(lastStep.value, request, response);
		}
		catch (WebApplicationException e) {
			throw e;
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}

	// @PATCH
	// @Path("{subResources:.*}")
	// public StreamingOutput patch(InputStream is, @Context HttpServletRequest request,
	// @Context HttpServletResponse response) {
	// try {
	// throw new NotSupportedException("Not yet supported");
	//
	// // preServiceCall(request);
	// //
	// // String contextPath = request.getPathInfo();
	// // String[] path = contextPath.split("/");
	// //
	// // NavigationStep[] navigationSteps = navigateTo(path, NavigationMode.DEFAULT);
	// // NavigationStep lastStep = navigationSteps[navigationSteps.length - 1];
	// //
	// // if (lastStep.metaData == null)
	// // {
	// // throw new WebApplicationException("Resource is not an entity", 422); // unprocessable
	// // request
	// // }
	// // Object[] args = getArguments(is, request);
	// // if (args.length != 1 || args[0] == null)
	// // {
	// // throw new BadRequestException("No values provided to create entity '" +
	// // Arrays.toString(path) + "'");
	// // }
	// // Object valueObject = args[0];
	// //
	// // IConversionHelper conversionHelper = getService(IConversionHelper.class);
	// // IMapperServiceFactory mapperServiceFactory = getService(IMapperServiceFactory.class);
	// // IMergeProcess mergeProcess = getService(IMergeProcess.class);
	// // ITypeInfoProvider typeInfoProvider = getService(ITypeInfoProvider.class);
	// //
	// // IMapperService mapperService = mapperServiceFactory.create();
	// // try
	// // {
	// // Object businessObject = lastStep.value;
	// // typeInfoProvider.getClass<?> valueType = lastStep.config.getValueType();
	// //
	// // mergeProcess.process(businessObject, null, null, null);
	// //
	// // valueObject = mapperService.mapToValueObject(businessObject,
	// // lastStep.config.getValueType());
	// // }
	// // finally
	// // {
	// // mapperService.dispose();
	// // }
	// // return createResult(valueObject, response);
	// }
	// catch (WebApplicationException e) {
	// throw e;
	// }
	// catch (Throwable e) {
	// return createExceptionResult(e, request, response);
	// }
	// finally {
	// postServiceCall();
	// }
	// }

	@PUT
	@Path("{subResources:.*}")
	public StreamingOutput put(InputStream is, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			String contextPath = request.getPathInfo();
			String[] path = contextPath.split("/");

			NavigationStep[] navigationSteps = navigateTo(path, NavigationMode.DEFAULT);
			NavigationStep parentStep = navigationSteps.length >= 2
					? navigationSteps[navigationSteps.length - 2]
					: null;
			NavigationStep lastStep = navigationSteps[navigationSteps.length - 1];

			Object[] args = getArguments(is, request);
			if (args.length != 1 || args[0] == null) {
				throw new BadRequestException(
						"No values provided to modify entity '" + Arrays.toString(path) + "'");
			}
			Object valueObject = args[0];

			IEntityMetaData metaData = lastStep.metaData != null ? lastStep.metaData
					: parentStep.metaData;
			IValueObjectConfig config = lastStep.metaData != null ? lastStep.config : parentStep.config;
			Object entity = lastStep.metaData != null ? lastStep.value : parentStep.value;

			IConversionHelper conversionHelper = getService(IConversionHelper.class);
			IMapperServiceFactory mapperServiceFactory = getService(IMapperServiceFactory.class);
			IMergeProcess mergeProcess = getService(IMergeProcess.class);
			ITypeInfoProvider typeInfoProvider = getService(ITypeInfoProvider.class);

			if (lastStep.metaData != null) {
				Object entityId = metaData.getIdMember().getValue(entity);
				String voIdMemberName = config.getValueObjectMemberName(metaData.getIdMember().getName());
				ITypeInfoItem voIdMember = typeInfoProvider.getHierarchicMember(config.getValueType(),
						voIdMemberName);
				voIdMember.setValue(valueObject,
						conversionHelper.convertValueToType(voIdMember.getRealType(), entityId));

				IMapperService mapperService = mapperServiceFactory.create();
				try {
					Object businessObject = mapperService.mapToBusinessObject(valueObject);
					mergeProcess.process(businessObject, null, null, null);

					valueObject = mapperService.mapToValueObject(businessObject, config.getValueType());
					return createResult(valueObject, request, response);
				}
				finally {
					mapperService.dispose();
				}
			}
			else {
				parentStep.boMember.setValue(entity,
						conversionHelper.convertValueToType(parentStep.boMember.getRealType(), valueObject));
				mergeProcess.process(entity, null, null, null);

				IMapperService mapperService = mapperServiceFactory.create();
				try {
					valueObject = mapperService.mapToValueObject(entity, config.getValueType());
					return createResult(valueObject, request, response);
				}
				finally {
					mapperService.dispose();
				}
			}
		}
		catch (WebApplicationException e) {
			throw e;
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}

	@POST
	@Path("{subResources:.*}")
	public StreamingOutput post(InputStream is, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			String contextPath = request.getPathInfo();
			String[] path = contextPath.split("/");

			NavigationStep[] navigationSteps = navigateTo(path, NavigationMode.DEFAULT);
			NavigationStep parentStep = navigationSteps.length >= 2
					? navigationSteps[navigationSteps.length - 2]
					: null;
			NavigationStep lastStep = navigationSteps[navigationSteps.length - 1];

			Object[] args = getArguments(is, request);
			if (args.length != 1 || args[0] == null) {
				throw new BadRequestException(
						"No values provided to create entity '" + Arrays.toString(path) + "'");
			}
			Object valueObject = args[0];

			if (!lastStep.config.getValueType().isAssignableFrom(valueObject.getClass())) {
				throw new BadRequestException(
						"Expected value type '" + lastStep.config.getValueType().getName()
								+ "'. Given value type '" + valueObject.getClass().getName() + "'");
			}

			IMapperServiceFactory mapperServiceFactory = getService(IMapperServiceFactory.class);
			IMergeProcess mergeProcess = getService(IMergeProcess.class);
			ITypeInfoProvider typeInfoProvider = getService(ITypeInfoProvider.class);

			String voIdMemberName = lastStep.config
					.getValueObjectMemberName(lastStep.metaData.getIdMember().getName());
			String voVersionMemberName = lastStep.config
					.getValueObjectMemberName(lastStep.metaData.getVersionMember().getName());
			ITypeInfoItem voIdMember = typeInfoProvider
					.getHierarchicMember(lastStep.config.getValueType(), voIdMemberName);
			ITypeInfoItem voVersionMember = typeInfoProvider
					.getHierarchicMember(lastStep.config.getValueType(), voVersionMemberName);
			voIdMember.setValue(valueObject, null);
			if (voVersionMember != null) {
				voVersionMember.setValue(valueObject, null);
			}

			IMapperService mapperService = mapperServiceFactory.create();
			try {
				Object businessObject = mapperService.mapToBusinessObject(valueObject);
				if (lastStep.value != null && parentStep != null) {
					String boMemberName = parentStep.config
							.getBusinessObjectMemberName(path[path.length - 1]);
					Member boMember = parentStep.metaData.getMemberByName(boMemberName);
					if (boMember == null) {
						throw new BadRequestException(
								"Entity member '" + parentStep.metaData.getEntityType().getName() + "."
										+ boMemberName + "' not found");
					}
					if (parentStep.metaData.isRelationMember(boMember.getName())) {
						throw new BadRequestException(
								"Entity member '" + parentStep.metaData.getEntityType().getName() + "."
										+ boMemberName + "' is not pointing to a relation");
					}
					if (boMember.isToMany()) {
						ListUtil.fillList(boMember.getValue(parentStep.value), Arrays.asList(businessObject));
					}
					else {
						boMember.setValue(parentStep.value, businessObject);
					}
					mergeProcess.process(parentStep.value, null, null, null);
				}
				else {
					mergeProcess.process(businessObject, null, null, null);
				}

				valueObject = mapperService.mapToValueObject(businessObject,
						lastStep.config.getValueType());
				return createResult(valueObject, request, response);
			}
			finally {
				mapperService.dispose();
			}
		}
		catch (WebApplicationException e) {
			throw e;
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}

	@DELETE
	@Path("{subResources:.*}")
	public StreamingOutput delete(@Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		IStateRollback rollback = preServiceCall(request, response);
		try {
			String contextPath = request.getPathInfo();
			String[] path = contextPath.split("/");

			NavigationStep[] navigationSteps = navigateTo(path, NavigationMode.TRY_ONLY);
			NavigationStep lastStep = navigationSteps[navigationSteps.length - 1];

			if (lastStep.value == null) {
				throw new NoContentException("Resource not deleted. Reason: resource not found");
			}
			if (lastStep.config == null) {
				throw new BadRequestException(
						"Instance to delete must be an entity: '" + Arrays.toString(path) + "'");
			}
			IMergeProcess mergeProcess = getService(IMergeProcess.class);
			mergeProcess.process(null, lastStep.value, null, null);

			throw new NoContentException("Resource deleted");
		}
		catch (WebApplicationException e) {
			throw e;
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}
}
