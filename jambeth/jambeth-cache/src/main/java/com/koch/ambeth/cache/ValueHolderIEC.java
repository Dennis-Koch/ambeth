package com.koch.ambeth.cache;

/*-
 * #%L
 * jambeth-cache
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.ioc.proxy.ICgLibUtil;
import com.koch.ambeth.merge.IProxyHelper;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.merge.metadata.IMemberTypeProvider;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.IEmbeddedMember;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class ValueHolderIEC extends SmartCopyMap<Class<?>, Class<?>>
		implements IProxyHelper, IInitializingBean {
	public static class ValueHolderContainerEntry {
		protected final ValueHolderEntry[] entries;

		public ValueHolderContainerEntry(Class<?> targetType, RelationMember[] members,
				IBytecodeEnhancer bytecodeEnhancer, IPropertyInfoProvider propertyInfoProvider,
				IMemberTypeProvider memberTypeProvider) {
			entries = new ValueHolderEntry[members.length];
			try {
				FieldAccess targetFieldAccess = FieldAccess.get(targetType);
				MethodAccess targetMethodAccess = MethodAccess.get(targetType);

				for (int relationIndex = members.length; relationIndex-- > 0;) {
					RelationMember member = members[relationIndex];
					ValueHolderEntry vhEntry = new ValueHolderEntry(targetType, member, targetMethodAccess,
							targetFieldAccess, bytecodeEnhancer, propertyInfoProvider, memberTypeProvider);
					entries[relationIndex] = vhEntry;
				}
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e,
						"Error occured while processing type '" + targetType.getName() + "'");
			}
		}

		public void setUninitialized(Object obj, int relationIndex, IObjRef[] objRefs) {
			entries[relationIndex].setUninitialized(obj, objRefs);
		}

		public void setInitialized(Object obj, int relationIndex, Object value) {
			entries[relationIndex].setInitialized(obj, value);
		}

		public void setInitPending(Object obj, int relationIndex) {
			entries[relationIndex].setInitPending(obj);
		}

		public IObjRef[] getObjRefs(Object obj, int relationIndex) {
			return entries[relationIndex].getObjRefs(obj);
		}

		public void setObjRefs(Object obj, int relationIndex, IObjRef[] objRefs) {
			entries[relationIndex].setObjRefs(obj, objRefs);
		}

		public Object getValueDirect(Object obj, int relationIndex) {
			return entries[relationIndex].getValueDirect(obj);
		}

		public void setValueDirect(Object obj, int relationIndex, Object value) {
			entries[relationIndex].setInitialized(obj, value);
		}

		public boolean isInitialized(Object obj, int relationIndex) {
			return ValueHolderState.INIT == getState(obj, relationIndex);
		}

		public ValueHolderState getState(Object obj, int relationIndex) {
			return entries[relationIndex].getState(obj);
		}

		public void setState(Object obj, int relationIndex, ValueHolderState state) {
			entries[relationIndex].setState(obj, state);
		}
	}

	public static abstract class AbstractValueHolderEntry {
		public abstract void setObjRefs(Object obj, IObjRef[] objRefs);

		public abstract void setUninitialized(Object obj, IObjRef[] objRefs);

		public abstract void setInitialized(Object obj, Object value);

		public abstract void setInitPending(Object obj);

		public abstract IObjRef[] getObjRefs(Object obj);

		public abstract Object getValueDirect(Object obj);

		public abstract ValueHolderState getState(Object obj);

		public abstract void setState(Object obj, ValueHolderState state);
	}

	public static class ValueHolderEntry extends AbstractValueHolderEntry {
		protected static final Object[] EMPTY_ARGS = new Object[0];

		protected static final Object[] NULL_ARG = new Object[1];

		protected final String memberName;

		protected final Member objRefs;

		protected final Member state;

		protected final Member directValue;

		protected final RelationMember member;

		public ValueHolderEntry(Class<?> targetType, RelationMember member, MethodAccess methodAccess,
				FieldAccess fieldAccess, IBytecodeEnhancer bytecodeEnhancer,
				IPropertyInfoProvider propertyInfoProvider, IMemberTypeProvider memberTypeProvider) {
			this.member = member;
			memberName = member.getName();
			String lastPropertyName = memberName;
			String prefix = "";

			if (member instanceof IEmbeddedMember) {
				IEmbeddedMember embeddedMember = (IEmbeddedMember) member;
				lastPropertyName = embeddedMember.getChildMember().getName();

				prefix = embeddedMember.getMemberPathString() + ".";
			}
			state = memberTypeProvider.getMember(targetType,
					prefix + ValueHolderIEC.getInitializedFieldName(lastPropertyName));
			objRefs = memberTypeProvider.getMember(targetType,
					prefix + ValueHolderIEC.getObjRefsFieldName(lastPropertyName));
			directValue = memberTypeProvider.getMember(targetType,
					prefix + lastPropertyName + ValueHolderIEC.getNoInitSuffix());
		}

		@Override
		public void setObjRefs(Object obj, IObjRef[] objRefs) {
			if (objRefs != null && objRefs.length == 0) {
				objRefs = IObjRef.EMPTY_ARRAY;
			}
			this.objRefs.setValue(obj, objRefs);
		}

		@Override
		public void setUninitialized(Object obj, IObjRef[] objRefs) {
			state.setValue(obj, ValueHolderState.LAZY);
			this.objRefs.setValue(obj, objRefs);
			directValue.setValue(obj, null);
		}

		@Override
		public void setInitialized(Object obj, Object value) {
			member.setValue(obj, value);
		}

		@Override
		public void setInitPending(Object obj) {
			state.setValue(obj, ValueHolderState.PENDING);
		}

		@Override
		public IObjRef[] getObjRefs(Object obj) {
			return (IObjRef[]) objRefs.getValue(obj, false);
		}

		@Override
		public Object getValueDirect(Object obj) {
			return directValue.getValue(obj, false);
		}

		@Override
		public ValueHolderState getState(Object obj) {
			return (ValueHolderState) state.getValue(obj, false);
		}

		@Override
		public void setState(Object obj, ValueHolderState state) {
			this.state.setValue(obj, state);
		}
	}

	// public static class ValueHolderFieldAccessEntry extends AbstractValueHolderEntry2
	// {
	// protected final FieldAccess entityFieldAccess;
	//
	// protected final int fieldIndex;
	//
	// public ValueHolderFieldAccessEntry(IRelationInfoItem member, FieldAccess fieldAccess,
	// FieldAccess entityFieldAccess,
	// Map<String, Integer> fieldNameToFieldAccess, Map<String, Integer> fieldNameToEntityFieldAccess,
	// IBytecodeEnhancer bytecodeEnhancer)
	// {
	// super(member, fieldAccess, fieldNameToFieldAccess, bytecodeEnhancer);
	// this.entityFieldAccess = entityFieldAccess;
	//
	// String fieldName;
	// if (member instanceof PropertyInfoItem)
	// {
	// Field field = ((PropertyInfoItem) member).getProperty().getBackingField();
	// if (field == null)
	// {
	// throw new IllegalStateException("No backing field found for property " + member);
	// }
	// if (Modifier.isPrivate(field.getModifiers()))
	// {
	// throw new IllegalStateException("Backing field must not be private for property " + member);
	// }
	// fieldName = field.getName();
	// }
	// else
	// {
	// Field field = ((FieldInfoItem) member).getField();
	// if (Modifier.isPrivate(field.getModifiers()))
	// {
	// throw new IllegalStateException("Field must not be private for property " + member);
	// }
	// fieldName = field.getName();
	// }
	// Integer index = fieldNameToEntityFieldAccess.get(fieldName);
	// if (index == null)
	// {
	// throw new IllegalStateException("No field '" + fieldName + "' found");
	// }
	// fieldIndex = index.intValue();
	// }
	//
	// @Override
	// public void setUninitialized(Object obj, IObjRef[] objRefs)
	// {
	// super.setUninitialized(obj, objRefs);
	// entityFieldAccess.set(obj, fieldIndex, null);
	// }
	// }

	public static final String getObjRefsFieldName(String propertyName) {
		return propertyName + "$ObjRefs";
	}

	public static final String getInitializedFieldName(String propertyName) {
		return propertyName + "$State";
	}

	public static final String getSetterNameOfRelationPropertyWithNoInit(String propertyName) {
		return "set" + propertyName + getNoInitSuffix();
	}

	public static final String getGetterNameOfRelationPropertyWithNoInit(String propertyName) {
		return "get" + propertyName + getNoInitSuffix();
	}

	public static final String getNoInitSuffix() {
		return "$NoInit";
	}

	@Autowired
	protected ICgLibUtil cgLibUtil;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired(optional = true)
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IMemberTypeProvider memberTypeProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	protected final SmartCopyMap<Class<?>, ValueHolderContainerEntry> typeToVhcEntryMap =
			new SmartCopyMap<>(0.5f);

	@Override
	public void afterPropertiesSet() throws Throwable {
		// Intended blank
	}

	protected ValueHolderContainerEntry getVhcEntry(Object parentObj) {
		if (!(parentObj instanceof IObjRefContainer)) {
			return null;
		}
		return getVhcEntry(parentObj.getClass());
	}

	public ValueHolderContainerEntry getVhcEntry(Class<?> targetType) {
		ValueHolderContainerEntry vhcEntry = typeToVhcEntryMap.get(targetType);
		if (vhcEntry == null) {
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(targetType);
			vhcEntry = new ValueHolderContainerEntry(targetType, metaData.getRelationMembers(),
					bytecodeEnhancer, propertyInfoProvider, memberTypeProvider);
			typeToVhcEntryMap.put(targetType, vhcEntry);
		}
		return vhcEntry;
	}

	@Override
	public Class<?> getRealType(Class<?> type) {
		if (type == null) {
			return null;
		}
		Class<?> realType = get(type);
		if (realType != null) {
			return realType;
		}
		IBytecodeEnhancer bytecodeEnhancer = this.bytecodeEnhancer;
		if (bytecodeEnhancer != null) {
			realType = bytecodeEnhancer.getBaseType(type);
		}
		if (realType == null) {
			realType = cgLibUtil.getOriginalClass(type);
		}
		if (realType == null) {
			realType = type;
		}
		put(type, realType);
		return realType;
	}

	@Override
	public boolean objectEquals(Object leftObject, Object rightObject) {
		if (leftObject == null) {
			return rightObject == null;
		}
		if (rightObject == null) {
			return false;
		}
		if (leftObject == rightObject) {
			return true;
		}
		Class<?> leftType = leftObject.getClass(), rightType = rightObject.getClass();
		if (!leftType.equals(rightType)) {
			// Real entity types are not equal
			return false;
		}
		IEntityMetaData leftMetaData = ((IEntityMetaDataHolder) leftObject).get__EntityMetaData();
		Object leftId = leftMetaData.getIdMember().getValue(leftObject, false);
		Object rightId = leftMetaData.getIdMember().getValue(rightObject, false);
		if (leftId == null || rightId == null) {
			// Entities are never equal with anything beside themselves if they do not have a persistent
			// id
			return false;
		}
		return leftId.equals(rightId);
	}
}
