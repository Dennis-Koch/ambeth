package de.osthus.ambeth.xml.pending;

import java.util.Collection;

import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.merge.transfer.UpdateContainer;
import de.osthus.ambeth.util.ParamChecker;

public class CommandBuilder implements ICommandBuilder, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IServiceContext beanContext;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(beanContext, "BeanContext");
	}

	public void setBeanContext(IServiceContext beanContext)
	{
		this.beanContext = beanContext;
	}

	@Override
	public IObjectCommand build(ICommandTypeRegistry commandTypeRegistry, IObjectFuture objectFuture, Object parent, Object... optionals)
	{
		IObjectCommand command;
		if (parent == null)
		{
			command = buildIntern(commandTypeRegistry, ResolveObjectCommand.class, objectFuture, null).finish();
		}
		else if (parent.getClass().isArray())
		{
			command = buildIntern(commandTypeRegistry, ArraySetterCommand.class, objectFuture, parent).propertyValue("Index", optionals[0]).finish();
		}
		else if (parent instanceof Collection)
		{
			IBeanRuntime<? extends CollectionSetterCommand> beanRuntime = buildIntern(commandTypeRegistry, CollectionSetterCommand.class, objectFuture, parent);
			if (optionals.length == 1)
			{
				beanRuntime.propertyValue("Object", optionals[0]);
			}
			command = beanRuntime.finish();
		}
		else if (parent instanceof CreateContainer || parent instanceof UpdateContainer)
		{
			command = buildIntern(commandTypeRegistry, MergeCommand.class, objectFuture, parent).finish();
		}
		else
		{
			command = buildIntern(commandTypeRegistry, ObjectSetterCommand.class, objectFuture, parent).propertyValue("Member", optionals[0]).finish();
		}

		return command;
	}

	protected <C extends IObjectCommand> IBeanRuntime<? extends C> buildIntern(ICommandTypeRegistry commandTypeRegistry, Class<? extends C> commandType,
			IObjectFuture objectFuture, Object parent)
	{
		Class<? extends C> overridingCommandType = commandTypeRegistry.getOverridingCommandType(commandType);
		if (overridingCommandType != null)
		{
			commandType = overridingCommandType;
		}
		IBeanRuntime<? extends C> beanRuntime = beanContext.registerAnonymousBean(commandType).propertyValue("ObjectFuture", objectFuture)
				.propertyValue("Parent", parent);
		return beanRuntime;
	}
}
