package com.koch.ambeth.cache.transfer;

import java.util.ArrayList;
import java.util.Collection;

import com.koch.ambeth.service.merge.model.IObjRef;

// TODO [CollectionDataContract(IsReference = true, Namespace = "http://schema.kochdev.com/Ambeth")]
public class ValueHolderOriList extends ArrayList<IObjRef>
{

	private static final long serialVersionUID = 1L;

	public ValueHolderOriList()
	{
		super();
		// Intended blank
	}

	public ValueHolderOriList(Collection<IObjRef> collection)
	{
		super(collection);
		// Intended blank
	}

	public ValueHolderOriList(int capacity)
	{
		super(capacity);
		// Intended blank
	}

}
