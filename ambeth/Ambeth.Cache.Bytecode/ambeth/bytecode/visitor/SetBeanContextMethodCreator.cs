using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class SetBeanContextMethodCreator : ClassVisitor
    {
        private static readonly String beanContextName = "$beanContext";

        public static PropertyInstance GetBeanContextPI(IClassVisitor cv)
        {
            Object bean = State.BeanContext.GetService<IServiceContext>();
            PropertyInstance pi = State.GetProperty(beanContextName, NewType.GetType(bean.GetType()));
            if (pi != null)
            {
                return pi;
            }
            return cv.ImplementAssignedReadonlyProperty(beanContextName, bean);
        }

        public SetBeanContextMethodCreator(IClassVisitor cv)
            : base(cv)
        {
            // intended blank
        }

        public override void VisitEnd()
        {
            // force implementation
            GetBeanContextPI(this);

            base.VisitEnd();
        }
    }
}