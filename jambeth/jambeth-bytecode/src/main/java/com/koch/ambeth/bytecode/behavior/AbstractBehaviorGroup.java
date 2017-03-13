package com.koch.ambeth.bytecode.behavior;

import java.util.List;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.LinkedHashSet;

public abstract class AbstractBehaviorGroup implements IBytecodeBehavior, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	protected final ArrayList<IBytecodeBehavior> childBehaviors = new ArrayList<IBytecodeBehavior>();

	protected final LinkedHashSet<Class<?>> supportedEnhancements = new LinkedHashSet<Class<?>>(0.5f);

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	protected void addDefaultChildBehavior(Class<? extends IBytecodeBehavior> behaviorType)
	{
		IBytecodeBehavior behavior = beanContext.registerBean(behaviorType).finish();
		childBehaviors.add(behavior);
		supportedEnhancements.addAll(behavior.getEnhancements());
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors, List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		for (int a = 0, size = childBehaviors.size(); a < size; a++)
		{
			IBytecodeBehavior childBehavior = childBehaviors.get(a);
			visitor = childBehavior.extend(visitor, state, remainingPendingBehaviors, cascadePendingBehaviors);
		}
		return visitor;
	}

	@Override
	public Class<?>[] getEnhancements()
	{
		return supportedEnhancements.toArray(Class.class);
	}
}
