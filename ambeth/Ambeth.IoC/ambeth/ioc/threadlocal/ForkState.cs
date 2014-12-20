using De.Osthus.Ambeth.Threading;
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

        protected Object[] SetThreadLocals()
	    {
		    ForkStateEntry[] forkStateEntries = this.forkStateEntries;
		    IForkedValueResolver[] forkedValueResolvers = this.forkedValueResolvers;
		    Object[] oldValues = new Object[forkedValueResolvers.Length];
            for (int a = 0, size = forkStateEntries.Length; a < size; a++)
		    {
			    ThreadLocal<Object> tlHandle = (ThreadLocal<Object>) forkStateEntries[a].valueTL;
			    oldValues[a] = tlHandle.Value;
			    Object forkedValue = forkedValueResolvers[a].GetForkedValue();
			    tlHandle.Value = forkedValue;
		    }
		    return oldValues;
	    }

	    protected void RestoreThreadLocals(Object[] oldValues)
	    {
		    ForkStateEntry[] forkStateEntries = this.forkStateEntries;
		    for (int a = 0, size = forkStateEntries.Length; a < size; a++)
		    {
			    ThreadLocal<Object> tlHandle = (ThreadLocal<Object>) forkStateEntries[a].valueTL;
			    Object oldValue = oldValues[a];
				tlHandle.Value = oldValue;
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
    }
}