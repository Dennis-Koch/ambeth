package de.osthus.ambeth.typeinfo;

import java.util.Collections;
import java.util.Comparator;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;

public class PropertyInfoEntry
{
	protected final HashMap<String, IPropertyInfo> map;

	protected final IPropertyInfo[] properties;

	public PropertyInfoEntry(HashMap<String, IPropertyInfo> map)
	{
		this.map = map;
		ArrayList<IPropertyInfo> pis = new ArrayList<IPropertyInfo>(map.toArray(IPropertyInfo.class));
		Collections.sort(pis, new Comparator<IPropertyInfo>()
		{
			@Override
			public int compare(IPropertyInfo o1, IPropertyInfo o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});
		this.properties = pis.toArray(IPropertyInfo.class);
	}
}