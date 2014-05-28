package de.osthus.ambeth.cache.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;

@XmlRootElement(name = "LoadContainer", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class LoadContainer implements ILoadContainer
{
	@XmlElement(required = true)
	protected IObjRef reference;

	@XmlElement(required = true)
	protected Object[] primitives;

	@XmlElement(required = true)
	protected IObjRef[][] relations;

	@XmlTransient
	protected IList<IObjRef>[] relationBuilds;

	@Override
	public IObjRef getReference()
	{
		return reference;
	}

	public void setReference(IObjRef reference)
	{
		this.reference = reference;
	}

	@Override
	public Object[] getPrimitives()
	{
		return primitives;
	}

	@Override
	public void setPrimitives(Object[] primitives)
	{
		this.primitives = primitives;
	}

	@Override
	public IObjRef[][] getRelations()
	{
		return this.relations;
	}

	public void setRelations(IObjRef[][] relations)
	{
		this.relations = relations;
	}

	public IList<IObjRef>[] getRelationBuilds()
	{
		return relationBuilds;
	}

	public void setRelationBuilds(IList<IObjRef>[] relationBuilds)
	{
		this.relationBuilds = relationBuilds;
	}
}
