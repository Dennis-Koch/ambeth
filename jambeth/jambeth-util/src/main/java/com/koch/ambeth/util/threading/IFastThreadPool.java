package com.koch.ambeth.util.threading;

/*-
 * #%L
 * jambeth-util
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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

public interface IFastThreadPool extends Executor {

	void queueAction(final HandlerRunnable<?, ?> handlerRunnable);

	<O> void queueAction(final O object, final HandlerRunnable<O, ?> handlerRunnable,
			final CountDownLatch latch);

	<O> void queueActions(final List<O> objects, final HandlerRunnable<O, ?> handlerRunnable,
			final CountDownLatch latch);

	<O> void queueActionsWait(final List<O> objects, final HandlerRunnable<O, ?> handlerRunnable);

	<O, C> void queueActionsWait(final List<O> objects, final C context,
			final HandlerRunnable<O, C> handlerRunnable);

	<O, C> void queueActions(final List<O> objects, final C context,
			final HandlerRunnable<O, C> handlerRunnable, final CountDownLatch latch);

	<O, C> void queueAction(final O object, final C context,
			final HandlerRunnable<O, C> handlerRunnable, final CountDownLatch latch);
}
