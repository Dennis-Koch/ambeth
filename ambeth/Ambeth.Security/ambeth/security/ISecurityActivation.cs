using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Security
{
    public interface ISecurityActivation
    {
	    bool Secured { get;  }

        bool FilterActivated { get; }

	    R ExecuteWithoutSecurity<R>(IResultingBackgroundWorkerDelegate<R> pausedSecurityRunnable);

	    R ExecuteWithoutFiltering<R>(IResultingBackgroundWorkerDelegate<R> noFilterRunnable);
    }
}
