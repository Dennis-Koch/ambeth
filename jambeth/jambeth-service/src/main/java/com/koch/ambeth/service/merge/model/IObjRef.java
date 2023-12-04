package com.koch.ambeth.service.merge.model;

import com.koch.ambeth.service.metadata.IDTOType;
import com.koch.ambeth.util.annotation.XmlType;

import java.util.Set;

/**
 * Contains reference information about an Object in the cache or not loaded yet. By use of these
 * information, a specific Object is uniquely defined and can be loaded from the cache.
 *
 * @see com.koch.ambeth.merge.cache.ICache#getObject(IObjRef, Set)
 */
@XmlType
public interface IObjRef extends IDTOType {
    byte PRIMARY_KEY_INDEX = -1;

    byte UNDEFINED_KEY_INDEX = Byte.MIN_VALUE;

    IObjRef[] EMPTY_ARRAY = new IObjRef[0];

    IObjRef[][] EMPTY_ARRAY_ARRAY = new IObjRef[0][0];

    byte getIdNameIndex();

    void setIdNameIndex(byte idNameIndex);

    Object getId();

    void setId(Object id);

    Object getVersion();

    void setVersion(Object version);

    Class<?> getRealType();

    void setRealType(Class<?> realType);
}
