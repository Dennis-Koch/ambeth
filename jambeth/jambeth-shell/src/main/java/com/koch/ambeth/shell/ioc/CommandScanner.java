package com.koch.ambeth.shell.ioc;

/*-
 * #%L
 * jambeth-shell
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import com.koch.ambeth.ioc.IBeanPostProcessor;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import com.koch.ambeth.shell.core.CommandBinding;
import com.koch.ambeth.shell.core.CommandBindingExtendable;
import com.koch.ambeth.shell.core.CommandBindingImpl;
import com.koch.ambeth.shell.core.CommandExtensionExtendable;
import com.koch.ambeth.shell.core.annotation.Command;
import com.koch.ambeth.shell.core.annotation.CommandArg;
import com.koch.ambeth.shell.core.annotation.CommandExtension;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.collections.ArrayList;

public class CommandScanner implements IBeanPostProcessor {

	@Override
	public Object postProcessBean(IBeanContextFactory bcf, IServiceContext sc, IBeanConfiguration bc,
			Class<?> clazz, Object bean, Set<Class<?>> unknown) {
		CommandExtension commandExtension = clazz.getAnnotation(CommandExtension.class);
		if (commandExtension != null) {
			if (sc.isRunning()) {
				sc.link(bean).to(CommandExtensionExtendable.class).with(commandExtension.command())
						.finishLink();
			}
			else {
				bcf.link(bean).to(CommandExtensionExtendable.class).with(commandExtension.command());
			}
		}

		Method[] methods = ReflectUtil.getDeclaredMethodsInHierarchy(clazz);
		MethodAccess methodAccess = null;
		for (Method method : methods) {
			Command commandAnnotation = method.getAnnotation(Command.class);
			if (commandAnnotation == null) {
				continue;
			}
			String description = commandAnnotation.description();
			if (!commandAnnotation.descriptionFile().isEmpty()) {
				description = commandAnnotation.descriptionFile();
			}
			if (methodAccess == null) {
				methodAccess = MethodAccess.get(method.getDeclaringClass());
			}
			if (sc.isRunning()) {
				CommandBinding cbBeanConfig = sc.registerBean(CommandBindingImpl.class) //
						.propertyValue("Name", commandAnnotation.name()) //
						.propertyValue("Description", description) //
						.propertyValue("MethodAccess", methodAccess) //
						.propertyValue("Method", method) //
						.propertyValue("CommandBean", bean) //
						.propertyValue("Args", getCommandArgsForMethod(method)).finish();
				sc.link(cbBeanConfig).to(CommandBindingExtendable.class).with(commandAnnotation.name())
						.finishLink();
			}
			else {
				IBeanConfiguration cbBeanConfig = bcf.registerBean(CommandBindingImpl.class) //
						.propertyValue("Name", commandAnnotation.name()) //
						.propertyValue("Description", description) //
						.propertyValue("MethodAccess", methodAccess) //
						.propertyValue("Method", method) //
						.propertyValue("CommandBean", bean) //
						.propertyValue("Args", getCommandArgsForMethod(method));
				bcf.link(cbBeanConfig).to(CommandBindingExtendable.class).with(commandAnnotation.name());
			}
		}

		return bean;
	}

	private CommandArg[] getCommandArgsForMethod(Method method) {
		ArrayList<CommandArg> args = new ArrayList<>();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		for (Annotation[] as : parameterAnnotations) {
			for (Annotation a : as) {
				if (a instanceof CommandArg) {
					args.add((CommandArg) a);
				}
			}
		}
		return args.toArray(CommandArg.class);
	}
}
