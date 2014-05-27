package de.osthus.ambeth.xml.pending;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.ParamChecker;

public class PrefetchFutureHandler implements IObjectFutureHandler, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private IPrefetchHelper prefetchHelper;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(prefetchHelper, "PrefetchHelper");
	}

	public void setPrefetchHelper(IPrefetchHelper prefetchHelper)
	{
		this.prefetchHelper = prefetchHelper;
	}

	@Override
	public void handle(IList<IObjectFuture> objectFutures)
	{
		IList<Iterable<Object>> allToPrefetch = new ArrayList<Iterable<Object>>(objectFutures.size());
		for (int i = 0, size = objectFutures.size(); i < size; i++)
		{
			IObjectFuture objectFuture = objectFutures.get(i);
			if (!(objectFuture instanceof PrefetchFuture))
			{
				throw new IllegalArgumentException("'" + getClass().getSimpleName() + "' cannot handle " + IObjectFuture.class.getSimpleName()
						+ " implementations of type '" + objectFuture.getClass().getSimpleName() + "'");
			}

			PrefetchFuture prefetchFuture = (PrefetchFuture) objectFuture;
			Iterable<Object> toPrefetch = prefetchFuture.getToPrefetch();
			allToPrefetch.add(toPrefetch);
		}

		prefetchHelper.prefetch(allToPrefetch);
	}
}
