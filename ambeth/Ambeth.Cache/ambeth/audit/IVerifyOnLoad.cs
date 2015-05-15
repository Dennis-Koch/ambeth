using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Threading;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Audit
{
	public interface IVerifyOnLoad
	{
		R VerifyEntitiesOnLoad<R>(IResultingBackgroundWorkerDelegate<R> runnable);

		void QueueVerifyEntitiesOnLoad(IList<ILoadContainer> loadedEntities);
	}
}