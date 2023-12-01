package com.koch.ambeth.merge.proxy;

import com.koch.ambeth.util.typeinfo.ITypeInfoItem;

/**
 * Marker interface for bytecode generated objects that are composite ids: Primary or Alternate Identifiers that are composed by more than one attribute of an entity
 */
public interface ICompositeId {
    ITypeInfoItem[] getIdMembers();
}
