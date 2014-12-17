using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public class ForkStateEntry
    {
        public static readonly Object[] EMPTY_ARGS = new Object[0];

        public readonly IThreadLocalCleanupBean tlBean;

        public readonly Object valueTL;

        public readonly MethodInfo getValueMI, setValueMI;

        public readonly ForkableType forkableType;

        public ForkStateEntry(IThreadLocalCleanupBean tlBean, Object valueTL, ForkableType forkableType)
        {
            this.tlBean = tlBean;
            this.valueTL = valueTL;
            this.forkableType = forkableType;

            getValueMI = valueTL.GetType().GetMethod("get_Value");
            setValueMI = valueTL.GetType().GetMethod("set_Value", new Type[] { getValueMI.ReturnType });
        }
    }
}