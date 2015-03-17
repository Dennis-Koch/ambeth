using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public delegate IClassVisitor WaitForApplyBehaviorDelegate(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors, IList<IBytecodeBehavior> cascadePendingBehaviors);
}