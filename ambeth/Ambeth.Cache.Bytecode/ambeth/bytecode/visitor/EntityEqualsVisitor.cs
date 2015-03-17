using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Mixin;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class EntityEqualsVisitor : ClassVisitor
    {
        public static readonly Type templateType = typeof(EntityEqualsMixin);

        public static readonly String templatePropertyName = "__" + templateType.Name;

        private static readonly MethodInstance entityEquals_Equals = new MethodInstance(null, templateType,
                typeof(bool), "Equals", typeof(IEntityEquals), typeof(Object));

        private static readonly MethodInstance entityEquals_HashCode = new MethodInstance(null, templateType,
                typeof(int), "GetHashCode", typeof(IEntityEquals));

        private static readonly MethodInstance entityEquals_toString_Obj = new MethodInstance(null, templateType,
                typeof(String), "ToString", typeof(IEntityEquals), typeof(IPrintable));

        private static readonly MethodInstance entityEquals_toString_Printable = new MethodInstance(null,
                templateType, typeof(void), "ToString", typeof(IEntityEquals), typeof(StringBuilder));

        public static PropertyInstance GetEntityEqualsTemplateProperty(IClassVisitor cv)
        {
            Object bean = State.BeanContext.GetService(templateType);
            PropertyInstance p_embeddedTypeTemplate = PropertyInstance.FindByTemplate(templatePropertyName, NewType.GetType(bean.GetType()), true);
            if (p_embeddedTypeTemplate != null)
            {
                return p_embeddedTypeTemplate;
            }
            return cv.ImplementAssignedReadonlyProperty(templatePropertyName, bean);
        }

        public EntityEqualsVisitor(IClassVisitor cv)
            : base(cv)
        {
            // Intended blank
        }

        public override void VisitEnd()
        {
            ImplementIEntityEqualsCode();
            base.VisitEnd();
        }

        protected void ImplementIEntityEqualsCode()
        {
            PropertyInstance p_entityEqualsTemplate = GetEntityEqualsTemplateProperty(this);
            ImplementEqualsMethod(p_entityEqualsTemplate);
            ImplementHashCodeMethod(p_entityEqualsTemplate);
            ImplementToStringMethod(p_entityEqualsTemplate);
        }

        protected void ImplementEqualsMethod(PropertyInstance p_entityEqualsTemplate)
        {
            MethodInstance methodTemplate = new MethodInstance(null, typeof(Object), typeof(bool), "Equals", typeof(Object));
            MethodInstance method = MethodInstance.FindByTemplate(methodTemplate, true);
            if (NewType.GetType(typeof(Object)).Equals(method.Owner) || method.Access.HasFlag(MethodAttributes.Abstract))
            {
                IMethodVisitor mg = VisitMethod(methodTemplate);
                mg.CallThisGetter(p_entityEqualsTemplate);
                mg.LoadThis();
                mg.LoadArgs();
                mg.InvokeVirtual(entityEquals_Equals);
                mg.ReturnValue();
                mg.EndMethod();
            }
        }

        protected void ImplementHashCodeMethod(PropertyInstance p_entityEqualsTemplate)
        {
            MethodInstance methodTemplate = new MethodInstance(null, typeof(Object), typeof(int), "GetHashCode");
            MethodInstance method = MethodInstance.FindByTemplate(methodTemplate, true);
            if (NewType.GetType(typeof(Object)).Equals(method.Owner) || method.Access.HasFlag(MethodAttributes.Abstract))
            {
                IMethodVisitor mg = VisitMethod(methodTemplate);
                mg.CallThisGetter(p_entityEqualsTemplate);
                mg.LoadThis();
                mg.LoadArgs();
                mg.InvokeVirtual(entityEquals_HashCode);
                mg.ReturnValue();
                mg.EndMethod();
            }
        }

        protected void ImplementToStringMethod(PropertyInstance p_entityEqualsTemplate)
        {
            {
                MethodInstance methodTemplate = new MethodInstance(null, typeof(Object), typeof(String), "ToString");
                MethodInstance method = MethodInstance.FindByTemplate(methodTemplate, true);
                if (NewType.GetType(typeof(Object)).Equals(method.Owner) || method.Access.HasFlag(MethodAttributes.Abstract))
                {
                    IMethodVisitor mg = VisitMethod(methodTemplate);
                    mg.CallThisGetter(p_entityEqualsTemplate);
                    mg.LoadThis();
                    mg.LoadThis();
                    mg.InvokeVirtual(entityEquals_toString_Obj);
                    mg.ReturnValue();
                    mg.EndMethod();
                }
            }

            {
                MethodInstance methodTemplate = new MethodInstance(null, typeof(IPrintable), typeof(void), "ToString", typeof(StringBuilder));
                MethodInstance method = MethodInstance.FindByTemplate(methodTemplate, true);
                if (method == null || method.Access.HasFlag(MethodAttributes.Abstract))
                {
                    IMethodVisitor mg = VisitMethod(methodTemplate);
                    mg.CallThisGetter(p_entityEqualsTemplate);
                    mg.LoadThis();
                    mg.LoadArgs();
                    mg.InvokeVirtual(entityEquals_toString_Printable);
                    mg.ReturnValue();
                    mg.EndMethod();
                }
            }
        }
    }
}