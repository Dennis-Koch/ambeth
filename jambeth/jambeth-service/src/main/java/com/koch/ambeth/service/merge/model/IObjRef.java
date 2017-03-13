package com.koch.ambeth.service.merge.model;

import com.koch.ambeth.util.annotation.XmlType;

/**
 * Contains reference information about an Object in the cache or not loaded yet. By use of these
 * information, a specific Object is uniquely defined and can be loaded from the cache.
 *
 * @see com.koch.ambeth.merge.cache.ICache#getObject(IObjRef, java.util.Set)
 */
@XmlType
public interface IObjRef {
	byte getIdNameIndex();

	void setIdNameIndex(byte idNameIndex);

	Object getId();

	void setId(Object id);

	Object getVersion();

	void setVersion(Object version);

	Class<?> getRealType();

	void setRealType(Class<?> realType);
}
