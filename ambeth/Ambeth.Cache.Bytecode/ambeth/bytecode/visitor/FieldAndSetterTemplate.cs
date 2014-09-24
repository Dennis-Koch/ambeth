using De.Osthus.Ambeth.Bytecode.Behavior;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class FieldAndSetterTemplate
    {
        private readonly String fieldName;
        private readonly MethodInfo setterMethod;
        private readonly FieldAttributes fieldAccess;

        public FieldAndSetterTemplate(FieldAttributes fieldAccess, String fieldName, MethodInfo setterMethod)
        {
            this.fieldAccess = fieldAccess;
            this.fieldName = fieldName;
            this.setterMethod = setterMethod;
        }

        public FieldInstance GetField(IClassVisitor cg)
        {
            FieldInstance f_beanContext = BytecodeBehaviorState.State.GetAlreadyImplementedField(fieldName);
            if (f_beanContext == null)
            {
                f_beanContext = ImplementSetter(cg);
            }
            return f_beanContext;
        }

        public MethodInstance GetSetter(IClassVisitor cg)
        {
            MethodInstance setter = MethodInstance.FindByTemplate(setterMethod, true);
            if (setter == null)
            {
                ImplementSetter(cg);
            }
            return MethodInstance.FindByTemplate(setterMethod, false);
        }

        protected FieldInstance ImplementSetter(IClassVisitor cg)
        {
            FieldInstance f_beanContext = new FieldInstance(fieldAccess, fieldName, setterMethod.GetParameters()[0].ParameterType);
            f_beanContext = cg.ImplementField(f_beanContext);
            cg.ImplementSetter(new MethodInstance(setterMethod), f_beanContext);

            return f_beanContext;
        }
    }
}