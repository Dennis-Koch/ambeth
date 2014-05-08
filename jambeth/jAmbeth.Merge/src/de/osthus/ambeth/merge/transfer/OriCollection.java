package de.osthus.ambeth.merge.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IOriCollection;

@XmlRootElement
public class OriCollection implements IOriCollection
{
	protected transient Map<Class<?>, List<IObjRef>> typeToOriDict;

	@XmlElement(required = true)
	protected List<IObjRef> allChangeORIs;

	@XmlElement(required = true)
	protected Long changedOn;

	@XmlElement(required = true)
	protected String changedBy;

	public OriCollection()
	{
	}

	public OriCollection(List<IObjRef> oriList)
	{
		this.allChangeORIs = oriList;
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
		if (this.typeToOriDict == null)
		{
			this.typeToOriDict = new HashMap<Class<?>, List<IObjRef>>();

			for (int a = this.allChangeORIs.size(); a-- > 0;)
			{
				IObjRef ori = this.allChangeORIs.get(a);
				Class<?> realType = ori.getRealType();
				List<IObjRef> modList = this.typeToOriDict.get(realType);
				if (modList == null)
				{
					modList = new ArrayList<IObjRef>();
					this.typeToOriDict.put(realType, modList);
				}
				modList.add(ori);
			}
		}

		return this.typeToOriDict.get(type);
	}

	@Override
	public Long getChangedOn()
	{
		return this.changedOn;
	}

	public void setChangedOn(Long changedOn)
	{
		this.changedOn = changedOn;
	}

	@Override
	public String getChangedBy()
	{
		return this.changedBy;
	}

	public void setChangedBy(String changedBy)
	{
		this.changedBy = changedBy;
	}
}
