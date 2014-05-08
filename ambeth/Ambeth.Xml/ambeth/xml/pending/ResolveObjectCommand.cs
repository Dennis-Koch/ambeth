using System;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public class ResolveObjectCommand : IObjectCommand, IInitializingBean
    {
        public virtual IObjectFuture ObjectFuture { get; set; }

        public virtual Object Parent
        {
            set
            {
                // NoOp
            }
        }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ObjectFuture, "ObjectFuture");
        }

        public virtual void Execute(IReader reader)
        {
            // NoOp
        }
    }
}
