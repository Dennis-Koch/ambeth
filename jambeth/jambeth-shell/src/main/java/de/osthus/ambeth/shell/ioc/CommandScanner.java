package de.osthus.ambeth.shell.ioc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.IBeanPostProcessor;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import de.osthus.ambeth.shell.core.CommandBinding;
import de.osthus.ambeth.shell.core.CommandBindingExtendable;
import de.osthus.ambeth.shell.core.CommandBindingImpl;
import de.osthus.ambeth.shell.core.CommandExtensionExtendable;
import de.osthus.ambeth.shell.core.annotation.Command;
import de.osthus.ambeth.shell.core.annotation.CommandArg;
import de.osthus.ambeth.shell.core.annotation.CommandExtension;
import de.osthus.ambeth.util.ReflectUtil;

public class CommandScanner implements IBeanPostProcessor
{

	@Override
	public Object postProcessBean(IBeanContextFactory bcf, IServiceContext sc, IBeanConfiguration bc, Class<?> clazz, Object bean, Set<Class<?>> unknown)
	{
		CommandExtension commandExtension = clazz.getAnnotation(CommandExtension.class);
		if (commandExtension != null)
		{
			if (sc.isRunning())
			{
				sc.link(bean).to(CommandExtensionExtendable.class).with(commandExtension.command()).finishLink();
			}
			else
			{
				bcf.link(bean).to(CommandExtensionExtendable.class).with(commandExtension.command());
			}
		}

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
			if (sc.isRunning())
			{
				CommandBinding cbBeanConfig = sc.registerBean(CommandBindingImpl.class) //
						.propertyValue("Name", commandAnnotation.name()) //
						.propertyValue("Description", description) //
						.propertyValue("MethodAccess", methodAccess) //
						.propertyValue("Method", method) //
						.propertyValue("CommandBean", bean) //
						.propertyValue("Args", getCommandArgsForMethod(method)).finish();
				sc.link(cbBeanConfig).to(CommandBindingExtendable.class).with(commandAnnotation.name()).finishLink();
			}
			else
			{
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

	private CommandArg[] getCommandArgsForMethod(Method method)
	{
		ArrayList<CommandArg> args = new ArrayList<CommandArg>();
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
		return args.toArray(CommandArg.class);
	}
}
