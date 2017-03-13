package com.koch.ambeth.xml.pending;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;

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
