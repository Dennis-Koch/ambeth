using De.Osthus.Ambeth.Util;
using System;
using System.Threading;
namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public class ForkState : IForkState
    {
        protected readonly ForkStateEntry[] forkStateEntries;

        protected readonly IForkedValueResolver[] forkedValueResolvers;

        public ForkState(ForkStateEntry[] forkStateEntries, IForkedValueResolver[] forkedValueResolvers)
        {
            this.forkStateEntries = forkStateEntries;
            this.forkedValueResolvers = forkedValueResolvers;
        }

        public void Use(Runnable runnable)
        {
            ForkStateEntry[] forkStateEntries = this.forkStateEntries;
            IForkedValueResolver[] forkedValueResolvers = this.forkedValueResolvers;

            Object[][] oldValues = new Object[forkedValueResolvers.Length][];
            for (int a = 0, size = forkStateEntries.Length; a < size; a++)
            {
                ForkStateEntry forkStateEntry = forkStateEntries[a];
                Object tlHandle = forkStateEntry.valueTL;
                Object oldValue = forkStateEntry.getValueMI.Invoke(tlHandle, ForkStateEntry.EMPTY_ARGS);
                Object forkedValue = forkedValueResolvers[a].GetForkedValue();
                Object[] args = new Object[] { forkedValue };
                forkStateEntry.setValueMI.Invoke(tlHandle, args);
                args[0] = oldValue;
                oldValues[a] = args;
            }
            try
            {
                runnable.Run();
            }
            finally
            {
                for (int a = 0, size = forkStateEntries.Length; a < size; a++)
                {
                    ForkStateEntry forkStateEntry = forkStateEntries[a];
                    Object tlHandle = forkStateEntry.valueTL;
                    forkStateEntry.setValueMI.Invoke(tlHandle, oldValues[a]);
                }
            }
        }
    }
}