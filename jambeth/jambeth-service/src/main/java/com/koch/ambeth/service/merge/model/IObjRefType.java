package com.koch.ambeth.service.merge.model;

import java.util.List;

/**
 * Allows to extract an {@link IObjRef} handle by directly casting a given entity instance to this
 * interface
 */
public interface IObjRefType {
    /**
     * Creates an {@link IObjRef} handle pointing to this entity via its primary identifier. If the
     * current entity is not yet persisted in its data repository the returned instance is an
     * {@link com.koch.ambeth.merge.model.IDirectObjRef}.
     *
     * @return Handle pointing to this entity via the primary identifier
     */
    IObjRef getObjRef();

    /**
     * Creates an {@link IObjRef} handle pointing to this entity via its provided identifying member
     * name: That is either the primary identifier or any additional unique identifier.
     *
     * @param identifierMemberName The member name describing the expected identifier to be created
     *                             from
     * @return Handle pointing to this entity via the specified identifier
     */
    IObjRef getObjRef(String identifierMemberName);

    /**
     * Creates a list of all valid (=non-null) identifiers pointing to this entity. If the entity does
     * not have any non-null unique identifiers and is also not yet persisted the returned collection
     * is empty.
     *
     * @return The list of all valid handles each based on a different identifier pointing to this
     * entity
     */
    List<IObjRef> getAllObjRefs();
}
