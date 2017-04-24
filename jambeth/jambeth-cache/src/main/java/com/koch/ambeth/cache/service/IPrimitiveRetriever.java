package com.koch.ambeth.cache.service;

import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.util.collections.IList;

/**
 * Extension to provide the value of a primitive (=non-relational) property of an entity. This is
 * used in cases where a previous call to the corresponding {@link ICacheRetriever} did not provide
 * the value information in the {@link com.koch.ambeth.service.cache.model.ILoadContainer} for
 * whatever reason. In most cases the primitive value has been omitted intentionally in order to
 * provide a lazy-loading pattern. So again: This way not only relations can be fetched lazily but
 * also dedicated non-relational properties of any entity.<br>
 * <br>
 * Please note that in order to achieve a true streaming-type for a primitive property you should
 * rather specify a property type inheriting from {@link com.koch.ambeth.stream.IInputSource} on the
 * entity. See the documentation there for usage details.<br>
 * <br>
 *
 * An implementation of this extension can link very easily to the provided extension point with the
 * Link-API of the IoC container: <code><br><br>
 * IBeanContextFactory bcf = ...<br>
 * IBeanConfiguration myPrimitiveRetriever = bcf.registerBean(MyPrimitiveRetriever.class);<br>
 * bcf.link(myPrimitiveRetriever).to(IPrimitiveRetrieverExtendable.class).with(MyEntity.class, "MyLazilyLoadedValue");
 * </code><br>
 */
public interface IPrimitiveRetriever {
	Object[] getPrimitives(IList<IObjRelation> objPropertyKeys, IList<ILoadContainer> loadContainers);
}
