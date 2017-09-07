package com.koch.ambeth.eclipse.databinding.bytecode;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;

import com.koch.ambeth.cache.mixin.ICollectionChangeProcessor;
import com.koch.ambeth.cache.mixin.IPropertyChangeItemListener;
import com.koch.ambeth.cache.mixin.PropertyChangeMixin;
import com.koch.ambeth.eclipse.databinding.IListChangeListenerSource;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.model.INotifyPropertyChangedSource;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;

public class EclipseBindingMixin
		implements IPropertyChangeItemListener, ICollectionChangeProcessor {
	@Autowired
	protected PropertyChangeMixin propertyChangeMixin;

	public void handleListChange(Object obj, ListChangeEvent<?> evnt) {
		propertyChangeMixin.handleCollectionChange((INotifyPropertyChangedSource) obj, evnt,
				evnt.getSource(), this);
	}

	@Override
	public void processCollectionChangeEvent(INotifyPropertyChangedSource obj, IPropertyInfo property,
			Object evnt_anon, boolean isParentChildProperty) {
		ListChangeEvent<?> evnt = (ListChangeEvent<?>) evnt_anon;
		for (ListDiffEntry<?> entry : evnt.diff.getDifferences()) {
			Object item = entry.getElement();
			if (entry.isAddition()) {
				propertyChangeMixin.handleAddedItem(obj, property, item, isParentChildProperty);
			}
			else {
				propertyChangeMixin.handleRemovedItem(obj, property, item, isParentChildProperty);
			}
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void handleAddedItem(INotifyPropertyChangedSource obj, IPropertyInfo property,
			Object item, boolean isParentChildProperty) {
		if (!(item instanceof IObservableList)) {
			// not an eclipse collection
			return;
		}
		if (!(obj instanceof IListChangeListenerSource)) {
			// not an enhanced entity
			return;
		}
		IListChangeListener listChangeListener =
				((IListChangeListenerSource) obj).getListChangeListener();
		((IObservableList) item).addListChangeListener(listChangeListener);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void handleRemovedItem(INotifyPropertyChangedSource obj, IPropertyInfo property,
			Object item, boolean isParentChildProperty) {
		if (!(item instanceof IObservableList)) {
			// not an eclipse collection
			return;
		}
		if (!(obj instanceof IListChangeListenerSource)) {
			// not an enhanced entity
			return;
		}
		IListChangeListener listChangeListener =
				((IListChangeListenerSource) obj).getListChangeListener();
		((IObservableList) item).removeListChangeListener(listChangeListener);
	}
}
