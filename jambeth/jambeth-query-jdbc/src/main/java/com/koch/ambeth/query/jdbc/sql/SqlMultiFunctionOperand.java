package com.koch.ambeth.query.jdbc.sql;

/*-
 * #%L
 * jambeth-query-jdbc
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
import com.koch.ambeth.query.IMultiValueOperand;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.IAppendable;

import java.util.List;
import java.util.Map;

public class SqlMultiFunctionOperand implements IOperand, IInitializingBean {
    protected ListToSqlUtil listToSqlUtil;

    protected IMultiValueOperand multiValueOperand;

    protected String name;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(listToSqlUtil, "listToSqlUtil");
        ParamChecker.assertNotNull(multiValueOperand, "multiValueOperand");
        ParamChecker.assertNotNull(name, "name");
    }

    public void setListToSqlUtil(ListToSqlUtil listToSqlUtil) {
        this.listToSqlUtil = listToSqlUtil;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMultiValueOperand(IMultiValueOperand multiValueOperand) {
        this.multiValueOperand = multiValueOperand;
    }

    @Override
    public void expandQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) {
        var items = multiValueOperand.getMultiValue(nameToValueMap);
        var listToSqlUtil = this.listToSqlUtil;
        var name = this.name;
        for (int a = 0, size = items.size(); a < size; a++) {
            var item = items.get(a);
            querySB.append(name).append('(');
            listToSqlUtil.expandValue(querySB, item, this, nameToValueMap);
            querySB.append(')');
        }
    }
}
