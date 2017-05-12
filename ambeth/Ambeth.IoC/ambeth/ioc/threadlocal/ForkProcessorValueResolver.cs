using System;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public class ForkProcessorValueResolver : IForkedValueResolver
    {
	    private readonly Object originalValue;

	    private readonly IForkProcessor forkProcessor;

	    public ForkProcessorValueResolver(Object originalValue, IForkProcessor forkProcessor)
	    {
		    this.originalValue = originalValue;
		    this.forkProcessor = forkProcessor;
	    }

	    public IForkProcessor GetForkProcessor()
	    {
		    return forkProcessor;
	    }

	    public Object GetOriginalValue()
	    {
		    return originalValue;
	    }

	    public Object CreateForkedValue()
	    {
		    return forkProcessor.CreateForkedValue(originalValue);
	    }
    }
}