package com.koch.ambeth.service.transfer;

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

import com.koch.ambeth.service.metadata.IDTOType;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.service.model.IServiceDescription;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringConversionHelper;
import com.koch.ambeth.util.objectcollector.IObjectCollector;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceDescription implements IServiceDescription, IDTOType, IPrintable {
    @XmlElement(required = true)
    protected String serviceName;

    @XmlElement(required = true)
    protected String methodName;

    @XmlElement(required = true)
    protected Class<?>[] paramTypes;

    @XmlElement(required = true)
    protected ISecurityScope[] securityScopes;

    @XmlElement(required = true)
    protected Object[] arguments;

    protected transient Method method;

    @Override
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @SneakyThrows
    @Override
    public Method getMethod(Class<?> serviceType, IObjectCollector objectCollector) {
        if (method == null) {
            var paramTypes = getParamTypes();
            if (paramTypes == null) {
                throw new IllegalStateException("paramTypes must be valid");
            }
            for (var paramType : paramTypes) {
                if (paramType == null) {
                    // paramType could not be resolved with the current classloader
                    return null;
                }
            }
            try {
                method = serviceType.getMethod(StringConversionHelper.upperCaseFirst(objectCollector, getMethodName()), paramTypes);
            } catch (NoSuchMethodException e) {
                method = serviceType.getMethod(StringConversionHelper.lowerCaseFirst(objectCollector, getMethodName()), paramTypes);
            }
        }
        return method;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    @Override
    public ISecurityScope[] getSecurityScopes() {
        return securityScopes;
    }

    public void setSecurityScopes(ISecurityScope[] securityScopes) {
        this.securityScopes = securityScopes;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append(getServiceName()).append(" => ").append(getMethodName()).append('(');
        var first = true;
        var paramTypes = getParamTypes();
        if (paramTypes == null) {
            sb.append("...");
        } else {
            for (var paramType : paramTypes) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                if (paramType == null) {
                    sb.append("<n/a>");
                } else {
                    sb.append(paramType.getSimpleName());
                }
            }
        }
        sb.append(')');
    }
}
