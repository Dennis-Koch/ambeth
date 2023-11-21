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

import java.util.Comparator;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringBuilderUtil;

@XmlRootElement(name = "ObjRef", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class ObjRef implements IObjRef, IPrintable {
	public static final Comparator<IObjRef> comparator = new Comparator<IObjRef>() {
		@Override
		public int compare(IObjRef o1, IObjRef o2) {
			int result = o1.getRealType().getName().compareTo(o2.getRealType().getName());
			if (result != 0) {
				return result;
			}
			result = o1.getIdNameIndex() == o2.getIdNameIndex() ? 0
					: o1.getIdNameIndex() > o2.getIdNameIndex() ? 1 : -1;
			if (result != 0) {
				return result;
			}
			return o1.getId().toString().compareTo(o2.getId().toString());
		}
	};

	@XmlElement(required = true)
	protected byte idNameIndex = IObjRef.PRIMARY_KEY_INDEX;

	@XmlElement(required = true)
	protected Object id;

	@XmlElement
	protected Object version;

	@XmlElement(required = true)
	protected Class<?> realType;

	public ObjRef() {
		// Intended blank
	}

	public ObjRef(Class<?> realType, Object id, Object version) {
		this(realType, IObjRef.PRIMARY_KEY_INDEX, id, version);
	}

	public ObjRef(Class<?> realType, byte idNameIndex, Object id, Object version) {
		setRealType(realType);
		setIdNameIndex(idNameIndex);
		setId(id);
		setVersion(version);
	}

	public void init(Class<?> entityType, byte idNameIndex, Object id, Object version) {
		setRealType(entityType);
		setIdNameIndex(idNameIndex);
		setId(id);
		setVersion(version);
	}

	@Override
	public Object getId() {
		return id;
	}

	@Override
	public void setId(Object id) {
		this.id = id;
	}

	@Override
	public byte getIdNameIndex() {
		return idNameIndex;
	}

	@Override
	public void setIdNameIndex(byte idNameIndex) {
		this.idNameIndex = idNameIndex;
	}

	@Override
	public Object getVersion() {
		return version;
	}

	@Override
	public void setVersion(Object version) {
		this.version = version;
	}

	@Override
	public Class<?> getRealType() {
		return realType;
	}

	@Override
	public void setRealType(Class<?> realType) {
		this.realType = realType;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof IObjRef)) {
			return false;
		}
		return this.equals((IObjRef) obj);
	}

	public boolean equals(IObjRef obj) {
		if (obj == null) {
			return false;
		}
		if (getIdNameIndex() != obj.getIdNameIndex() || !getRealType().equals(obj.getRealType())) {
			return false;
		}
		Object id = getId();
		Object otherId = obj.getId();
		if (id == null || otherId == null) {
			return false;
		}
		if (!id.getClass().isArray() || !otherId.getClass().isArray()) {
			return id.equals(otherId);
		}
		Object[] idArray = (Object[]) id;
		Object[] otherIdArray = (Object[]) otherId;
		if (idArray.length != otherIdArray.length) {
			return false;
		}
		for (int a = idArray.length; a-- > 0;) {
			if (!idArray[a].equals(otherIdArray[a])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return getId().hashCode() ^ getRealType().hashCode() ^ getIdNameIndex();
	}

	@Override
	public String toString() {
		return StringBuilderUtil.printPrintable(this);
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append("ObjRef ");
		byte idIndex = getIdNameIndex();
		if (idIndex == IObjRef.PRIMARY_KEY_INDEX) {
			sb.append("PK=");
		}
		else {
			sb.append("AK").append(idIndex).append('=');
		}
		StringBuilderUtil.appendPrintable(sb, getId());
		sb.append(" version=").append(getVersion()).append(" type=").append(getRealType().getName());
	}
}
