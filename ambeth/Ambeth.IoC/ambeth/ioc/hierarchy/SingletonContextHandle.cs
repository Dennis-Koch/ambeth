using De.Osthus.Ambeth.Log;

namespace De.Osthus.Ambeth.Ioc.Hierarchy
{
    public class SingletonContextHandle : AbstractSingletonContextHandle
    {
        [LogInstance]
        public new ILogger Log { private get; set; }

	    protected IServiceContext childContext;

	    protected override IServiceContext GetChildContext()
	    {
		    return childContext;
	    }

        protected override void SetChildContext(IServiceContext childContext)
	    {
		    this.childContext = childContext;
	    }
    }
}
