package com.koch.ambeth.datachange.model;

/**
 * Wrapper object around an {@link IDataChange}. It is dispatched via the Ambeth Event Bus and
 * subscribers to the DCE infrastructure are encouraged to subscribe to this type instead of
 * directly subscribing {@link IDataChange}. The former approach ensures that the low-level
 * technical subscribers are already processed consistently (because they do subscribe to
 * {@link IDataChange} and that specifically all applicable cache states have been updated prior to
 * notifying the first subscriber of {@link IPostDataChange}.
 */
public interface IPostDataChange {
	/**
	 * Get the wrapped data change anyone is really interested in
	 * 
	 * @return The wrapped data change originally dispatched to the Ambeth Event Bus
	 */
	IDataChange getDataChange();
}
