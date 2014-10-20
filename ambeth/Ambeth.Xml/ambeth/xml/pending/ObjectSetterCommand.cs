using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Metadata;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public class ObjectSetterCommand : AbstractObjectCommand, IObjectCommand, IInitializingBean
    {
        [Property]
        public Member Member { protected get; set; }
        
        public override void Execute(IReader reader)
        {
            Object value = ObjectFuture.Value;
            Member.SetValue(Parent, value);
        }
    }
}
