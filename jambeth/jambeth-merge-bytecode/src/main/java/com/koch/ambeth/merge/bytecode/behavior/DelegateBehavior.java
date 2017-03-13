package com.koch.ambeth.merge.bytecode.behavior;

import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.behavior.AbstractBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.DelegateEnhancementHint;
import com.koch.ambeth.ioc.link.LinkContainer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.bytecode.visitor.DelegateVisitor;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.collections.IMap;

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
