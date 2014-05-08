package de.osthus.ambeth.merge;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;

public interface IProxyHelper
{
	Class<?> getRealType(Class<?> type);

	boolean isInitialized(Object parentObj, String memberName);

	boolean isInitialized(Object parentObj, IRelationInfoItem member);

	void setUninitialized(Object parentObj, IRelationInfoItem member, IObjRef[] objRefs);

	IObjRef[] getObjRefs(Object parentObj, String memberName);

	IObjRef[] getObjRefs(Object parentObj, IRelationInfoItem member);

	void setObjRefs(Object parentObj, IRelationInfoItem member, IObjRef[] objRefs);

	boolean objectEquals(Object leftObject, Object rightObject);

	Object getValueDirect(Object parentObj, IRelationInfoItem member);

	void setValueDirect(Object parentObj, IRelationInfoItem member, Object value);
}