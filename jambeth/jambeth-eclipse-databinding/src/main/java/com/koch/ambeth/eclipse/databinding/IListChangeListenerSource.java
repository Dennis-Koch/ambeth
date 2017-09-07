package com.koch.ambeth.eclipse.databinding;

import org.eclipse.core.databinding.observable.list.IListChangeListener;

public interface IListChangeListenerSource {
	IListChangeListener<?> getListChangeListener();
}
