using System.Threading;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Threadlocal;

namespace De.Osthus.Ambeth.Ioc.Hierarchy
{
    public class ThreadLocalContextHandle : AbstractSingletonContextHandle, IThreadLocalCleanupBean
    {
        [LogInstance]
        public new ILogger Log { private get; set; }

        [Forkable]
	    protected readonly ThreadLocal<IServiceContext> childContextTL = new ThreadLocal<IServiceContext>();

	    protected override IServiceContext GetChildContext()
	    {
		    return childContextTL.Value;
	    }

        protected override void SetChildContext(IServiceContext childContext)
	    {
		    this.childContextTL.Value = childContext;
	    }

        public void CleanupThreadLocal()
        {
            IServiceContext context = childContextTL.Value;
            if (context == null)
            {
                return;
            }
            childContextTL.Value = null;
            context.Dispose();
        }
    }
}
