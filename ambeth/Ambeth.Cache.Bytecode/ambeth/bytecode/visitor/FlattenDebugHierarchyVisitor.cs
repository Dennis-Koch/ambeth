using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Debug;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Proxy;
using System;
using System.Diagnostics;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class FlattenDebugHierarchyVisitor : ClassVisitor
    {
        protected readonly bool hasValueHolders;

        public FlattenDebugHierarchyVisitor(IClassVisitor cv, bool hasValueHolders)
            : base(cv)
        {
            this.hasValueHolders = hasValueHolders;
        }

        public override void VisitEnd()
        {            
            ConstructorInfo ci = typeof(DebuggerTypeProxyAttribute).GetConstructor(new Type[] { typeof(Type) });
            if (hasValueHolders)
            {
                VisitAnnotation(ci, typeof(ValueHolderFlattenHierarchyProxy));
            }
            else
            {
                VisitAnnotation(ci, typeof(FlattenHierarchyProxy));
            }
            base.VisitEnd();
        }
    }
}