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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.service.merge.model.IObjRef;

@XmlRootElement(name = "DirectObjRef", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectObjRef extends ObjRef implements IDirectObjRef {
	protected transient Object direct;

	@XmlElement(required = true)
	protected int createContainerIndex = -1;

	public DirectObjRef() {
		// Intended blank
	}

	public DirectObjRef(Class<?> realType, Object direct) {
		this.realType = realType;
		this.direct = direct;
	}

	@Override
	public Object getDirect() {
		return direct;
	}

	@Override
	public void setDirect(Object direct) {
		this.direct = direct;
	}

	@Override
	public void setId(Object id) {
		super.setId(id);
	}

	@Override
	public int getCreateContainerIndex() {
		return createContainerIndex;
	}

	@Override
	public void setCreateContainerIndex(int createContainerIndex) {
		this.createContainerIndex = createContainerIndex;
	}

	@Override
	public boolean equals(IObjRef obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (direct != null) {
			if (!(obj instanceof IDirectObjRef)) {
				return false;
			}
			// Identity - not equals - intentionally here!
			return direct == ((IDirectObjRef) obj).getDirect();
		}
		if (id == null) {
			if (!(obj instanceof IDirectObjRef)) {
				return false;
			}
			return getCreateContainerIndex() == ((IDirectObjRef) obj).getCreateContainerIndex();
		}
		return id.equals(obj.getId()) && realType.equals(obj.getRealType());
	}

	@Override
	public int hashCode() {
		if (direct != null) {
			return direct.hashCode();
		}
		if (id == null) {
			return getCreateContainerIndex();
		}
		return super.hashCode();
	}

	@Override
	public String toString() {
		if (direct != null) {
			return "ObjRef (new) type=" + getRealType().getName();
		}
		return super.toString();
	}
}
