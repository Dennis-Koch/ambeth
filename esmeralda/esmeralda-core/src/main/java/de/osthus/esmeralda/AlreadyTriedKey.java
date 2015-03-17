package de.osthus.esmeralda;

import com.sun.source.util.TreePath;

public class AlreadyTriedKey
{
	private final TreePath tree;

	private final String typeName;

	public AlreadyTriedKey(TreePath tree, String typeName)
	{
		super();
		this.tree = tree;
		this.typeName = typeName;
	}

	@Override
	public int hashCode()
	{
		return tree.hashCode() ^ typeName.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof AlreadyTriedKey))
		{
			return false;
		}
		AlreadyTriedKey other = (AlreadyTriedKey) obj;
		return tree == other.tree && typeName.equals(other.typeName);
	}

}
