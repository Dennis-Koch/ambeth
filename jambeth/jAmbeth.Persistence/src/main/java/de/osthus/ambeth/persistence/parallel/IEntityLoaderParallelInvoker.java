package de.osthus.ambeth.persistence.parallel;

import java.util.List;

import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;

public interface IEntityLoaderParallelInvoker
{
	<V extends AbstractParallelItem> void invokeAndWait(List<V> items, IBackgroundWorkerParamDelegate<V> run);
}