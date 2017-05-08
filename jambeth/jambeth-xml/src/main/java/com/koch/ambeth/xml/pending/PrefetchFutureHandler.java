package com.koch.ambeth.xml.pending;

/*-
 * #%L
 * jambeth-xml
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;

public class PrefetchFutureHandler implements IObjectFutureHandler, IInitializingBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private IPrefetchHelper prefetchHelper;

	@Override
	public void afterPropertiesSet() {
		ParamChecker.assertNotNull(prefetchHelper, "PrefetchHelper");
	}

	public void setPrefetchHelper(IPrefetchHelper prefetchHelper) {
		this.prefetchHelper = prefetchHelper;
	}

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
