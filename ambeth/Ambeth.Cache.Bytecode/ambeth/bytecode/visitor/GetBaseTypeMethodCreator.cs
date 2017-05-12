using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Proxy;
using System;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class GetBaseTypeMethodCreator : ClassVisitor
    {
        private static readonly MethodInstance template_m_getBaseType = new MethodInstance(null, typeof(IEnhancedType), typeof(Type), "Get__BaseType");

        public static MethodInstance GetGetBaseType()
        {
            return MethodInstance.FindByTemplate(template_m_getBaseType, false);
        }

        public GetBaseTypeMethodCreator(IClassVisitor cv) : base(cv)
        {
            // Intended blank
        }

        public override void VisitEnd()
        {
            ImplementGetBaseType();
            base.VisitEnd();
        }

        protected void ImplementGetBaseType()
        {
            MethodInstance getBaseType = MethodInstance.FindByTemplate(template_m_getBaseType, true);
            if (getBaseType != null)
            {
                return;
            }
            IBytecodeBehaviorState state = State;
            IMethodVisitor mg = VisitMethod(template_m_getBaseType);
            mg.Push(state.OriginalType);
            mg.ReturnValue();
            mg.EndMethod();
        }
    }
}