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

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.koch.ambeth.ioc.IFactoryBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ReflectUtil;

public class PreparedStatementParamLoggerFactory implements IFactoryBean, IInitializingBean {
	private static final Pattern SETTER = Pattern.compile("^set[A-Z].*");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final Set<Method> paramSetters;

	protected IServiceContext beanContext;

	public PreparedStatementParamLoggerFactory() {
		Set<Method> paramSetters = new HashSet<Method>();

		Method[] methods = ReflectUtil.getMethods(PreparedStatement.class);
		for (int i = methods.length; i-- > 0;) {
			Method method = methods[i];
			if (!SETTER.matcher(method.getName()).matches()) {
				continue;
			}

			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 2 || !int.class.equals(parameterTypes[0])) {
				continue;
			}

			paramSetters.add(method);
		}

		this.paramSetters = Collections.unmodifiableSet(paramSetters);
	}

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(beanContext, "beanContext");
	}

	public void setBeanContext(IServiceContext beanContext) {
		this.beanContext = beanContext;
	}

	@Override
	public Object getObject() throws Throwable {
		PreparedStatementParamLogger paramLogger =
				beanContext.registerBean(PreparedStatementParamLogger.class)
						.propertyValue("ParamSetters", paramSetters).finish();

		return paramLogger;
	}
}
