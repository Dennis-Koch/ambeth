package com.koch.ambeth.testutil;

/*-
 * #%L
 * jambeth-information-bus-with-persistence-test
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.jdbc.connection.IPreparedStatementParamLogger;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.LinkedHashMap;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class PreparedStatementParamLogger implements IPreparedStatementParamLogger, IInitializingBean {
    private static final String NL = System.getProperty("line.separator");
    protected final List<ILinkedMap<Integer, Object>> params = new ArrayList<>();
    protected ILinkedMap<Integer, Object> currentBatch = new LinkedHashMap<>();
    protected IConversionHelper conversionHelper;
    protected Set<Method> paramSetters;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
        ParamChecker.assertNotNull(paramSetters, "paramSetters");
    }

    public void setConversionHelper(IConversionHelper conversionHelper) {
        this.conversionHelper = conversionHelper;
    }

    public void setParamSetters(Set<Method> paramSetters) {
        this.paramSetters = paramSetters;
    }

    @Override
    public boolean isCallToBeLogged(Method method) {
        return paramSetters.contains(method);
    }

    @Override
    public void logParams(Method method, Object[] args) {
        if (!isCallToBeLogged(method)) {
            return;
        }

        currentBatch.put((Integer) args[0], args[1]);
    }

    @Override
    public void addBatch() {
        params.add(currentBatch);
        currentBatch = new LinkedHashMap<>();
    }

    @Override
    public void doLog() {
        if (!log.isDebugEnabled() || currentBatch.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        appendDataSet(sb, currentBatch);

        params.clear();
        currentBatch.clear();

        log.debug(sb.toString());
    }

    @Override
    public void doLogBatch() {
        if (!log.isDebugEnabled() || params.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0, size = params.size(); i < size; i++) {
            appendDataSet(sb, params.get(i));
            if (i < size - 1) {
                sb.append(NL).append("\t");
            }
        }

        params.clear();
        currentBatch.clear();

        log.debug(sb.toString());
    }

    private void appendDataSet(StringBuilder sb, ILinkedMap<Integer, Object> dataSet) {
        sb.append("[ ");
        String separator = "";
        for (Entry<Integer, Object> entry : dataSet) {
            Integer index = entry.getKey();
            Object param = entry.getValue();

            String paramString = conversionHelper.convertValueToType(String.class, param);
            sb.append(separator).append(index).append(": ").append(paramString);
            separator = ", ";
        }
        sb.append(" ]");
    }
}
