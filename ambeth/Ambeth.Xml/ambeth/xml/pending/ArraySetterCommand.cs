using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public class ArraySetterCommand : AbstractObjectCommand, IObjectCommand, IInitializingBean
    {
        public int Index { protected get; set; }

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();

            ParamChecker.AssertTrue(Parent.GetType().IsArray, "Parent has to be an array");
            //ParamChecker.AssertNotNull(index, "Index");
        }

        public override void Execute(IReader reader)
        {
            Object value = ObjectFuture.Value;
            ((Array)Parent).SetValue(value, Index);
        }
    }
}
