package com.koch.ambeth.cache.mixin;

import com.koch.ambeth.util.model.INotifyPropertyChangedSource;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;

public interface ICollectionChangeProcessor {
	void processCollectionChangeEvent(INotifyPropertyChangedSource obj, IPropertyInfo property,
			Object evnt, boolean isParentChildProperty);
}
