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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IChangeContainer;

@XmlRootElement(name = "CUDResult", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class CUDResult implements ICUDResult {
	@XmlElement(required = true)
	protected List<IChangeContainer> allChanges;

	@XmlTransient
	protected Map<Class<?>, List<IChangeContainer>> typeToModDict;

	@XmlTransient
	protected List<Object> originalRefs;

	public CUDResult() {
		allChanges = new ArrayList<>();
	}

	public CUDResult(List<IChangeContainer> allChanges, List<Object> originalRefs) {
		this.allChanges = new ArrayList<>();
		for (int a = 0, size = allChanges.size(); a < size; a++) {
			this.allChanges.add(allChanges.get(a));
		}
		this.originalRefs = originalRefs;
	}

	public void setAllChanges(List<IChangeContainer> allChanges) {
		this.allChanges = allChanges;
	}

	@Override
	public List<IChangeContainer> getAllChanges() {
		return allChanges;
	}

	@Override
	public List<Object> getOriginalRefs() {
		return originalRefs;
	}

	@Override
	public List<IChangeContainer> getChanges(Class<?> type) {
		if (typeToModDict != null) {
			return typeToModDict.get(type);
		}
		typeToModDict = new HashMap<>();

		for (int a = allChanges.size(); a-- > 0;) {
			IChangeContainer changeContainer = allChanges.get(a);
			Class<?> realType = changeContainer.getReference().getRealType();
			List<IChangeContainer> modList = typeToModDict.get(realType);
			if (modList == null) {
				modList = new ArrayList<>();
				typeToModDict.put(realType, modList);
			}
			modList.add(changeContainer);
		}
		return typeToModDict.get(type);
	}

}
