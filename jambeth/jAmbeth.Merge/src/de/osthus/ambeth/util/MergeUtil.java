package de.osthus.ambeth.util;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.PrimitiveUpdateItem;
import de.osthus.ambeth.merge.transfer.RelationUpdateItem;

public final class MergeUtil
{
	public static PrimitiveUpdateItem createPrimitiveUpdateItem(String propertyName, Object newValue)
	{
		PrimitiveUpdateItem primitive = new PrimitiveUpdateItem();
		primitive.setMemberName(propertyName);
		primitive.setNewValue(newValue);
		return primitive;
	}

	public static RelationUpdateItem createRelationUpdateItem(String propertyName, IList<IObjRef> addedORIs, IList<IObjRef> removedORIs)
	{
		IObjRef[] addedORIsArray = (addedORIs != null && !addedORIs.isEmpty()) ? addedORIs.toArray(IObjRef.class) : null;
		IObjRef[] removedORIsArray = (removedORIs != null && !removedORIs.isEmpty()) ? removedORIs.toArray(IObjRef.class) : null;
		return createRelationUpdateItem(propertyName, addedORIsArray, removedORIsArray);
	}

	public static RelationUpdateItem createRelationUpdateItem(String propertyName, IObjRef[] addedORIs, IObjRef[] removedORIs)
	{
		RelationUpdateItem relation = new RelationUpdateItem();
		relation.setMemberName(propertyName);
		if (addedORIs != null)
		{
			relation.setAddedORIs(addedORIs);
		}
		if (removedORIs != null)
		{
			relation.setRemovedORIs(removedORIs);
		}

		return relation;
	}

	private MergeUtil()
	{
		// Intended blank
	}
}
