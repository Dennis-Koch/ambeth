package com.koch.ambeth.xml.pending;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;

public class PrefetchFutureHandler implements IObjectFutureHandler {
	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Override
	public void handle(IList<IObjectFuture> objectFutures) {
		IList<Iterable<Object>> allToPrefetch = new ArrayList<>(objectFutures.size());
		for (int i = 0, size = objectFutures.size(); i < size; i++) {
			IObjectFuture objectFuture = objectFutures.get(i);
			if (!(objectFuture instanceof PrefetchFuture)) {
				throw new IllegalArgumentException("'" + getClass().getSimpleName() + "' cannot handle "
						+ IObjectFuture.class.getSimpleName() + " implementations of type '"
						+ objectFuture.getClass().getSimpleName() + "'");
			}

			PrefetchFuture prefetchFuture = (PrefetchFuture) objectFuture;
			Iterable<Object> toPrefetch = prefetchFuture.getToPrefetch();
			allToPrefetch.add(toPrefetch);
		}

		prefetchHelper.prefetch(allToPrefetch);
	}
}
