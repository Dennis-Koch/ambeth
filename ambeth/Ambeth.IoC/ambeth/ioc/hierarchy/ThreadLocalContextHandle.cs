using System.Threading;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Ioc.Hierarchy
{
    public class ThreadLocalContextHandle : AbstractSingletonContextHandle
    {
        [LogInstance]
        public new ILogger Log { private get; set; }

	    protected readonly ThreadLocal<IServiceContext> childContextTL = new ThreadLocal<IServiceContext>();

	    protected override IServiceContext GetChildContext()
	    {
		    return childContextTL.Value;
	    }

        protected override void SetChildContext(IServiceContext childContext)
	    {
		    this.childContextTL.Value = childContext;
	    }
    }
}
