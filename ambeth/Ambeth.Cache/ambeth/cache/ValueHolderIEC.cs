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
using De.Osthus.Ambeth.Metadata;

namespace De.Osthus.Ambeth.Cache
{
    public class ValueHolderContainerEntry
    {
        protected readonly ValueHolderEntry[] entries;

        public ValueHolderContainerEntry(Type targetType, RelationMember[] members, IBytecodeEnhancer bytecodeEnhancer,
            IPropertyInfoProvider propertyInfoProvider, IMemberTypeProvider memberTypeProvider)
        {
            entries = new ValueHolderEntry[members.Length];
            try
            {
                for (int relationIndex = members.Length; relationIndex-- > 0; )
                {
                    RelationMember member = members[relationIndex];
                    ValueHolderEntry vhEntry = new ValueHolderEntry(targetType, member, bytecodeEnhancer, propertyInfoProvider, memberTypeProvider);
                    entries[relationIndex] = vhEntry;
                }
            }
            catch (Exception e)
            {
                throw RuntimeExceptionUtil.Mask(e, "Error occured while processing type '" + targetType.FullName + "'");
            }
        }

        public void SetUninitialized(Object obj, int relationIndex, IObjRef[] objRefs)
        {
            entries[relationIndex].SetUninitialized(obj, objRefs);
        }

        public void SetInitialized(Object obj, int relationIndex, Object value)
        {
            entries[relationIndex].SetInitialized(obj, value);
        }

        public void SetInitPending(Object obj, int relationIndex)
        {
            entries[relationIndex].SetInitPending(obj);
        }

        public IObjRef[] GetObjRefs(Object obj, int relationIndex)
        {
            return entries[relationIndex].GetObjRefs(obj);
        }

        public void SetObjRefs(Object obj, int relationIndex, IObjRef[] objRefs)
        {
            entries[relationIndex].SetObjRefs(obj, objRefs);
        }

        public Object GetValueDirect(Object obj, int relationIndex)
        {
            return entries[relationIndex].GetValueDirect(obj);
        }

        public void SetValueDirect(Object obj, int relationIndex, Object value)
        {
            entries[relationIndex].SetInitialized(obj, value);
        }

        public bool IsInitialized(Object obj, int relationIndex)
        {
            return ValueHolderState.INIT == GetState(obj, relationIndex);
        }

        public ValueHolderState GetState(Object obj, int relationIndex)
        {
            return entries[relationIndex].GetState(obj);
        }

        public void SetState(Object obj, int relationIndex, ValueHolderState state)
        {
            entries[relationIndex].SetState(obj, state);
        }
    }

    public abstract class AbstractValueHolderEntry
    {
        public abstract void SetObjRefs(Object obj, IObjRef[] objRefs);

        public abstract void SetUninitialized(Object obj, IObjRef[] objRefs);

        public abstract void SetInitialized(Object obj, Object value);

        public abstract void SetInitPending(Object obj);

        public abstract IObjRef[] GetObjRefs(Object obj);

        public abstract Object GetValueDirect(Object obj);

        public abstract ValueHolderState GetState(Object obj);

        public abstract void SetState(Object obj, ValueHolderState state);
    }

    public class ValueHolderEntry : AbstractValueHolderEntry
    {
        protected readonly String memberName;

        protected readonly Member objRefs;

        protected readonly Member state;

        protected readonly Member directValue;

        protected readonly RelationMember member;

        public ValueHolderEntry(Type targetType, RelationMember member, IBytecodeEnhancer bytecodeEnhancer, IPropertyInfoProvider propertyInfoProvider,
            IMemberTypeProvider memberTypeProvider)
        {
            this.member = member;
            this.memberName = member.Name;
            String lastPropertyName = memberName;
            String prefix = "";

            if (member is IEmbeddedMember)
			{
				IEmbeddedMember embeddedMember = (IEmbeddedMember) member;
				lastPropertyName = embeddedMember.ChildMember.Name;

				prefix = embeddedMember.GetMemberPathString() + ".";
			}
			state = memberTypeProvider.GetMember(targetType, prefix + ValueHolderIEC.GetInitializedFieldName(lastPropertyName));
			objRefs = memberTypeProvider.GetMember(targetType, prefix + ValueHolderIEC.GetObjRefsFieldName(lastPropertyName));
			directValue = memberTypeProvider.GetMember(targetType, prefix + lastPropertyName + ValueHolderIEC.GetNoInitSuffix());
        }

        public override void SetObjRefs(Object obj, IObjRef[] objRefs)
        {
            this.objRefs.SetValue(obj, objRefs);
        }

        public override void SetUninitialized(Object obj, IObjRef[] objRefs)
        {
            this.state.SetValue(obj, ValueHolderState.LAZY);
            this.objRefs.SetValue(obj, objRefs);
            this.directValue.SetValue(obj, null);
        }

        public override void SetInitialized(Object obj, Object value)
        {
            member.SetValue(obj, value);
        }

        public override void SetInitPending(Object obj)
        {
            this.state.SetValue(obj, ValueHolderState.PENDING);
        }

        public override IObjRef[] GetObjRefs(Object obj)
        {
            return (IObjRef[])this.objRefs.GetValue(obj);
        }

        public override object GetValueDirect(Object obj)
        {
            return this.directValue.GetValue(obj);
        }

        public override ValueHolderState GetState(Object obj)
        {
            return (ValueHolderState)this.state.GetValue(obj);
        }

        public override void SetState(Object obj, ValueHolderState state)
        {
            this.state.SetValue(obj, state);
        }
    }

    public class ValueHolderIEC : SmartCopyMap<Type, Type>, IProxyHelper, IInitializingBean
    {
        public static String GetObjRefsFieldName(String propertyName)
        {
            return propertyName + "$ObjRefs";
        }

        public static String GetInitializedFieldName(String propertyName)
        {
            return propertyName + "$State";
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
        public IMemberTypeProvider MemberTypeProvider { protected get; set; }

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
                vhcEntry = new ValueHolderContainerEntry(targetType, metaData.RelationMembers, BytecodeEnhancer, PropertyInfoProvider, MemberTypeProvider);
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
