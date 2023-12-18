package com.koch.ambeth.sensor;

/*-
 * #%L
 * jambeth-sensor
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import com.koch.ambeth.ioc.IBeanPreProcessor;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IPropertyConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.sensor.ISensor;
import com.koch.ambeth.util.sensor.ISensorProvider;
import com.koch.ambeth.util.sensor.Sensor;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;

public class SensorPreProcessor extends SmartCopyMap<Class<?>, Object[]> implements IBeanPreProcessor, IInitializingBean {
    protected ISensorProvider sensorProvider;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(sensorProvider, "SensorProvider");
    }

    public void setSensorProvider(ISensorProvider sensorProvider) {
        this.sensorProvider = sensorProvider;
    }

    @Override
    public void preProcessProperties(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IProperties props, String beanName, Object service, Class<?> beanType,
            List<IPropertyConfiguration> propertyConfigs, Set<String> ignoredPropertyNames, IPropertyInfo[] properties) {
        ISensorProvider sensorProvider = this.sensorProvider;
        Object[] sensorFields = getSensorFields(beanType);
        Field[] relevantFields = (Field[]) sensorFields[0];
        String[] sensorNames = (String[]) sensorFields[1];
        for (int a = relevantFields.length; a-- > 0; ) {
            ISensor sensor = sensorProvider.lookup(sensorNames[a]);
            if (sensor == null) {
                continue;
            }
            try {
                relevantFields[a].set(service, sensor);
            } catch (IllegalAccessException e) {
                throw RuntimeExceptionUtil.mask(e);
            }
        }
        for (IPropertyInfo prop : properties) {
            if (!prop.isWritable()) {
                continue;
            }
            Sensor sensorAttribute = prop.getAnnotation(Sensor.class);
            if (sensorAttribute == null) {
                continue;
            }
            if (ignoredPropertyNames.contains(prop.getName())) {
                continue;
            }
            String sensorName = sensorAttribute.name();
            ISensor sensor = sensorProvider.lookup(sensorName);
            if (sensor == null) {
                continue;
            }
            prop.setValue(service, sensor);
        }
    }

    protected Object[] getSensorFields(Class<?> type) {
        Object[] sensorFields = get(type);
        if (sensorFields != null) {
            return sensorFields;
        }
        Lock writeLock = getWriteLock();
        writeLock.lock();
        try {
            sensorFields = get(type);
            if (sensorFields != null) {
                // Concurrent thread might have been faster
                return sensorFields;
            }
            ArrayList<Field> targetFields = new ArrayList<>();
            ArrayList<String> targetSensorNames = new ArrayList<>();
            Class<?> currType = type;
            while (currType != Object.class && currType != null) {
                Field[] fields = ReflectUtil.getDeclaredFields(currType);
                for (Field field : fields) {
                    int modifiers = field.getModifiers();
                    if (Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers)) {
                        continue;
                    }
                    Sensor sensorAttribute = field.getAnnotation(Sensor.class);
                    if (sensorAttribute == null) {
                        continue;
                    }
                    targetFields.add(field);
                    targetSensorNames.add(sensorAttribute.name());
                }
                currType = currType.getSuperclass();
            }
            sensorFields = new Object[] { targetFields.toArray(Field[]::new), targetSensorNames.toArray(String[]::new) };
            put(type, sensorFields);
            return sensorFields;
        } finally {
            writeLock.unlock();
        }
    }
}
