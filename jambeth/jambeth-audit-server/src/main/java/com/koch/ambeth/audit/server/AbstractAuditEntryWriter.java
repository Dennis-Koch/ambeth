package com.koch.ambeth.audit.server;

/*-
 * #%L
 * jambeth-audit-server
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

import java.io.DataOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.transfer.PrimitiveUpdateItem;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.model.IDataObject;

public abstract class AbstractAuditEntryWriter {
	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected void writeProperties(String[] props, IEntityMetaDataHolder obj, DataOutputStream os) {
		IConversionHelper conversionHelper = this.conversionHelper;
		IEntityMetaData metaData = obj.get__EntityMetaData();
		if (obj instanceof IDataObject) {
			for (String prop : props) {
				Member member = metaData.getMemberByName(prop);
				Object value = member.getValue(obj);
				String sValue = conversionHelper.convertValueToType(String.class, value);
				writeProperty(prop, sValue, os);
			}
			return;
		}
		IPrimitiveUpdateItem[] fullPUIs = ((CreateOrUpdateContainerBuild) obj).getFullPUIs();
		for (String prop : props) {
			Object value = fullPUIs[metaData.getIndexByPrimitiveName(prop)].getNewValue();
			String sValue = conversionHelper.convertValueToType(String.class, value);
			writeProperty(prop, sValue, os);
		}
	}

	protected Object getPrimitiveValue(String memberName, Object obj) {
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
		if (obj instanceof IDataObject) {
			Member member = metaData.getMemberByName(memberName);
			return member.getValue(obj);
		}
		PrimitiveUpdateItem pui = ((CreateOrUpdateContainerBuild) obj).findPrimitive(memberName);
		if (pui == null) {
			return null;
		}
		return pui.getNewValue();
	}

	protected IEntityMetaDataHolder getRelationValue(String memberName, Object obj) {
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
		if (obj instanceof IDataObject) {
			Member member = metaData.getMemberByName(memberName);
			return (IEntityMetaDataHolder) member.getValue(obj);
		}
		IRelationUpdateItem rui = ((CreateOrUpdateContainerBuild) obj).findRelation(memberName);
		if (rui == null) {
			return null;
		}
		IObjRef[] addedORIs = rui.getAddedORIs();
		return (CreateOrUpdateContainerBuild) ((IDirectObjRef) addedORIs[0]).getDirect();
	}

	protected void writeProperty(String name, String value, DataOutputStream os) {
		try {
			for (int a = 0, size = name.length(); a < size; a++) {
				os.writeShort(name.charAt(a));
			}
			if (value == null) {
				os.writeBoolean(false);
			}
			else {
				os.writeBoolean(true);
				for (int a = 0, size = value.length(); a < size; a++) {
					os.writeShort(value.charAt(a));
				}
			}
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected List<? extends IEntityMetaDataHolder> sortOrderedMember(String memberName,
			String orderName, Object obj) {
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
		Member member = metaData.getMemberByName(memberName);

		if (obj instanceof IDataObject) {
			Object value = member.getValue(obj);
			ArrayList<IEntityMetaDataHolder> items;
			if (value instanceof Collection) {
				Collection<? extends IEntityMetaDataHolder> coll = (Collection<? extends IEntityMetaDataHolder>) member
						.getValue(obj);
				if (coll.isEmpty()) {
					return EmptyList.<IEntityMetaDataHolder>getInstance();
				}
				items = new ArrayList<>(coll);
			}
			else if (value != null) {
				items = new ArrayList<>(new IEntityMetaDataHolder[] { (IEntityMetaDataHolder) value });
			}
			else {
				return EmptyList.<IEntityMetaDataHolder>getInstance();
			}
			if (orderName == null) {
				return items;
			}
			IEntityMetaData itemMetaData = entityMetaDataProvider.getMetaData(member.getElementType());
			final Member orderMember = itemMetaData.getMemberByName(orderName);

			Collections.sort(items, new Comparator<IEntityMetaDataHolder>() {
				@Override
				public int compare(IEntityMetaDataHolder o1, IEntityMetaDataHolder o2) {
					int order1 = orderMember.getIntValue(o1);
					int order2 = orderMember.getIntValue(o2);
					if (order1 == order2) {
						return 0;
					}
					return order1 < order2 ? -1 : 1;
				}
			});
			for (int a = items.size(); a-- > 0;) {
				IEntityMetaDataHolder item = items.get(a);
				int order = orderMember.getIntValue(item);
				if (order != a + 1) {
					return null;
				}
			}
			return items;
		}
		IRelationUpdateItem rui = ((CreateOrUpdateContainerBuild) obj).findRelation(memberName);
		if (rui == null) {
			return EmptyList.<IEntityMetaDataHolder>getInstance();
		}
		IObjRef[] addedORIs = rui.getAddedORIs();
		ArrayList<CreateOrUpdateContainerBuild> items = new ArrayList<>(addedORIs.length);
		for (IObjRef addedORI : addedORIs) {
			items.add((CreateOrUpdateContainerBuild) ((IDirectObjRef) addedORI).getDirect());
		}
		if (orderName == null) {
			return items;
		}
		IEntityMetaData itemMetaData = entityMetaDataProvider.getMetaData(member.getElementType());
		final int orderIndex = itemMetaData.getIndexByPrimitiveName(orderName);

		Collections.sort(items, new Comparator<CreateOrUpdateContainerBuild>() {
			@Override
			public int compare(CreateOrUpdateContainerBuild o1, CreateOrUpdateContainerBuild o2) {
				int order1 = ((Number) o1.getFullPUIs()[orderIndex].getNewValue()).intValue();
				int order2 = ((Number) o2.getFullPUIs()[orderIndex].getNewValue()).intValue();
				if (order1 == order2) {
					return 0;
				}
				return order1 < order2 ? -1 : 1;
			}
		});
		for (int a = items.size(); a-- > 0;) {
			CreateOrUpdateContainerBuild item = items.get(a);
			int order = ((Number) item.getFullPUIs()[orderIndex].getNewValue()).intValue();
			if (order != a + 1) {
				return null;
			}
		}
		return items;
	}
}