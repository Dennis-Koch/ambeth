using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Reflection;
using System.Text;

namespace De.Osthus.Ambeth.Debug
{
    public class ValueHolderFlattenHierarchyProxy : FlattenHierarchyProxy
    {
        [DebuggerDisplay("{State}: {Value.Length}", Name = "{Name,nq}", Type = "{Type.ToString(),nq}")]
        public class LazyMember : IMember
        {
            [DebuggerBrowsable(DebuggerBrowsableState.Never)]
            public String Name { get; set; }

            [DebuggerBrowsable(DebuggerBrowsableState.Never)]
            public ValueHolderState State;

            [DebuggerBrowsable(DebuggerBrowsableState.RootHidden)]
            public IObjRef[] Value;

            [DebuggerBrowsable(DebuggerBrowsableState.Never)]
            public Type Type;

            public LazyMember(String name, ValueHolderState state, IObjRef[] value, Type type)
            {
                Name = name;
                State = state;
                Value = value;
                Type = type;
            }
        }

        [DebuggerDisplay("{State}", Name = "{Name,nq}", Type = "{Type.ToString(),nq}")]
        public class LazyUnknownMember : IMember
        {
            [DebuggerBrowsable(DebuggerBrowsableState.Never)]
            public String Name { get; set; }

            [DebuggerBrowsable(DebuggerBrowsableState.Never)]
            public ValueHolderState State;

            [DebuggerBrowsable(DebuggerBrowsableState.Never)]
            public Type Type;

            public LazyUnknownMember(String name, ValueHolderState state, Type type)
            {
                Name = name;
                State = state;
                Type = type;
            }
        }

        public ValueHolderFlattenHierarchyProxy(Object target) : base(target)
        {
            // Intended blank
        }

        protected override List<IMember> BuildMemberList()
        {
            List<IMember> list = new List<IMember>();
            if (_target == null)
            {
                return list;
            }
            if (Context == null)
            {
                return base.BuildMemberList();
            }
            IEntityMetaDataProvider entityMetaDataProvider = Context.GetService<IEntityMetaDataProvider>();

            Type type = _target.GetType();
            IEntityMetaData metaData = entityMetaDataProvider.GetMetaData(type, true);
            if (metaData == null)
            {
                return base.BuildMemberList();
            }
            HashSet<String> suppressedPropertyNames = new HashSet<String>();
            foreach (IRelationInfoItem member in metaData.RelationMembers)
            {
                suppressedPropertyNames.Add(ValueHolderIEC.GetObjRefsFieldName(member.Name));
                suppressedPropertyNames.Add(ValueHolderIEC.GetInitializedFieldName(member.Name));
            }
            HashMap<String, IRelationInfoItem> nameToRelationMap = new HashMap<String, IRelationInfoItem>();
            foreach (IRelationInfoItem member in metaData.RelationMembers)
            {
                nameToRelationMap.Put(member.Name + ValueHolderIEC.GetNoInitSuffix(), member);
            }
            ITypeInfoItem[] members = Context.GetService<ITypeInfoProvider>().GetTypeInfo(type).Members;
            foreach (ITypeInfoItem member in members)
            {
                if (!member.CanRead)
                {
                    continue;
                }
                DebuggerBrowsableAttribute att = member.GetAnnotation<DebuggerBrowsableAttribute>();
                if (att != null && att.State == DebuggerBrowsableState.Never)
                {
                    continue;
                }
                String propertyName = member.Name;
                if (suppressedPropertyNames.Contains(propertyName))
                {
                    continue;
                }
                IRelationInfoItem relMember = nameToRelationMap.Get(propertyName);
                Object value = null;
                if (relMember != null)
                {
                    propertyName = relMember.Name;
                    int relationIndex = metaData.GetIndexByRelationName(propertyName);
                    ValueHolderState state = ((IObjRefContainer)_target).Get__State(relationIndex);
                    if (!ValueHolderState.INIT.Equals(state))
                    {
                        IObjRef[] objRefs = ((IObjRefContainer)_target).Get__ObjRefs(relationIndex);
                        if (objRefs == null)
                        {
                            list.Add(new LazyUnknownMember(propertyName, state, member.RealType));
                        }
                        else
                        {
                            list.Add(new LazyMember(propertyName, state, objRefs, member.RealType));
                        }
                        continue;
                    }
                }
                if (value == null)
                {
                    try
                    {
                        value = member.GetValue(_target);
                    }
                    catch (Exception ex)
                    {
                        value = ex;
                    }
                }
                list.Add(new FHPMember(propertyName, value, member.RealType));
            }
            return list.OrderBy(m => m.Name).ToList();
        }
    }
}
