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
import com.koch.ambeth.util.typeinfo.ITypeInfoProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.NoContentException;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Path("/GenericEntityService")
// @Consumes({ MediaType.TEXT_PLAIN })
// @Produces({ MediaType.TEXT_PLAIN })
public class GenericEntityREST extends AbstractServiceREST {
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
        } catch (ClassNotFoundException e) {
            // intended blank
        }

        IValueObjectConfig config = null;
        if (metaData == null) {
            config = entityMetaDataProvider.getValueObjectConfig(valueObjectTypeName);
        }
        if (config == null) {
            if (metaData == null) {
                throw new BadRequestException("Type '" + valueObjectTypeName + "' neither known as an entity nor as a DTO type");
            }
        }
        if (metaData == null) {
            metaData = entityMetaDataProvider.getMetaData(config.getEntityType());
        }

        Object value = null;

        String entityId = null;

        if (path.length > 1) {
            entityId = path[1];
            Object convertedEntityId = conversionHelper.convertValueToType(metaData.getIdMember().getRealType(), entityId);
            value = cache.getObject(metaData.getEntityType(), convertedEntityId);
        }
        StringBuilder pathSB = new StringBuilder(valueObjectTypeName);

        if (value == null) {
            if (navigationMode == NavigationMode.TRY_ONLY) {
                return new NavigationStep[] { new NavigationStep(null, metaData, config, null) };
            }
            throw new NotFoundException("Entity '" + pathSB + "'  with id '" + entityId + "' not found");
        }

        ArrayList<NavigationStep> navigationSteps = new ArrayList<>();

        for (int index = 4, size = path.length; index < size; index++) {
            String voMemberName = path[index];
            pathSB.append('.').append(voMemberName);

            String boMemberName = config != null ? config.getBusinessObjectMemberName(voMemberName) : voMemberName;
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
                    List<Class<?>> availableConfigs = entityMetaDataProvider.getValueObjectTypesByEntityType(metaData.getEntityType());
                    if (availableConfigs.isEmpty()) {
                        throw new BadRequestException("Entity member '" + pathSB + "' not serializable");
                    }
                    config = entityMetaDataProvider.getValueObjectConfig(availableConfigs.get(0));
                }
            } else {
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
            } else {
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
    public StreamingOutput get(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, () -> {
            var basePath = getClass().getAnnotation(Path.class).value();
            var contextPath = request.getPathInfo();
            if (!basePath.startsWith("/")) {
                basePath = "/" + basePath;
            }
            if (!basePath.endsWith("/")) {
                basePath += "/";
            }
            if (contextPath.startsWith(basePath)) {
                contextPath = contextPath.substring(basePath.length());
            }
            var path = contextPath.split("/");

            var navigationSteps = navigateTo(path, NavigationMode.DEFAULT);
            var lastStep = navigationSteps[navigationSteps.length - 1];

            if (lastStep.value instanceof IEntityMetaDataHolder && lastStep.config != null) {
                var mapperServiceFactory = getService(IMapperServiceFactory.class);

                var mapperService = mapperServiceFactory.create();
                try {
                    var valueObject = mapperService.mapToValueObject(lastStep.value, lastStep.config.getValueType());
                    return createSynchronousResult(valueObject, request, response);
                } finally {
                    mapperService.dispose();
                }
            }
            return createSynchronousResult(lastStep.value, request, response);
        });
    }

    @PUT
    @Path("{subResources:.*}")
    public StreamingOutput put(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, is, args -> {
            var contextPath = request.getPathInfo();
            var path = contextPath.split("/");

            var navigationSteps = navigateTo(path, NavigationMode.DEFAULT);
            var parentStep = navigationSteps.length >= 2 ? navigationSteps[navigationSteps.length - 2] : null;
            var lastStep = navigationSteps[navigationSteps.length - 1];

            if (args.length != 1 || args[0] == null) {
                throw new BadRequestException("No values provided to modify entity '" + Arrays.toString(path) + "'");
            }
            var valueObject = args[0];

            var metaData = lastStep.metaData != null ? lastStep.metaData : parentStep.metaData;
            var config = lastStep.metaData != null ? lastStep.config : parentStep.config;
            var entity = lastStep.metaData != null ? lastStep.value : parentStep.value;

            var conversionHelper = getService(IConversionHelper.class);
            var mapperServiceFactory = getService(IMapperServiceFactory.class);
            var mergeProcess = getService(IMergeProcess.class);
            var typeInfoProvider = getService(ITypeInfoProvider.class);

            if (lastStep.metaData != null) {
                var entityId = metaData.getIdMember().getValue(entity);
                var voIdMemberName = config.getValueObjectMemberName(metaData.getIdMember().getName());
                var voIdMember = typeInfoProvider.getHierarchicMember(config.getValueType(), voIdMemberName);
                voIdMember.setValue(valueObject, conversionHelper.convertValueToType(voIdMember.getRealType(), entityId));

                var mapperService = mapperServiceFactory.create();
                try {
                    var businessObject = mapperService.mapToBusinessObject(valueObject);
                    mergeProcess.process(businessObject);

                    valueObject = mapperService.mapToValueObject(businessObject, config.getValueType());
                    return valueObject;
                } finally {
                    mapperService.dispose();
                }
            } else {
                parentStep.boMember.setValue(entity, conversionHelper.convertValueToType(parentStep.boMember.getRealType(), valueObject));
                mergeProcess.process(entity);

                var mapperService = mapperServiceFactory.create();
                try {
                    valueObject = mapperService.mapToValueObject(entity, config.getValueType());
                    return valueObject;
                } finally {
                    mapperService.dispose();
                }
            }
        });
    }

    @POST
    @Path("{subResources:.*}")
    public StreamingOutput post(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, is, args -> {
            var contextPath = request.getPathInfo();
            var path = contextPath.split("/");

            var navigationSteps = navigateTo(path, NavigationMode.DEFAULT);
            var parentStep = navigationSteps.length >= 2 ? navigationSteps[navigationSteps.length - 2] : null;
            var lastStep = navigationSteps[navigationSteps.length - 1];

            if (args.length != 1 || args[0] == null) {
                throw new BadRequestException("No values provided to create entity '" + Arrays.toString(path) + "'");
            }
            var valueObject = args[0];

            if (!lastStep.config.getValueType().isAssignableFrom(valueObject.getClass())) {
                throw new BadRequestException("Expected value type '" + lastStep.config.getValueType().getName() + "'. Given value type '" + valueObject.getClass().getName() + "'");
            }

            var mapperServiceFactory = getService(IMapperServiceFactory.class);
            var mergeProcess = getService(IMergeProcess.class);
            var typeInfoProvider = getService(ITypeInfoProvider.class);

            var voIdMemberName = lastStep.config.getValueObjectMemberName(lastStep.metaData.getIdMember().getName());
            var voVersionMemberName = lastStep.config.getValueObjectMemberName(lastStep.metaData.getVersionMember().getName());
            var voIdMember = typeInfoProvider.getHierarchicMember(lastStep.config.getValueType(), voIdMemberName);
            var voVersionMember = typeInfoProvider.getHierarchicMember(lastStep.config.getValueType(), voVersionMemberName);
            voIdMember.setValue(valueObject, null);
            if (voVersionMember != null) {
                voVersionMember.setValue(valueObject, null);
            }

            var mapperService = mapperServiceFactory.create();
            try {
                var businessObject = mapperService.mapToBusinessObject(valueObject);
                if (lastStep.value != null && parentStep != null) {
                    var boMemberName = parentStep.config.getBusinessObjectMemberName(path[path.length - 1]);
                    var boMember = parentStep.metaData.getMemberByName(boMemberName);
                    if (boMember == null) {
                        throw new BadRequestException("Entity member '" + parentStep.metaData.getEntityType().getName() + "." + boMemberName + "' not found");
                    }
                    if (parentStep.metaData.isRelationMember(boMember.getName())) {
                        throw new BadRequestException("Entity member '" + parentStep.metaData.getEntityType().getName() + "." + boMemberName + "' is not pointing to a relation");
                    }
                    if (boMember.isToMany()) {
                        ListUtil.fillList(boMember.getValue(parentStep.value), Arrays.asList(businessObject));
                    } else {
                        boMember.setValue(parentStep.value, businessObject);
                    }
                    mergeProcess.process(parentStep.value);
                } else {
                    mergeProcess.process(businessObject);
                }

                valueObject = mapperService.mapToValueObject(businessObject, lastStep.config.getValueType());
                return valueObject;
            } finally {
                mapperService.dispose();
            }
        });
    }

    @DELETE
    @Path("{subResources:.*}")
    public StreamingOutput delete(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, () -> {
            var contextPath = request.getPathInfo();
            var path = contextPath.split("/");

            var navigationSteps = navigateTo(path, NavigationMode.TRY_ONLY);
            var lastStep = navigationSteps[navigationSteps.length - 1];

            if (lastStep.value == null) {
                throw new NoContentException("Resource not deleted. Reason: resource not found");
            }
            if (lastStep.config == null) {
                throw new BadRequestException("Instance to delete must be an entity: '" + Arrays.toString(path) + "'");
            }
            var mergeProcess = getService(IMergeProcess.class);
            mergeProcess.begin().delete(lastStep.value).finish();

            throw new NoContentException("Resource deleted");
        });
    }

    private enum NavigationMode {
        DEFAULT, TRY_ONLY, SKIP_LAST;
    }

    private final class NavigationStep {
        public final Object value;

        public final IValueObjectConfig config;

        public final IEntityMetaData metaData;

        public final Member boMember;

        public NavigationStep(Object value, IEntityMetaData metaData, IValueObjectConfig config, Member boMember) {
            super();
            this.value = value;
            this.metaData = metaData;
            this.config = config;
            this.boMember = boMember;
        }
    }
}
