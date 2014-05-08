using System;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public abstract class AbstractObjectCommand : IObjectCommand, IInitializingBean
    {
        public virtual IObjectFuture ObjectFuture { get; set; }

        public virtual Object Parent { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ObjectFuture, "ObjectFuture");
            ParamChecker.AssertNotNull(Parent, "Parent");
        }

        public abstract void Execute(IReader reader);
    }
}
