package de.osthus.ambeth.bytecode.behavior;

import java.lang.reflect.Method;
import java.util.List;

import de.osthus.ambeth.bytecode.visitor.DelegateVisitor;
import de.osthus.ambeth.bytecode.visitor.InterfaceAdder;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.bytecode.DelegateEnhancementHint;
import de.osthus.ambeth.ioc.link.LinkContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public class DelegateBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		DelegateEnhancementHint hint = state.getContext(DelegateEnhancementHint.class);
		if (hint == null)
		{
			return visitor;
		}
		Class<?> targetType = hint.getType();
		String methodName = hint.getMethodName();
		Class<?> parameterType = hint.getParameterType();

		IMap<Method, Method> delegateMethodMap = LinkContainer.buildDelegateMethodMap(targetType, methodName, parameterType);

		if (parameterType.isInterface())
		{
			visitor = new InterfaceAdder(visitor, parameterType);
		}
		visitor = new DelegateVisitor(visitor, targetType, delegateMethodMap);
		return visitor;
	}
}
