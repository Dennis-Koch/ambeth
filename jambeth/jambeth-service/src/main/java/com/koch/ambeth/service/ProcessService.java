package com.koch.ambeth.service;

/*-
 * #%L
 * jambeth-service
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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.service.model.IServiceDescription;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ProcessService implements IProcessService {
    @Autowired
    protected IThreadLocalObjectCollector objectCollector;

    @Autowired
    protected IServiceByNameProvider serviceByNameProvider;

    @Override
    public Object invokeService(IServiceDescription serviceDescription) {
        ParamChecker.assertParamNotNull(serviceDescription, "serviceDescription");
        try {
            Object service = serviceByNameProvider.getService(serviceDescription.getServiceName());

            if (service == null) {
                throw new IllegalArgumentException("Service not found. serviceName='" + serviceDescription.getServiceName() + "'");
            }
            Method method = serviceDescription.getMethod(service.getClass(), objectCollector);
            if (method == null) {
                throw new IllegalArgumentException("Requested method not found");
            }
            int modifiers = method.getModifiers();
            if (!Modifier.isPublic(modifiers)) {
                throw new IllegalArgumentException("Method is not accessible");
            }
            return method.invoke(service, serviceDescription.getArguments());
        } catch (Throwable e) {
            throw RuntimeExceptionUtil.mask(e, "Error occured while trying to call service '" + serviceDescription + "'");
        }
    }
}
