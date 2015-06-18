using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Security
{
    public interface ISecurityActivation
    {
	    bool Secured { get;  }

		bool ServiceSecurityEnabled { get; }

        bool FilterActivated { get; }

		void ExecuteWithSecurityDirective(SecurityDirective securityDirective, IBackgroundWorkerDelegate runnable);

		R ExecuteWithSecurityDirective<R>(SecurityDirective securityDirective, IResultingBackgroundWorkerDelegate<R> runnable);
		
	    R ExecuteWithoutSecurity<R>(IResultingBackgroundWorkerDelegate<R> pausedSecurityRunnable);

	    R ExecuteWithoutFiltering<R>(IResultingBackgroundWorkerDelegate<R> noFilterRunnable);

		R ExecuteWithFiltering<R>(IResultingBackgroundWorkerDelegate<R> filterRunnable);
    }
}
