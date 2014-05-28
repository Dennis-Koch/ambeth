package de.osthus.ambeth.merge.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IChangeContainer;

@XmlRootElement(name = "CUDResult", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class CUDResult implements ICUDResult
{
	@XmlElement(required = true)
	protected List<IChangeContainer> allChanges;

	@XmlTransient
	protected Map<Class<?>, List<IChangeContainer>> typeToModDict;

	@XmlTransient
	protected List<Object> originalRefs;

	public CUDResult()
	{
		this.allChanges = new ArrayList<IChangeContainer>();
	}

	public CUDResult(List<IChangeContainer> allChanges, List<Object> originalRefs)
	{
		this.allChanges = new ArrayList<IChangeContainer>();
		for (int a = 0, size = allChanges.size(); a < size; a++)
		{
			this.allChanges.add(allChanges.get(a));
		}
		this.originalRefs = originalRefs;
	}

	public void setAllChanges(List<IChangeContainer> allChanges)
	{
		this.allChanges = allChanges;
	}

	@Override
	public List<IChangeContainer> getAllChanges()
	{
		return allChanges;
	}

	@Override
	public List<Object> getOriginalRefs()
	{
		return this.originalRefs;
	}

	@Override
	public List<IChangeContainer> getChanges(Class<?> type)
	{
		if (typeToModDict != null)
		{
			return this.typeToModDict.get(type);
		}
		typeToModDict = new HashMap<Class<?>, List<IChangeContainer>>();

		for (int a = this.allChanges.size(); a-- > 0;)
		{
			IChangeContainer changeContainer = this.allChanges.get(a);
			Class<?> realType = changeContainer.getReference().getRealType();
			List<IChangeContainer> modList = this.typeToModDict.get(realType);
			if (modList == null)
			{
				modList = new ArrayList<IChangeContainer>();
				typeToModDict.put(realType, modList);
			}
			modList.add(changeContainer);
		}
		return this.typeToModDict.get(type);
	}

}
