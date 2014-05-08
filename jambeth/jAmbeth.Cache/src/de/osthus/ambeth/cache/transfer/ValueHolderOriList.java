package de.osthus.ambeth.cache.transfer;

import java.util.ArrayList;
import java.util.Collection;

import de.osthus.ambeth.merge.model.IObjRef;

// TODO [CollectionDataContract(IsReference = true, Namespace = "http://schemas.osthus.de/Ambeth")]
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
