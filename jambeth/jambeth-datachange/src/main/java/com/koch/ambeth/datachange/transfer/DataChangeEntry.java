package com.koch.ambeth.datachange.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.datachange.model.IDataChangeEntry;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringBuilderUtil;

@XmlRootElement(name = "DataChangeEntry", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataChangeEntry implements IDataChangeEntry, IPrintable
{
	@XmlElement
	protected Object id;

	@XmlElement
	protected byte idNameIndex;

	@XmlElement
	protected Class<?> entityType;

	@XmlElement
	protected Object version;

	@XmlElement
	protected String[] topics;

	public DataChangeEntry()
	{
		// Intended blank
	}

	public DataChangeEntry(Class<?> entityType, byte idNameIndex, Object id, Object version)
	{
		this.entityType = entityType;
		this.idNameIndex = idNameIndex;
		this.id = id;
		this.version = version;
	}

	@Override
	public Object getId()
	{
		return id;
	}

	public void setId(Object id)
	{
		this.id = id;
	}

	@Override
	public byte getIdNameIndex()
	{
		return idNameIndex;
	}

	public void setIdNameIndex(byte idNameIndex)
	{
		this.idNameIndex = idNameIndex;
	}

	@Override
	public Class<?> getEntityType()
	{
		return entityType;
	}

	public void setEntityType(Class<?> entityType)
	{
		this.entityType = entityType;
	}

	@Override
	public Object getVersion()
	{
		return version;
	}

	public void setVersion(Object version)
	{
		this.version = version;
	}

	@Override
	public String[] getTopics()
	{
		return topics;
	}

	@Override
	public void setTopics(String[] topics)
	{
		this.topics = topics;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append("EntityType=").append(getEntityType().getName()).append(" IdIndex=").append(getIdNameIndex()).append(" Id='");
		StringBuilderUtil.appendPrintable(sb, getId());
		sb.append("' Version='").append(getVersion()).append('\'');
	}
}
