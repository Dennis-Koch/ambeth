package com.koch.ambeth.cache.mixin;

public interface IPropertyChangeItemListenerExtendable {
	void registerIPropertyChangeItemListener(IPropertyChangeItemListener propertyChangeItemListener);

	void unregisterIPropertyChangeItemListener(
			IPropertyChangeItemListener propertyChangeItemListener);
}
