using System;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Xml.Pending;

namespace De.Osthus.Ambeth.Xml.PostProcess
{
    public class MergeArraySetterCommand : ArraySetterCommand, IObjectCommand, IInitializingBean
    {
        public override void Execute(IReader reader)
        {
            Object value = ObjectFuture.Value;
            if (typeof(IObjRef).IsAssignableFrom(Parent.GetType().GetElementType()))
            {
                // Happens in CUDResults in PostProcessing tags (<pp>)
                value = new DirectObjRef(value.GetType(), value);
            }
            ((Array)Parent).SetValue(value, Index);
        }
    }
}
