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
        protected readonly AbstractValueHolderEntry2[] entries;

        public ValueHolderContainerEntry(Type targetType, RelationMember[] members, IBytecodeEnhancer bytecodeEnhancer,
            IPropertyInfoProvider propertyInfoProvider, IMemberTypeProvider memberTypeProvider)
        {
            entries = new AbstractValueHolderEntry2[members.Length];
            try
            {
                for (int relationIndex = members.Length; relationIndex-- > 0; )
                {
                    RelationMember member = members[relationIndex];
                    AbstractValueHolderEntry2 vhEntry = new AbstractValueHolderEntry2(targetType, member, bytecodeEnhancer, propertyInfoProvider, memberTypeProvider);
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

    public class AbstractValueHolderEntry2 : AbstractValueHolderEntry
    {
        protected readonly String memberName;

        protected readonly MemberGetDelegate getState;

        protected readonly MemberSetDelegate setState;

        protected readonly MemberGetDelegate getObjRefs;

        protected readonly MemberSetDelegate setObjRefs;

        protected readonly MemberGetDelegate getDirectValue;

        protected readonly MemberSetDelegate setDirectValue;

        protected readonly Member objRefs;

        protected readonly Member state;

        protected readonly Member directValue;

        protected readonly RelationMember member;

        public AbstractValueHolderEntry2(Type targetType, RelationMember member, IBytecodeEnhancer bytecodeEnhancer, IPropertyInfoProvider propertyInfoProvider,
            IMemberTypeProvider memberTypeProvider)
        {
            this.member = member;
            this.memberName = member.Name;
            String lastPropertyName = memberName;
            Type currType = targetType;
            String prefix = "";

            if (member is IEmbeddedMember)
			{
				IEmbeddedMember embeddedMember = (IEmbeddedMember) member;
				lastPropertyName = embeddedMember.ChildMember.Name;
				GetMemberDelegate(targetType, embeddedMember, out currType, bytecodeEnhancer, propertyInfoProvider, memberTypeProvider);

				prefix = embeddedMember.GetMemberPathString() + ".";
			}
			state = memberTypeProvider.getMember(targetType, prefix + ValueHolderIEC.GetInitializedFieldName(lastPropertyName));
			objRefs = memberTypeProvider.getMember(targetType, prefix + ValueHolderIEC.GetObjRefsFieldName(lastPropertyName));
			directValue = memberTypeProvider.getMember(targetType, prefix + lastPropertyName + ValueHolderIEC.GetNoInitSuffix());
        }

        protected Member[] GetMemberDelegate(Type targetType,  IEmbeddedMember member,
            out Type currTypeOut, IBytecodeEnhancer bytecodeEnhancer, IPropertyInfoProvider propertyInfoProvider, IMemberTypeProvider memberTypeProvider)
        {
            Type currType = targetType;
			Type parentObjectType = targetType;
			String embeddedPath = "";
			String[] memberPath = member.GetMemberPathToken();
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
				MethodInfo mi = ReflectUtil.GetDeclaredMethod(true, currType, null, memberItem, new Type[0]);
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
			return member.GetMemberPath();
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
            member.SetValue(obj, value);
        }

        public override void SetInitPending(Object obj)
        {
            setState(obj, ValueHolderState.PENDING);
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

    public class ValueHolderIEC : SmartCopyMap<Type, Type>, IProxyHelper, IInitializingBean
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
