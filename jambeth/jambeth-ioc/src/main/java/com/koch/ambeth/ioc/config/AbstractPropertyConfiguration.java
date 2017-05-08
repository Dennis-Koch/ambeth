package com.koch.ambeth.ioc.config;

/*-
 * #%L
 * jambeth-ioc
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

import java.util.Arrays;
import java.util.Set;

import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.ioc.link.LinkController;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.config.IProperties;

public abstract class AbstractPropertyConfiguration implements IPropertyConfiguration {
	protected static final LinkedHashSet<String> ignoreClassNames = new LinkedHashSet<>(0.5f);

	static {
		ignoreClassNames.add(Thread.class.getName());
		ignoreClassNames.add(AbstractPropertyConfiguration.class.getName());
		ignoreClassNames.add(BeanContextFactory.class.getName());
		ignoreClassNames.add(LinkController.class.getName());
		ignoreClassNames.add(PropertyEmbeddedRefConfiguration.class.getName());
		ignoreClassNames.add(PropertyRefConfiguration.class.getName());
		ignoreClassNames.add(PropertyValueConfiguration.class.getName());

		ignoreClassNames.addAll(AbstractBeanConfiguration.ignoreClassNames);
	}

	public static StackTraceElement[] getCurrentStackTraceCompact() {
		return getCurrentStackTraceCompact(null);
	}

	public static StackTraceElement[] getCurrentStackTraceCompact(Set<String> ignoreClassNames,
			IProperties props) {
		if (props == null || !Boolean
				.parseBoolean(props.getString(IocConfigurationConstants.TrackDeclarationTrace, "false"))) {
			return null;
		}
		return getCurrentStackTraceCompact(ignoreClassNames);
	}

	public static StackTraceElement[] getCurrentStackTraceCompact(Set<String> ignoreClassNames) {
		StackTraceElement[] stes = Thread.currentThread().getStackTrace();
		int start = 0, end = stes.length;
		if (ignoreClassNames != null && ignoreClassNames.size() > 0) {
			for (int a = 0, size = stes.length; a < size; a++) {
				StackTraceElement ste = stes[a];
				if (!ignoreClassNames.contains(ste.getClassName())) {
					start = a;
					break;
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		for (int a = start, size = stes.length; a < size; a++) {
			StackTraceElement ste = stes[a];
			if (ste.getClassName().startsWith("org.eclipse.jdt")) {
				end = a;
				break;
			}
			if (a != start) {
				sb.append('\n');
			}
			sb.append(ste.getClassName()).append('.').append(ste.getMethodName());
			if (ste.isNativeMethod()) {
				sb.append("(Native Method)");
			}
			else if (ste.getFileName() != null) {
				sb.append('(').append(ste.getFileName());
				if (ste.getLineNumber() >= 0) {
					sb.append(':').append(ste.getLineNumber()).append(')');
				}
				else {
					sb.append(')');
				}
			}
			else {
				sb.append("(Unknown Source)");
			}
			sb.append(ste);
		}
		return Arrays.copyOfRange(stes, start, end);
	}

	protected StackTraceElement[] declarationStackTrace;

	protected IBeanConfiguration beanConfiguration;

	public AbstractPropertyConfiguration(IBeanConfiguration beanConfiguration, IProperties props) {
		this.beanConfiguration = beanConfiguration;
		ParamChecker.assertParamNotNull(beanConfiguration, "beanConfiguration");
		declarationStackTrace = getCurrentStackTraceCompact(ignoreClassNames, props);
	}

	@Override
	public IBeanConfiguration getBeanConfiguration() {
		return beanConfiguration;
	}

	@Override
	public StackTraceElement[] getDeclarationStackTrace() {
		return declarationStackTrace;
	}
}
