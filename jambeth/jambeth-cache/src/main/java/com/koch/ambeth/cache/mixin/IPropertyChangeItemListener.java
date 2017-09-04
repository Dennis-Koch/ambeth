package com.koch.ambeth.cache.mixin;

import com.koch.ambeth.util.model.INotifyPropertyChangedSource;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;

public interface IPropertyChangeItemListener {
	void handleAddedItem(INotifyPropertyChangedSource obj, IPropertyInfo property, Object item,
			boolean isParentChildProperty);

	void handleRemovedItem(INotifyPropertyChangedSource obj, IPropertyInfo property, Object item,
			boolean isParentChildProperty);
}
