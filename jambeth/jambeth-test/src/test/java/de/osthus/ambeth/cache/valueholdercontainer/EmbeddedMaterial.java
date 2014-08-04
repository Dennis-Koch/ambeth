package de.osthus.ambeth.cache.valueholdercontainer;

import java.util.List;

import javax.persistence.Embeddable;

@Embeddable
public class EmbeddedMaterial
{
	private String name;

	private List<String> names;

	private MaterialType embMatType;

	private EmbeddedMaterial2 embMat2;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<String> getNames()
	{
		return names;
	}

	public void setNames(List<String> names)
	{
		this.names = names;
	}

	public MaterialType getEmbMatType()
	{
		return embMatType;
	}

	public void setEmbMatType(MaterialType embMatType)
	{
		this.embMatType = embMatType;
	}

	public EmbeddedMaterial2 getEmbMat2()
	{
		return embMat2;
	}

	public void setEmbMat2(EmbeddedMaterial2 embMat2)
	{
		this.embMat2 = embMat2;
	}
}
