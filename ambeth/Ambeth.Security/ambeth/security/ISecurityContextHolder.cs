using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Security
{
    public interface ISecurityContextHolder
    {
	    ISecurityContext Context  { get; }

	    ISecurityContext GetCreateContext();

	    void ClearContext();

	    R SetScopedAuthentication<R>(IAuthentication authentication, IResultingBackgroundWorkerDelegate<R> runnableScope);
    }
}