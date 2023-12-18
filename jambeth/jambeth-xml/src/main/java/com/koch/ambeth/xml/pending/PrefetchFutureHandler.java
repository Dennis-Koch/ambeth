package com.koch.ambeth.xml.pending;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.util.collections.ArrayList;

import java.util.List;

public class PrefetchFutureHandler implements IObjectFutureHandler {
    @Autowired
    protected IPrefetchHelper prefetchHelper;

    @Override
    public void handle(List<IObjectFuture> objectFutures) {
        var allToPrefetch = new ArrayList<Iterable<Object>>(objectFutures.size());
        for (int i = 0, size = objectFutures.size(); i < size; i++) {
            var objectFuture = objectFutures.get(i);
            if (!(objectFuture instanceof PrefetchFuture)) {
                throw new IllegalArgumentException(
                        "'" + getClass().getSimpleName() + "' cannot handle " + IObjectFuture.class.getSimpleName() + " implementations of type '" + objectFuture.getClass().getSimpleName() + "'");
            }

            var prefetchFuture = (PrefetchFuture) objectFuture;
            var toPrefetch = prefetchFuture.getToPrefetch();
            allToPrefetch.add(toPrefetch);
        }

        prefetchHelper.prefetch(allToPrefetch);
    }
}
