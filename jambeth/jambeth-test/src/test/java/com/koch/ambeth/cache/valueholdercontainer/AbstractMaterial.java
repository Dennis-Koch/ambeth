package com.koch.ambeth.cache.valueholdercontainer;

import java.util.List;

//[DebuggerTypeProxy(typeof(FlattenHierarchyProxy))]
public class AbstractMaterial
{
	// [DebuggerBrowsable(DebuggerBrowsableState.Never)]
	private List<MaterialType> types;

	// [DebuggerDisplay("Types1")]
	// [DebuggerBrowsable(DebuggerBrowsableState.Never)]
	public List<MaterialType> getTypes()
	{
		return types;
	}

	public void setTypes(List<MaterialType> types)
	{
		this.types = types;
	}

	protected AbstractMaterial()
	{
		// Intended blank
	}
}