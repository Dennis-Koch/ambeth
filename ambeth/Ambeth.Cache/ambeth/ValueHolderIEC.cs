using System;
using System.Text;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Cache.Model;
#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Proxy;
using System.Reflection;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Exceptions;

namespace De.Osthus.Ambeth.Cache
{
    public class ValueHolderContainerEntry : HashMap<ITypeInfoItem, AbstractValueHolderEntry>
    {
        public ValueHolderContainerEntry(Type targetType, IRelationInfoItem[] members, IBytecodeEnhancer bytecodeEnhancer,
            IPropertyInfoProvider propertyInfoProvider)
            : base(0.5f)
        {
            try
            {
                foreach (IRelationInfoItem member in members)
                {
                    AbstractValueHolderEntry vhEntry = new AbstractValueHolderEntry2(targetType, member, bytecodeEnhancer, propertyInfoProvider);
                    Put(member, vhEntry);
                }
            }
            catch (Exception e)
            {
                throw RuntimeExceptionUtil.Mask(e, "Error occured while processing type '" + targetType.FullName + "'");
            }
        }

        public IObjRef[] GetObjRefs(Object obj, IRelationInfoItem member)
        {
            return Get(member).GetObjRefs(obj);
        }

        public void SetUninitialized(Object obj, IRelationInfoItem member, IObjRef[] objRefs)
        {
            Get(member).SetUninitialized(obj, objRefs);
        }

        public void SetInitialized(Object obj, IRelationInfoItem member, Object value)
        {
            Get(member).SetInitialized(obj, value);
        }

        public void SetObjRefs(Object obj, IRelationInfoItem member, IObjRef[] objRefs)
        {
            Get(member).SetObjRefs(obj, objRefs);
        }

        public Object GetValueDirect(Object obj, IRelationInfoItem member)
        {
            return Get(member).GetValueDirect(obj);
        }

        public ValueHolderState GetState(Object obj, IRelationInfoItem member)
        {
            return Get(member).GetState(obj);
        }

        public void SetState(Object obj, IRelationInfoItem member, ValueHolderState state)
        {
            Get(member).SetState(obj, state);
        }
    }

    public abstract class AbstractValueHolderEntry
    {
        public abstract void SetObjRefs(Object obj, IObjRef[] objRefs);

        public abstract void SetUninitialized(Object obj, IObjRef[] objRefs);

        public abstract void SetInitialized(Object obj, Object value);

        public abstract IObjRef[] GetObjRefs(Object obj);

        public abstract Object GetValueDirect(Object obj);

        public abstract ValueHolderState GetState(Object obj);

        public abstract void SetState(Object obj, ValueHolderState state);
    }

    public class AbstractValueHolderEntry2 : AbstractValueHolderEntry
    {
        protected readonly String memberName;

        protected readonly MemberGetDelegate getState;

        protected readonly MemberSetDelegate setState;

        protected readonly MemberGetDelegate getObjRefs;

        protected readonly MemberSetDelegate setObjRefs;

        protected readonly MemberGetDelegate getDirectValue;

        protected readonly MemberSetDelegate setDirectValue;

        protected readonly ITypeInfoItem objRefs;

        protected readonly ITypeInfoItem state;

        protected readonly ITypeInfoItem directValue;

        protected readonly ITypeInfoItem member;

        public AbstractValueHolderEntry2(Type targetType, IRelationInfoItem member, IBytecodeEnhancer bytecodeEnhancer, IPropertyInfoProvider propertyInfoProvider)
        {
            this.member = member;
            this.memberName = member.Name;
            String lastPropertyName = memberName;
            Type currType = targetType;
            MemberGetDelegate[] getDelegates = null;
            if (member is IEmbeddedTypeInfoItem)
            {
                IEmbeddedTypeInfoItem embeddedMember = (IEmbeddedTypeInfoItem)member;
                lastPropertyName = embeddedMember.ChildMember.Name;
                getDelegates = GetMemberDelegate(targetType, embeddedMember.MemberPathToken, out currType,
                    bytecodeEnhancer, propertyInfoProvider);
            }
            FieldInfo[] initIndex = ReflectUtil.GetDeclaredFieldInHierarchy(currType, ValueHolderIEC.GetInitializedFieldName(lastPropertyName));
            if (initIndex.Length == 0)
            {
                throw new Exception("No field '" + ValueHolderIEC.GetInitializedFieldName(lastPropertyName) + "' found");
            }
            FieldInfo[] objRefsIndex = ReflectUtil.GetDeclaredFieldInHierarchy(currType, ValueHolderIEC.GetObjRefsFieldName(lastPropertyName));
            if (objRefsIndex.Length == 0)
            {
                throw new Exception("No field '" + ValueHolderIEC.GetObjRefsFieldName(lastPropertyName) + "' found");
            }
            MemberGetDelegate getInitializedFI_last = TypeUtility.GetMemberGetDelegate(currType, initIndex[0].Name);
            MemberSetDelegate setInitializedFI_last = TypeUtility.GetMemberSetDelegate(currType, initIndex[0].Name);
            MemberGetDelegate getObjRefsFI_last = TypeUtility.GetMemberGetDelegate(currType, objRefsIndex[0].Name);
            MemberSetDelegate setObjRefsFI_last = TypeUtility.GetMemberSetDelegate(currType, objRefsIndex[0].Name);
            MemberGetDelegate getDirectValueFI_last = TypeUtility.GetMemberGetDelegate(currType, ValueHolderIEC.GetGetterNameOfRelationPropertyWithNoInit(lastPropertyName));
            MemberSetDelegate setDirectValueFI_last = TypeUtility.GetMemberSetDelegate(currType, ValueHolderIEC.GetSetterNameOfRelationPropertyWithNoInit(lastPropertyName));
            getState = BuildCompositeDelegate(getInitializedFI_last, getDelegates);
            setState = BuildCompositeDelegate(setInitializedFI_last, getDelegates);
            getObjRefs = BuildCompositeDelegate(getObjRefsFI_last, getDelegates);
            setObjRefs = BuildCompositeDelegate(setObjRefsFI_last, getDelegates);
            getDirectValue = BuildCompositeDelegate(getDirectValueFI_last, getDelegates);
            setDirectValue = BuildCompositeDelegate(setDirectValueFI_last, getDelegates);
        }

        protected MemberGetDelegate[] GetMemberDelegate(Type targetType, String[] memberPath,
            out Type currTypeOut, IBytecodeEnhancer bytecodeEnhancer, IPropertyInfoProvider propertyInfoProvider)
        {
            Type currType = targetType;
            Type parentObjectType = targetType;
            String embeddedPath = "";
            MemberGetDelegate[] getDelegates = new MemberGetDelegate[memberPath.Length];
            //MemberSetDelegate[] setDelegates = new MemberSetDelegate[memberPath.Length];
            for (int a = 0, size = memberPath.Length; a < size; a++)
            {
                String memberItem = memberPath[a];

                if (embeddedPath.Length > 0)
                {
                    embeddedPath += ".";
                }
                embeddedPath += memberItem;
                getDelegates[a] = TypeUtility.GetMemberGetDelegate(currType, memberItem);
                //  setDelegates[a] = TypeUtility.GetMemberSetDelegate(currType, memberItem);
                PropertyInfo pi = currType.GetProperty(memberItem);
                if (pi != null)
                {
                    parentObjectType = currType;
                    currType = pi.PropertyType;
                    currType = bytecodeEnhancer.GetEnhancedType(currType, new EmbeddedEnhancementHint(targetType, parentObjectType, embeddedPath));
                    continue;
                }
                FieldInfo[] fi = ReflectUtil.GetDeclaredFieldInHierarchy(currType, memberItem);
                if (fi.Length > 0)
                {
                    parentObjectType = currType;
                    currType = fi[0].FieldType;
                    currType = bytecodeEnhancer.GetEnhancedType(currType, new EmbeddedEnhancementHint(targetType, parentObjectType, embeddedPath));
                    continue;
                }
                MethodInfo mi = ReflectUtil.GetDeclaredMethod(true, currType, null, memberItem, Type.EmptyTypes);
                if (mi != null)
                {
                    parentObjectType = currType;
                    currType = mi.ReturnType;
                    currType = bytecodeEnhancer.GetEnhancedType(currType, new EmbeddedEnhancementHint(targetType, parentObjectType, embeddedPath));
                    continue;
                }
                throw new Exception("Property/Field/Method not found: " + currType + "." + memberItem);
            }
            currTypeOut = currType;
            return getDelegates;
        }

        protected MemberGetDelegate BuildCompositeDelegate(MemberGetDelegate lastDelegate, MemberGetDelegate[] getDelegates)
        {
            if (getDelegates == null || getDelegates.Length == 0)
            {
                return lastDelegate;
            }
            return delegate(Object obj)
            {
                foreach (MemberGetDelegate getDelegateItem in getDelegates)
                {
                    obj = getDelegateItem(obj);
                    if (obj == null)
                    {
                        return null;
                    }
                }
                return lastDelegate(obj);
            };
        }

        protected MemberSetDelegate BuildCompositeDelegate(MemberSetDelegate lastDelegate, MemberGetDelegate[] getDelegates)
        {
            if (getDelegates == null || getDelegates.Length == 0)
            {
                return lastDelegate;
            }
            return delegate(Object obj, Object value)
            {
                foreach (MemberGetDelegate getDelegateItem in getDelegates)
                {
                    obj = getDelegateItem(obj);
                    if (obj == null)
                    {
                        throw new Exception("Embedded object is null");
                    }
                }
                lastDelegate(obj, value);
            };
        }

        protected ITypeInfoItem[] GetMemberDelegate(Type targetType, IEmbeddedTypeInfoItem member, IParamHolder<Type> currTypeOut,
                IBytecodeEnhancer bytecodeEnhancer, IPropertyInfoProvider propertyInfoProvider)
        {
            Type currType = targetType;
            Type parentObjectType = targetType;
            String embeddedPath = "";
            String[] memberPath = member.MemberPathToken;
            for (int a = 0, size = memberPath.Length; a < size; a++)
            {
                String memberItem = memberPath[a];

                if (embeddedPath.Length > 0)
                {
                    embeddedPath += ".";
                }
                embeddedPath += memberItem;
                IPropertyInfo pi = propertyInfoProvider.GetProperty(currType, memberItem);
                if (pi != null)
                {
                    parentObjectType = currType;
                    currType = pi.PropertyType;
                    currType = bytecodeEnhancer.GetEnhancedType(currType, new EmbeddedEnhancementHint(targetType, parentObjectType, embeddedPath));
                    continue;
                }
                FieldInfo[] fi = ReflectUtil.GetDeclaredFieldInHierarchy(currType, memberItem);
                if (fi.Length != 0)
                {
                    parentObjectType = currType;
                    currType = fi[0].FieldType;
                    currType = bytecodeEnhancer.GetEnhancedType(currType, new EmbeddedEnhancementHint(targetType, parentObjectType, embeddedPath));
                    continue;
                }
                MethodInfo mi = ReflectUtil.GetDeclaredMethod(true, currType, null, memberItem, Type.EmptyTypes);
                if (mi != null)
                {
                    parentObjectType = currType;
                    currType = mi.ReturnType;
                    currType = bytecodeEnhancer.GetEnhancedType(currType, new EmbeddedEnhancementHint(targetType, parentObjectType, embeddedPath));
                    continue;
                }
                throw new Exception("Property/Field/Method not found: " + currType + "." + memberItem);
            }
            currTypeOut.Value = currType;
            return member.MemberPath;
        }

        protected ITypeInfoItem BuildCompositeDelegate(ITypeInfoItem lastDelegate, ITypeInfoItem[] getDelegates)
        {
            if (getDelegates == null || getDelegates.Length == 0)
            {
                return lastDelegate;
            }
            StringBuilder nameSB = new StringBuilder();
            foreach (ITypeInfoItem member in getDelegates)
            {
                nameSB.Append(member.Name);
                nameSB.Append('.');
            }
            nameSB.Append(lastDelegate.Name);
            return new EmbeddedTypeInfoItem(nameSB.ToString(), lastDelegate, getDelegates);
        }

        public override void SetObjRefs(Object obj, IObjRef[] objRefs)
        {
            setObjRefs(obj, objRefs);
        }

        public override void SetUninitialized(Object obj, IObjRef[] objRefs)
        {
            setState(obj, ValueHolderState.LAZY);
            setObjRefs(obj, objRefs);
            setDirectValue(obj, null);
        }

        public override void SetInitialized(Object obj, Object value)
        {
            //setState(obj, ValueHolderState.INIT);
            //setObjRefs(obj, null);
            //setDirectValue(obj, value);
            member.SetValue(obj, value);
        }

        public override IObjRef[] GetObjRefs(Object obj)
        {
            return (IObjRef[])getObjRefs(obj);
        }

        public override object GetValueDirect(Object obj)
        {
            return getDirectValue(obj);
        }

        public override ValueHolderState GetState(Object obj)
        {
            return (ValueHolderState)getState(obj);
        }

        public override void SetState(Object obj, ValueHolderState state)
        {
            setState(obj, state);
        }
    }

    public class ValueHolderIEC : SmartCopyMap<Type, Type>, IEntityChecker, IProxyHelper, IInitializingBean
    {
        public static String GetObjRefsFieldName(String propertyName)
        {
            return propertyName + "_ObjRefs";
        }

        public static String GetInitializedFieldName(String propertyName)
        {
            return propertyName + "_State";
        }

        public static String GetSetterNameOfRelationPropertyWithNoInit(String propertyName)
        {
            return "set_" + propertyName + GetNoInitSuffix();
        }

        public static String GetGetterNameOfRelationPropertyWithNoInit(String propertyName)
        {
            return "get_" + propertyName + GetNoInitSuffix();
        }

        public static String GetNoInitSuffix()
        {
            return "_NoInit";
        }

        protected readonly SmartCopyMap<Type, ValueHolderContainerEntry> typeToVhcEntryMap = new SmartCopyMap<Type, ValueHolderContainerEntry>(0.5f);

        [Autowired(Optional = true)]
        public IBytecodeEnhancer BytecodeEnhancer { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IObjRefHelper OriHelper { protected get; set; }

        [Autowired(Optional = true)]
        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            // Intended blank
        }

        public ValueHolderContainerEntry GetVhcEntry(Type targetType)
        {
            ValueHolderContainerEntry vhcEntry = typeToVhcEntryMap.Get(targetType);
            if (vhcEntry == null)
            {
                IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(targetType);
                vhcEntry = new ValueHolderContainerEntry(targetType, metaData.RelationMembers, BytecodeEnhancer, PropertyInfoProvider);
                typeToVhcEntryMap.Put(targetType, vhcEntry);
            }
            return vhcEntry;
        }

        protected ValueHolderContainerEntry GetVhcEntry(Object parentObj)
        {
            if (!(parentObj is IValueHolderContainer))
            {
                return null;
            }
            return GetVhcEntry(parentObj.GetType());
        }

        public bool IsInitialized(Object parentObj, String memberName)
        {
            IRelationInfoItem member = (IRelationInfoItem)EntityMetaDataProvider.GetMetaData(parentObj.GetType()).GetMemberByName(memberName);
            return IsInitialized(parentObj, member);
        }

        public bool IsInitialized(Object parentObj, IRelationInfoItem member)
        {
            ValueHolderContainerEntry vhcEntry = GetVhcEntry(parentObj);
            return ValueHolderState.INIT == vhcEntry.GetState(parentObj, member);
        }

        public ValueHolderState GetState(Object parentObj, IRelationInfoItem member)
        {
            ValueHolderContainerEntry vhcEntry = GetVhcEntry(parentObj);
            if (vhcEntry == null)
            {
                return ValueHolderState.INIT;
            }
            return vhcEntry.GetState(parentObj, member);
        }

        public IObjRef[] GetObjRefs(Object parentObj, String memberName)
        {
            IRelationInfoItem member = (IRelationInfoItem)EntityMetaDataProvider.GetMetaData(parentObj.GetType()).GetMemberByName(memberName);
            return GetObjRefs(parentObj, member);
        }

        public IObjRef[] GetObjRefs(Object parentObj, IRelationInfoItem member)
        {
            ValueHolderContainerEntry vhcEntry = GetVhcEntry(parentObj);
            return vhcEntry.GetObjRefs(parentObj, member);
        }

        public void SetUninitialized(Object parentObj, IRelationInfoItem member, IObjRef[] objRefs)
        {
            ValueHolderContainerEntry vhcEntry = GetVhcEntry(parentObj);
            vhcEntry.SetUninitialized(parentObj, member, objRefs);
            // This may fire a property change listener, so it is important to set this AFTER if resetted the value holder above
        }

        public void SetInitialized(Object parentObj, IRelationInfoItem member, Object value)
        {
            ValueHolderContainerEntry vhcEntry = GetVhcEntry(parentObj);
            vhcEntry.SetInitialized(parentObj, member, value);
        }

        public void SetObjRefs(Object parentObj, IRelationInfoItem member, IObjRef[] objRefs)
        {
            ValueHolderContainerEntry vhcEntry = GetVhcEntry(parentObj);
            vhcEntry.SetObjRefs(parentObj, member, objRefs);
        }

        public EntityTypeResult CheckForEntityType(Type entityType)
        {
            if (typeof(ILoadContainer).IsAssignableFrom(entityType))
            {
                return EntityTypeResult.ENTITY_TRUE;
            }
            return EntityTypeResult.UNDEFINED;
        }

        public Type GetRealType(Type type)
        {
            Type realType = Get(type);
            if (realType != null)
            {
                return realType;
            }
            IBytecodeEnhancer bytecodeEnhancer = this.BytecodeEnhancer;
            if (bytecodeEnhancer != null)
            {
                realType = bytecodeEnhancer.GetBaseType(type);
            }
            if (realType == null)
            {
                if (typeof(IProxyTargetAccessor).IsAssignableFrom(type))
                {
                    realType = type.BaseType;
                }
            }
            if (realType == null)
            {
                realType = type;
            }
            Put(type, realType);
            return realType;
        }

        public Object GetValueDirect(Object parentObj, IRelationInfoItem member)
        {
            ValueHolderContainerEntry vhcEntry = GetVhcEntry(parentObj);
            return vhcEntry.GetValueDirect(parentObj, member);
        }

        public void SetInitPending(Object parentObj, IRelationInfoItem member)
        {
            ValueHolderContainerEntry vhcEntry = GetVhcEntry(parentObj);
            vhcEntry.SetState(parentObj, member, ValueHolderState.PENDING);
        }

        public bool GetInitPending(Object parentObj, IRelationInfoItem member)
        {
            ValueHolderContainerEntry vhcEntry = GetVhcEntry(parentObj);
            return ValueHolderState.PENDING.Equals(vhcEntry.GetState(parentObj, member));
        }

        public void SetValueDirect(Object parentObj, IRelationInfoItem member, Object value)
        {
            member.SetValue(parentObj, value);
        }

        public bool ObjectEquals(Object leftObject, Object rightObject)
        {
            if (leftObject == null)
            {
                return (rightObject == null);
            }
            if (rightObject == null)
            {
                return false;
            }
            if (leftObject == rightObject)
            {
                return true;
            }
            Type leftType = leftObject.GetType(), rightType = rightObject.GetType();
            if (!leftType.Equals(rightType))
            {
                // Real entity types are not equal
                return false;
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(leftType);
            Object leftId = metaData.IdMember.GetValue(leftObject, false);
            Object rightId = metaData.IdMember.GetValue(rightObject, false);
            if (leftId == null || rightId == null)
            {
                // Entities are never equal with anything beside themselves if they do not have a persistent id
                return false;
            }
            return Object.Equals(leftId, rightId);
        }
    }
}
