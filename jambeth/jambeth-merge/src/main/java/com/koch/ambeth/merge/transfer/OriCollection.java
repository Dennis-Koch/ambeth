package com.koch.ambeth.merge.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.service.merge.model.IObjRef;

@XmlRootElement
public class OriCollection implements IOriCollection
{
	protected transient Map<Class<?>, List<IObjRef>> typeToOriDict;

	@XmlElement(required = true)
	protected List<IObjRef> allChangeORIs;

	@XmlElement(required = false)
	protected Long changedOn;

	@XmlElement(required = false)
	protected String changedBy;

	@XmlElement(required = false)
	protected String[] allChangedBy;

	@XmlElement(required = false)
	protected Long[] allChangedOn;

	public OriCollection()
	{
	}

	public OriCollection(List<IObjRef> oriList)
	{
		allChangeORIs = oriList;
	}

	@Override
	public List<IObjRef> getAllChangeORIs()
	{
		return allChangeORIs;
	}

	public void setAllChangeORIs(List<IObjRef> allChangeORIs)
	{
		this.allChangeORIs = allChangeORIs;
	}

	@Override
	public List<IObjRef> getChangeRefs(Class<?> type)
	{
		if (typeToOriDict == null)
		{
			typeToOriDict = new HashMap<Class<?>, List<IObjRef>>();

			for (int a = allChangeORIs.size(); a-- > 0;)
			{
				IObjRef ori = allChangeORIs.get(a);
				Class<?> realType = ori.getRealType();
				List<IObjRef> modList = typeToOriDict.get(realType);
				if (modList == null)
				{
					modList = new ArrayList<IObjRef>();
					typeToOriDict.put(realType, modList);
				}
				modList.add(ori);
			}
		}

		return typeToOriDict.get(type);
	}

	@Override
	public Long getChangedOn()
	{
		return changedOn;
	}

	public void setChangedOn(Long changedOn)
	{
		this.changedOn = changedOn;
	}

	@Override
	public String getChangedBy()
	{
		return changedBy;
	}

	public void setChangedBy(String changedBy)
	{
		this.changedBy = changedBy;
	}

	@Override
	public String[] getAllChangedBy()
	{
		return allChangedBy;
	}

	public void setAllChangedBy(String[] allChangedBy)
	{
		this.allChangedBy = allChangedBy;
	}

	@Override
	public Long[] getAllChangedOn()
	{
		return allChangedOn;
	}

	public void setAllChangedOn(Long[] allChangedOn)
	{
		this.allChangedOn = allChangedOn;
	}
}
