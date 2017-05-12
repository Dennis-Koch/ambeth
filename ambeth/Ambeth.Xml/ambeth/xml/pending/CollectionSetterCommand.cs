using System;
using System.Collections.Generic;
using System.Reflection;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public class CollectionSetterCommand : AbstractObjectCommand, IObjectCommand, IInitializingBean
    {
        public virtual Object Obj { protected get; set; }

        public virtual MethodInfo AddMethod { protected get; set; }

        public override void AfterPropertiesSet()
        {
            // Intentionally not calling base

            ParamChecker.AssertTrue(ObjectFuture != null || Obj != null, "Either ObjectFuture or Obj have to be set");
            ParamChecker.AssertNotNull(Parent, "Parent");
            ParamChecker.AssertNotNull(AddMethod, "AddMethod");
        }

        public override void Execute(IReader reader)
        {
            Object value = ObjectFuture != null ? ObjectFuture.Value : Obj;

            Object[] parameters = new Object[1];
            parameters[0] = value;
            AddMethod.Invoke(Parent, parameters);
        }
    }
}
