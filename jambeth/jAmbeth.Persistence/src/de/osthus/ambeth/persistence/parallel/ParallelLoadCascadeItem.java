package de.osthus.ambeth.persistence.parallel;

import java.util.Collection;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.persistence.IDirectedLink;

public class ParallelLoadCascadeItem extends AbstractParallelItem
{
	public final Class<?> entityType;

	public final IDirectedLink link;

	public final ArrayList<Object> splittedIds;

	public final int relationIndex;

	public ParallelLoadCascadeItem(Class<?> entityType, IDirectedLink link, ArrayList<Object> splittedIds, int relationIndex,
			LinkedHashMap<Class<?>, Collection<Object>[]> sharedCascadeTypeToPendingInit)
	{
		super(sharedCascadeTypeToPendingInit);
		this.entityType = entityType;
		this.link = link;
		this.splittedIds = splittedIds;
		this.relationIndex = relationIndex;
	}
}
