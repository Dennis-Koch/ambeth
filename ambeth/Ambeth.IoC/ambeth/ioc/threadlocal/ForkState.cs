using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
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

        protected Object[] SetThreadLocals()
	    {
		    ForkStateEntry[] forkStateEntries = this.forkStateEntries;
		    IForkedValueResolver[] forkedValueResolvers = this.forkedValueResolvers;
		    Object[] oldValues = new Object[forkedValueResolvers.Length];
            for (int a = 0, size = forkStateEntries.Length; a < size; a++)
		    {
			    ThreadLocal<Object> tlHandle = (ThreadLocal<Object>) forkStateEntries[a].valueTL;
			    oldValues[a] = tlHandle.Value;
			    Object forkedValue = forkedValueResolvers[a].CreateForkedValue();
			    tlHandle.Value = forkedValue;
		    }
		    return oldValues;
	    }

	    protected void RestoreThreadLocals(Object[] oldValues)
	    {
		    ForkStateEntry[] forkStateEntries = this.forkStateEntries;
		    for (int a = 0, size = forkStateEntries.Length; a < size; a++)
		    {
                ForkStateEntry forkStateEntry = forkStateEntries[a];
                ThreadLocal<Object> tlHandle = (ThreadLocal<Object>)forkStateEntry.valueTL;
			    Object forkedValue = tlHandle.Value;
			    Object oldValue = oldValues[a];
			    tlHandle.Value = oldValue;
			    IForkedValueResolver forkedValueResolver = forkedValueResolvers[a];
			    if (!(forkedValueResolver is ForkProcessorValueResolver))
			    {
				    continue;
			    }
			    lock (forkStateEntry)
			    {
				    List<Object> forkedValues = forkStateEntry.forkedValues;
				    if (forkedValues == null)
				    {
					    forkedValues = new List<Object>();
					    forkStateEntry.forkedValues = forkedValues;
				    }
				    forkedValues.Add(forkedValue);
			    }
		    }
	    }

        public void Use(Runnable runnable)
        {
            Object[] oldValues = SetThreadLocals();
            try
            {
                runnable.Run();
            }
            finally
            {
                RestoreThreadLocals(oldValues);
            }
        }

	    public void Use(IBackgroundWorkerDelegate runnable)
	    {
		    Object[] oldValues = SetThreadLocals();
		    try
		    {
			    runnable();
		    }
		    finally
		    {
			    RestoreThreadLocals(oldValues);
		    }
	    }

	    public void Use<V>(IBackgroundWorkerParamDelegate<V> runnable, V arg)
	    {
		    Object[] oldValues = SetThreadLocals();
		    try
		    {
			    runnable(arg);
		    }
		    finally
		    {
			    RestoreThreadLocals(oldValues);
		    }
	    }

	    public R Use<R>(IResultingBackgroundWorkerDelegate<R> runnable)
	    {
		    Object[] oldValues = SetThreadLocals();
		    try
		    {
			    return runnable();
		    }
		    finally
		    {
			    RestoreThreadLocals(oldValues);
		    }
	    }

        public R Use<R, V>(IResultingBackgroundWorkerParamDelegate<R, V> runnable, V arg)
	    {
		    Object[] oldValues = SetThreadLocals();
		    try
		    {
			    return runnable(arg);
		    }
		    finally
		    {
			    RestoreThreadLocals(oldValues);
		    }
	    }

        public void ReintegrateForkedValues()
        {
            ForkStateEntry[] forkStateEntries = this.forkStateEntries;
            IForkedValueResolver[] forkedValueResolvers = this.forkedValueResolvers;
            for (int a = 0, size = forkStateEntries.Length; a < size; a++)
            {
                ForkStateEntry forkStateEntry = forkStateEntries[a];
                List<Object> forkedValues = forkStateEntry.forkedValues;

                if (forkedValues == null)
                {
                    // nothing to do
                    continue;
                }
                Object originalValue = forkedValueResolvers[a].GetOriginalValue();
                for (int b = 0, sizeB = forkedValues.Count; b < sizeB; b++)
                {
                    Object forkedValue = forkedValues[b];
                    forkStateEntry.forkProcessor.ReturnForkedValue(originalValue, forkedValue);
                }
            }
        }
    }
}