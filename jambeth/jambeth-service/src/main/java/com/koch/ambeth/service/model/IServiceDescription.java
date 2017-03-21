package com.koch.ambeth.service.model;

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

import java.lang.reflect.Method;

import com.koch.ambeth.util.annotation.XmlType;
import com.koch.ambeth.util.objectcollector.IObjectCollector;

@XmlType
public interface IServiceDescription {
	String getServiceName();

	Method getMethod(Class<?> serviceType, IObjectCollector objectCollector);

	Object[] getArguments();

	ISecurityScope[] getSecurityScopes();
}
