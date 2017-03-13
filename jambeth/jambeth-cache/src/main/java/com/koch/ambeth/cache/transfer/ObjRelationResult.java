package com.koch.ambeth.cache.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.model.IObjRef;

@XmlRootElement(name = "ObjRelationResult", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class ObjRelationResult implements IObjRelationResult
{
	@XmlElement(required = true)
	protected IObjRelation reference;

	@XmlElement(required = true)
	protected IObjRef[] relations;

	@Override
	public IObjRelation getReference()
	{
		return reference;
	}

	public void setReference(IObjRelation reference)
	{
		this.reference = reference;
	}

	@Override
	public IObjRef[] getRelations()
	{
		return relations;
	}

	public void setRelations(IObjRef[] relations)
	{
		this.relations = relations;
	}
}
