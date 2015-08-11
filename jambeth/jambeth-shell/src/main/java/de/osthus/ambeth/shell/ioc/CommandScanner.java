package de.osthus.ambeth.shell.ioc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.osthus.ambeth.ioc.IBeanPostProcessor;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import de.osthus.ambeth.shell.core.CommandBinding;
import de.osthus.ambeth.shell.core.CommandBindingExtendable;
import de.osthus.ambeth.shell.core.CommandBindingImpl;
import de.osthus.ambeth.shell.core.annotation.Command;
import de.osthus.ambeth.shell.core.annotation.CommandArg;
import de.osthus.ambeth.util.ReflectUtil;

public class CommandScanner implements IBeanPostProcessor
{

	@Override
	public Object postProcessBean(IBeanContextFactory bcf, IServiceContext sc, IBeanConfiguration bc, Class<?> clazz, Object bean, Set<Class<?>> unknown)
	{
		Method[] methods = ReflectUtil.getDeclaredMethodsInHierarchy(clazz);
		MethodAccess methodAccess = null;
		for (Method method : methods)
		{
			Command commandAnnotation = method.getAnnotation(Command.class);
			if (commandAnnotation == null)
			{
				continue;
			}
			String description = commandAnnotation.description();
			if (!commandAnnotation.descriptionFile().isEmpty())
			{
				description = commandAnnotation.descriptionFile();
			}
			if (methodAccess == null)
			{
				methodAccess = MethodAccess.get(method.getDeclaringClass());
			}
			int methodIndex = methodAccess.getIndex(method.getName(), method.getParameterTypes());
			if (sc.isRunning())
			{
				CommandBinding cbBeanConfig = sc.registerBean(CommandBindingImpl.class) //
						.propertyValue("Name", commandAnnotation.name()) //
						.propertyValue("Description", description) //
						// .propertyValue("Method", method) //
						.propertyValue("MethodAccess", methodAccess) //
						// .propertyValue("MethodName", method.getName()) //
						.propertyValue("ParameterTypes", method.getParameterTypes()) //
						.propertyValue("MethodIndex", methodIndex) //
						.propertyValue("CommandBean", bean) //
						.propertyValue("Args", getCommandArgsForMethod(method)).finish();
				sc.link(cbBeanConfig).to(CommandBindingExtendable.class).with(commandAnnotation.name()).finishLink();
			}
			else
			{
				IBeanConfiguration cbBeanConfig = bcf.registerBean(CommandBindingImpl.class) //
						.propertyValue("Name", commandAnnotation.name()) //
						.propertyValue("Description", description) //
						// .propertyValue("Method", method) //
						.propertyValue("MethodAccess", methodAccess) //
						// .propertyValue("MethodName", method.getName()) //
						.propertyValue("ParameterTypes", method.getParameterTypes()) //
						.propertyValue("MethodIndex", methodIndex) //
						.propertyValue("CommandBean", bean) //
						.propertyValue("Args", getCommandArgsForMethod(method));
				bcf.link(cbBeanConfig).to(CommandBindingExtendable.class).with(commandAnnotation.name());
			}
		}

		return bean;
	}

	private List<CommandArg> getCommandArgsForMethod(Method method)
	{
		List<CommandArg> args = new ArrayList<CommandArg>();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		for (Annotation[] as : parameterAnnotations)
		{
			for (Annotation a : as)
			{
				if (a instanceof CommandArg)
				{
					args.add((CommandArg) a);
				}
			}
		}
		return args;
	}
}
