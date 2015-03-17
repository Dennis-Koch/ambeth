using System;
using System.Collections.Generic;
using System.Reflection;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public class ForkStateEntry
    {
        public static readonly Object[] EMPTY_ARGS = new Object[0];

        public readonly IThreadLocalCleanupBean tlBean;

        public readonly String fieldName;

        public readonly Object valueTL;

        public readonly MethodInfo getValueMI, setValueMI;

        public readonly ForkableType forkableType;

        public readonly IForkProcessor forkProcessor;

        public List<Object> forkedValues;

        public ForkStateEntry(IThreadLocalCleanupBean tlBean, String fieldName, Object valueTL, ForkableType forkableType, IForkProcessor forkProcessor)
        {
            this.tlBean = tlBean;
            this.fieldName = fieldName;
            this.valueTL = valueTL;
            this.forkableType = forkableType;
            this.forkProcessor = forkProcessor;

            getValueMI = valueTL.GetType().GetMethod("get_Value");
            setValueMI = valueTL.GetType().GetMethod("set_Value", new Type[] { getValueMI.ReturnType });
        }
    }
}