using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public class ObjectSetterCommand : AbstractObjectCommand, IObjectCommand, IInitializingBean
    {
        public virtual ITypeInfoItem Member { protected get; set; }

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();

            ParamChecker.AssertNotNull(Member, "Member");
        }

        public override void Execute(IReader reader)
        {
            Object value = ObjectFuture.Value;
            Member.SetValue(Parent, value);
        }
    }
}
