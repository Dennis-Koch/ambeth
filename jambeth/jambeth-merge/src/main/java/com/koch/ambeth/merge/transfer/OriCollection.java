package com.koch.ambeth.merge.transfer;

/*-
 * #%L
 * jambeth-merge
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.service.merge.model.IObjRef;

@XmlRootElement
public class OriCollection implements IOriCollection {
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

	public OriCollection() {
	}

	public OriCollection(List<IObjRef> oriList) {
		allChangeORIs = oriList;
	}

	@Override
	public List<IObjRef> getAllChangeORIs() {
		return allChangeORIs;
	}

	public void setAllChangeORIs(List<IObjRef> allChangeORIs) {
		this.allChangeORIs = allChangeORIs;
	}

	@Override
	public List<IObjRef> getChangeRefs(Class<?> type) {
		if (typeToOriDict == null) {
			typeToOriDict = new HashMap<>();

			for (int a = allChangeORIs.size(); a-- > 0;) {
				IObjRef ori = allChangeORIs.get(a);
				Class<?> realType = ori.getRealType();
				List<IObjRef> modList = typeToOriDict.get(realType);
				if (modList == null) {
					modList = new ArrayList<>();
					typeToOriDict.put(realType, modList);
				}
				modList.add(ori);
			}
		}

		return typeToOriDict.get(type);
	}

	@Override
	public Long getChangedOn() {
		return changedOn;
	}

	public void setChangedOn(Long changedOn) {
		this.changedOn = changedOn;
	}

	@Override
	public String getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}

	@Override
	public String[] getAllChangedBy() {
		return allChangedBy;
	}

	public void setAllChangedBy(String[] allChangedBy) {
		this.allChangedBy = allChangedBy;
	}

	@Override
	public Long[] getAllChangedOn() {
		return allChangedOn;
	}

	public void setAllChangedOn(Long[] allChangedOn) {
		this.allChangedOn = allChangedOn;
	}
}
