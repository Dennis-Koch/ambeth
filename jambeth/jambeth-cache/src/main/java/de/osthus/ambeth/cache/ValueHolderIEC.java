package de.osthus.ambeth.cache;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.proxy.ICgLibUtil;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.typeinfo.EmbeddedTypeInfoItem;
import de.osthus.ambeth.typeinfo.FieldInfoItemASM;
import de.osthus.ambeth.typeinfo.IEmbeddedTypeInfoItem;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.MethodPropertyInfoASM;
import de.osthus.ambeth.typeinfo.PropertyInfoItem;
import de.osthus.ambeth.util.IParamHolder;
import de.osthus.ambeth.util.ParamHolder;
import de.osthus.ambeth.util.ReflectUtil;

public class ValueHolderIEC extends SmartCopyMap<Class<?>, Class<?>> implements IProxyHelper, IInitializingBean
{
	public static class ValueHolderContainerEntry
	{
		protected final AbstractValueHolderEntry2[] entries;

		public ValueHolderContainerEntry(Class<?> targetType, IRelationInfoItem[] members, IBytecodeEnhancer bytecodeEnhancer,
				IPropertyInfoProvider propertyInfoProvider)
		{
			entries = new AbstractValueHolderEntry2[members.length];
			try
			{
				FieldAccess targetFieldAccess = FieldAccess.get(targetType);
				MethodAccess targetMethodAccess = MethodAccess.get(targetType);

				for (int relationIndex = members.length; relationIndex-- > 0;)
				{
					IRelationInfoItem member = members[relationIndex];
					AbstractValueHolderEntry2 vhEntry = new AbstractValueHolderEntry2(targetType, member, targetMethodAccess, targetFieldAccess,
							bytecodeEnhancer, propertyInfoProvider);
					entries[relationIndex] = vhEntry;
				}
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e, "Error occured while processing type '" + targetType.getName() + "'");
			}
		}

		public void setUninitialized(Object obj, int relationIndex, IObjRef[] objRefs)
		{
			entries[relationIndex].setUninitialized(obj, objRefs);
		}

		public void setInitialized(Object obj, int relationIndex, Object value)
		{
			entries[relationIndex].setInitialized(obj, value);
		}

		public void setInitPending(Object obj, int relationIndex)
		{
			entries[relationIndex].setInitPending(obj);
		}

		public IObjRef[] getObjRefs(Object obj, int relationIndex)
		{
			return entries[relationIndex].getObjRefs(obj);
		}

		public void setObjRefs(Object obj, int relationIndex, IObjRef[] objRefs)
		{
			entries[relationIndex].setObjRefs(obj, objRefs);
		}

		public Object getValueDirect(Object obj, int relationIndex)
		{
			return entries[relationIndex].getValueDirect(obj);
		}

		public void setValueDirect(Object obj, int relationIndex, Object value)
		{
			entries[relationIndex].setInitialized(obj, value);
		}

		public boolean isInitialized(Object obj, int relationIndex)
		{
			return ValueHolderState.INIT == getState(obj, relationIndex);
		}

		public ValueHolderState getState(Object obj, int relationIndex)
		{
			return entries[relationIndex].getState(obj);
		}

		public void setState(Object obj, int relationIndex, ValueHolderState state)
		{
			entries[relationIndex].setState(obj, state);
		}
	}

	public static abstract class AbstractValueHolderEntry
	{
		public abstract void setObjRefs(Object obj, IObjRef[] objRefs);

		public abstract void setUninitialized(Object obj, IObjRef[] objRefs);

		public abstract void setInitialized(Object obj, Object value);

		public abstract void setInitPending(Object obj);

		public abstract IObjRef[] getObjRefs(Object obj);

		public abstract Object getValueDirect(Object obj);

		public abstract ValueHolderState getState(Object obj);

		public abstract void setState(Object obj, ValueHolderState state);
	}

	public static class AbstractValueHolderEntry2 extends AbstractValueHolderEntry
	{
		protected static final Object[] EMPTY_ARGS = new Object[0];

		protected static final Object[] NULL_ARG = new Object[1];

		protected final String memberName;

		protected final ITypeInfoItem objRefs;

		protected final ITypeInfoItem state;

		protected final ITypeInfoItem directValue;

		protected final ITypeInfoItem member;

		public AbstractValueHolderEntry2(Class<?> targetType, IRelationInfoItem member, MethodAccess methodAccess, FieldAccess fieldAccess,
				IBytecodeEnhancer bytecodeEnhancer, IPropertyInfoProvider propertyInfoProvider)
		{
			this.member = member;
			memberName = member.getName();
			String lastPropertyName = memberName;
			Class<?> currType = targetType;
			ITypeInfoItem[] getDelegates = null;

			if (member instanceof IEmbeddedTypeInfoItem)
			{
				IEmbeddedTypeInfoItem embeddedMember = (IEmbeddedTypeInfoItem) member;
				lastPropertyName = embeddedMember.getChildMember().getName();
				ParamHolder<Class<?>> currTypeOut = new ParamHolder<Class<?>>();
				getDelegates = getMemberDelegate(targetType, embeddedMember, currTypeOut, bytecodeEnhancer, propertyInfoProvider);
				currType = currTypeOut.getValue();
			}
			Field[] initIndex = ReflectUtil.getDeclaredFieldInHierarchy(currType, ValueHolderIEC.getInitializedFieldName(lastPropertyName));
			if (initIndex.length == 0)
			{
				throw new IllegalStateException("No field '" + ValueHolderIEC.getInitializedFieldName(lastPropertyName) + "' found");
			}
			Field[] objRefsIndex = ReflectUtil.getDeclaredFieldInHierarchy(currType, ValueHolderIEC.getObjRefsFieldName(lastPropertyName));
			if (objRefsIndex.length == 0)
			{
				throw new IllegalStateException("No field '" + ValueHolderIEC.getObjRefsFieldName(lastPropertyName) + "' found");
			}
			ITypeInfoItem initializedFI_last = new FieldInfoItemASM(initIndex[0], FieldAccess.get(initIndex[0].getDeclaringClass()));
			ITypeInfoItem objRefsFI_last = new FieldInfoItemASM(objRefsIndex[0], FieldAccess.get(objRefsIndex[0].getDeclaringClass()));
			Method m_getDirectValue = ReflectUtil.getDeclaredMethod(false, currType, (Type) null,
					ValueHolderIEC.getGetterNameOfRelationPropertyWithNoInit(lastPropertyName));
			Method m_setDirectValue = ReflectUtil.getDeclaredMethod(false, currType, null,
					ValueHolderIEC.getSetterNameOfRelationPropertyWithNoInit(lastPropertyName), m_getDirectValue.getReturnType());
			MethodPropertyInfoASM pi_directValue = new MethodPropertyInfoASM(currType, lastPropertyName + ValueHolderIEC.getNoInitSuffix(), m_getDirectValue,
					m_setDirectValue, null, MethodAccess.get(m_getDirectValue.getDeclaringClass()));
			ITypeInfoItem directValueFI_last = new PropertyInfoItem(pi_directValue);
			state = buildCompositeDelegate(initializedFI_last, getDelegates);
			objRefs = buildCompositeDelegate(objRefsFI_last, getDelegates);
			directValue = buildCompositeDelegate(directValueFI_last, getDelegates);
			// }
			// else
			// {
			// // FieldInfo initIndex = targetType.GetField(ValueHolderIEC.GetInitializedFieldName(member.Name));
			// // if (initIndex == null)
			// // {
			// // throw new Exception("No field '" + ValueHolderIEC.GetInitializedFieldName(member.Name) + "' found");
			// // }
			// // FieldInfo objRefsIndex = targetType.GetField(ValueHolderIEC.GetObjRefsFieldName(member.Name));
			// // if (objRefsIndex == null)
			// // {
			// // throw new Exception("No field '" + ValueHolderIEC.GetObjRefsFieldName(member.Name) + "' found");
			// // }
			// // getState = TypeUtility.GetMemberGetDelegate(targetType, initIndex.Name);
			// // setState = TypeUtility.GetMemberSetDelegate(targetType, initIndex.Name);
			// // getObjRefs = TypeUtility.GetMemberGetDelegate(targetType, objRefsIndex.Name);
			// // setObjRefs = TypeUtility.GetMemberSetDelegate(targetType, objRefsIndex.Name);
			// // getDirectValue = TypeUtility.GetMemberGetDelegate(targetType, ValueHolderIEC.GetGetterNameOfRelationPropertyWithNoInit(member.Name));
			// // setDirectValue = TypeUtility.GetMemberSetDelegate(targetType, ValueHolderIEC.GetSetterNameOfRelationPropertyWithNoInit(member.Name));
			//
			// String initializedMember = ValueHolderIEC.getInitializedFieldName(memberName);
			// Integer stateIndex = fieldNameToFieldAccess.get(initializedMember);
			// if (stateIndex == null)
			// {
			// throw new IllegalStateException("No field '" + initializedMember + "' found");
			// }
			// stateFieldIndex = stateIndex.intValue();
			// String objRefsMember = ValueHolderIEC.getObjRefsFieldName(memberName);
			// Integer objRefsIndex = fieldNameToFieldAccess.get(objRefsMember);
			// if (objRefsIndex == null)
			// {
			// throw new IllegalStateException("No field '" + objRefsMember + "' found");
			// }
			// objRefsFieldIndex = objRefsIndex.intValue();
			//
			// directValueGetterMethodIndex = methodAccess.getIndex(ValueHolderIEC.getGetterNameOfRelationPropertyWithNoInit(memberName));
			// directValueSetterMethodIndex = methodAccess.getIndex(ValueHolderIEC.getSetterNameOfRelationPropertyWithNoInit(memberName));
			// }
		}

		protected ITypeInfoItem[] getMemberDelegate(Class<?> targetType, IEmbeddedTypeInfoItem member, IParamHolder<Class<?>> currTypeOut,
				IBytecodeEnhancer bytecodeEnhancer, IPropertyInfoProvider propertyInfoProvider)
		{
			Class<?> currType = targetType;
			Class<?> parentObjectType = targetType;
			String embeddedPath = "";
			String[] memberPath = member.getMemberPathToken();
			for (int a = 0, size = memberPath.length; a < size; a++)
			{
				String memberItem = memberPath[a];

				if (embeddedPath.length() > 0)
				{
					embeddedPath += ".";
				}
				embeddedPath += memberItem;
				IPropertyInfo pi = propertyInfoProvider.getProperty(currType, memberItem);
				if (pi != null)
				{
					parentObjectType = currType;
					currType = pi.getPropertyType();
					currType = bytecodeEnhancer.getEnhancedType(currType, new EmbeddedEnhancementHint(targetType, parentObjectType, embeddedPath));
					continue;
				}
				Field[] fi = ReflectUtil.getDeclaredFieldInHierarchy(currType, memberItem);
				if (fi.length != 0)
				{
					parentObjectType = currType;
					currType = fi[0].getType();
					currType = bytecodeEnhancer.getEnhancedType(currType, new EmbeddedEnhancementHint(targetType, parentObjectType, embeddedPath));
					continue;
				}
				Method mi = ReflectUtil.getDeclaredMethod(true, currType, null, memberItem, new Class<?>[0]);
				if (mi != null)
				{
					parentObjectType = currType;
					currType = mi.getReturnType();
					currType = bytecodeEnhancer.getEnhancedType(currType, new EmbeddedEnhancementHint(targetType, parentObjectType, embeddedPath));
					continue;
				}
				throw new IllegalStateException("Property/Field/Method not found: " + currType + "." + memberItem);
			}
			currTypeOut.setValue(currType);
			return member.getMemberPath();
		}

		protected ITypeInfoItem buildCompositeDelegate(ITypeInfoItem lastDelegate, ITypeInfoItem[] getDelegates)
		{
			if (getDelegates == null || getDelegates.length == 0)
			{
				return lastDelegate;
			}
			StringBuilder nameSB = new StringBuilder();
			for (ITypeInfoItem member : getDelegates)
			{
				nameSB.append(member.getName());
				nameSB.append('.');
			}
			nameSB.append(lastDelegate.getName());
			return new EmbeddedTypeInfoItem(nameSB.toString(), lastDelegate, getDelegates);
		}

		@Override
		public void setObjRefs(Object obj, IObjRef[] objRefs)
		{
			if (objRefs != null && objRefs.length == 0)
			{
				objRefs = ObjRef.EMPTY_ARRAY;
			}
			this.objRefs.setValue(obj, objRefs);
		}

		@Override
		public void setUninitialized(Object obj, IObjRef[] objRefs)
		{
			state.setValue(obj, ValueHolderState.LAZY);
			this.objRefs.setValue(obj, objRefs);
			directValue.setValue(obj, null);
		}

		@Override
		public void setInitialized(Object obj, Object value)
		{
			member.setValue(obj, value);
		}

		@Override
		public void setInitPending(Object obj)
		{
			state.setValue(obj, ValueHolderState.PENDING);
		}

		@Override
		public IObjRef[] getObjRefs(Object obj)
		{
			return (IObjRef[]) objRefs.getValue(obj);
		}

		@Override
		public Object getValueDirect(Object obj)
		{
			return directValue.getValue(obj);
		}

		@Override
		public ValueHolderState getState(Object obj)
		{
			return (ValueHolderState) state.getValue(obj);
		}

		@Override
		public void setState(Object obj, ValueHolderState state)
		{
			this.state.setValue(obj, state);
		}
	}

	// public static class ValueHolderFieldAccessEntry extends AbstractValueHolderEntry2
	// {
	// protected final FieldAccess entityFieldAccess;
	//
	// protected final int fieldIndex;
	//
	// public ValueHolderFieldAccessEntry(IRelationInfoItem member, FieldAccess fieldAccess, FieldAccess entityFieldAccess,
	// Map<String, Integer> fieldNameToFieldAccess, Map<String, Integer> fieldNameToEntityFieldAccess, IBytecodeEnhancer bytecodeEnhancer)
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

	public static class ValueHolderTypeInfoItemEntry extends AbstractValueHolderEntry2
	{
		protected final IRelationInfoItem member;

		public ValueHolderTypeInfoItemEntry(Class<?> targetType, IRelationInfoItem member, MethodAccess methodAccess, FieldAccess fieldAccess,
				IBytecodeEnhancer bytecodeEnhancer, IPropertyInfoProvider propertyInfoProvider)
		{
			super(targetType, member, methodAccess, fieldAccess, bytecodeEnhancer, propertyInfoProvider);
			this.member = member;
		}

		@Override
		public void setUninitialized(Object obj, IObjRef[] objRefs)
		{
			super.setUninitialized(obj, objRefs);
			member.setValue(obj, null);
		}
	}

	public static final String getObjRefsFieldName(String propertyName)
	{
		return propertyName + "$objRefs";
	}

	public static final String getInitializedFieldName(String propertyName)
	{
		return propertyName + "$state";
	}

	public static final String getSetterNameOfRelationPropertyWithNoInit(String propertyName)
	{
		return "set" + propertyName + getNoInitSuffix();
	}

	public static final String getGetterNameOfRelationPropertyWithNoInit(String propertyName)
	{
		return "get" + propertyName + getNoInitSuffix();
	}

	public static final String getNoInitSuffix()
	{
		return "$NoInit";
	}

	@Autowired
	protected ICgLibUtil cgLibUtil;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired(optional = true)
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	protected final SmartCopyMap<Class<?>, ValueHolderContainerEntry> typeToVhcEntryMap = new SmartCopyMap<Class<?>, ValueHolderContainerEntry>(0.5f);

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	protected ValueHolderContainerEntry getVhcEntry(Object parentObj)
	{
		if (!(parentObj instanceof IObjRefContainer))
		{
			return null;
		}
		return getVhcEntry(parentObj.getClass());
	}

	public ValueHolderContainerEntry getVhcEntry(Class<?> targetType)
	{
		ValueHolderContainerEntry vhcEntry = typeToVhcEntryMap.get(targetType);
		if (vhcEntry == null)
		{
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(targetType);
			vhcEntry = new ValueHolderContainerEntry(targetType, metaData.getRelationMembers(), bytecodeEnhancer, propertyInfoProvider);
			typeToVhcEntryMap.put(targetType, vhcEntry);
		}
		return vhcEntry;
	}

	@Override
	public Class<?> getRealType(Class<?> type)
	{
		Class<?> realType = get(type);
		if (realType != null)
		{
			return realType;
		}
		IBytecodeEnhancer bytecodeEnhancer = this.bytecodeEnhancer;
		if (bytecodeEnhancer != null)
		{
			realType = bytecodeEnhancer.getBaseType(type);
		}
		if (realType == null)
		{
			realType = cgLibUtil.getOriginalClass(type);
		}
		if (realType == null)
		{
			realType = type;
		}
		put(type, realType);
		return realType;
	}

	@Override
	public boolean objectEquals(Object leftObject, Object rightObject)
	{
		if (leftObject == null)
		{
			return rightObject == null;
		}
		if (rightObject == null)
		{
			return false;
		}
		if (leftObject == rightObject)
		{
			return true;
		}
		Class<?> leftType = leftObject.getClass(), rightType = rightObject.getClass();
		if (!leftType.equals(rightType))
		{
			// Real entity types are not equal
			return false;
		}
		IEntityMetaData leftMetaData = ((IEntityMetaDataHolder) leftObject).get__EntityMetaData();
		Object leftId = leftMetaData.getIdMember().getValue(leftObject, false);
		Object rightId = leftMetaData.getIdMember().getValue(rightObject, false);
		if (leftId == null || rightId == null)
		{
			// Entities are never equal with anything beside themselves if they do not have a persistent id
			return false;
		}
		return leftId.equals(rightId);
	}
}
