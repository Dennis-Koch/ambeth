using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Reflection;
using System.Text;

namespace De.Osthus.Ambeth.Debug
{
    /// <summary>
    /// Based on http://blogs.msdn.com/b/jaredpar/archive/2010/02/19/flattening-class-hierarchies-when-debugging-c.aspx
    /// by Jared Par.
    /// Extended by Dennis Koch.
    /// </summary>
    public class FlattenHierarchyProxy
    {
        public interface IMember
        {
            String Name { get; }
        }

        [DebuggerDisplay("{Value}", Name = "{Name,nq}", Type = "{Type.ToString(),nq}")]
        public class Member : IMember
        {
            [DebuggerBrowsable(DebuggerBrowsableState.Never)]
            public String Name { get; set; }

            [DebuggerBrowsable(DebuggerBrowsableState.RootHidden)]
            public Object Value;

            [DebuggerBrowsable(DebuggerBrowsableState.Never)]
            public Type Type;

            public Member(String name, Object value, Type type)
            {
                Name = name;
                Value = value;
                Type = type;
            }
        }

        public static IServiceContext Context { get; set; }

        [DebuggerBrowsable(DebuggerBrowsableState.Never)]
        protected readonly Object _target;
        [DebuggerBrowsable(DebuggerBrowsableState.Never)]
        protected Object[] _memberList;

        [DebuggerBrowsable(DebuggerBrowsableState.RootHidden)]
        public Object[] Items
        {
            get
            {
                if (_memberList == null)
                {
                    _memberList = BuildMemberList().ToArray();
                }
                return _memberList;
            }
        }

        public FlattenHierarchyProxy(Object target)
        {
            _target = target;
        }

        protected virtual List<IMember> BuildMemberList()
        {
            List<IMember> list = new List<IMember>();
            if (_target == null)
            {
                return list;
            }
            if (Context != null)
            {
                ITypeInfoItem[] members = Context.GetService<ITypeInfoProvider>().GetTypeInfo(_target.GetType()).Members;
                foreach (ITypeInfoItem member in members)
                {
                    DebuggerBrowsableAttribute att = member.GetAnnotation<DebuggerBrowsableAttribute>();
                    if (att != null && att.State == DebuggerBrowsableState.Never)
                    {
                        continue;
                    }
                    if (!member.CanRead)
                    {
                        continue;
                    }
                    Object value;
                    try
                    {
                        value = member.GetValue(_target);
                    }
                    catch (Exception ex)
                    {
                        value = ex;
                    }
                    list.Add(new Member(member.Name, value, member.RealType));
                }
            }
            else
            {
                var flags = BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Instance | BindingFlags.FlattenHierarchy;
                var type = _target.GetType();
                Type currType = type;
                while (currType != null && !typeof(Object).Equals(currType))
                {
                    foreach (var field in currType.GetFields(flags))
                    {
                        var debuggerBrowsableAtts = field.GetCustomAttributes(typeof(DebuggerBrowsableAttribute), true);
                        if (debuggerBrowsableAtts.Count() == 1)
                        {
                            var att = debuggerBrowsableAtts[0] as DebuggerBrowsableAttribute;
                            if (att.State == DebuggerBrowsableState.Never)
                            {
                                continue;
                            }
                        }
                        if (field.Name.EndsWith("k__BackingField"))
                        {
                            continue;
                        }
                        var value = field.GetValue(_target);
                        list.Add(new Member(field.Name, value, field.FieldType));
                    }
                    foreach (var prop in currType.GetProperties(flags))
                    {
                        var debuggerBrowsableAtts = prop.GetCustomAttributes(typeof(DebuggerBrowsableAttribute), true);
                        if (debuggerBrowsableAtts.Count() == 1)
                        {
                            var att = debuggerBrowsableAtts[0] as DebuggerBrowsableAttribute;
                            if (att.State == DebuggerBrowsableState.Never)
                            {
                                continue;
                            }
                        }
                        Object value = null;
                        try
                        {
                            value = prop.GetValue(_target, null);
                        }
                        catch (Exception ex)
                        {
                            value = ex;
                        }
                        list.Add(new Member(prop.Name, value, prop.PropertyType));
                    }
                    currType = currType.BaseType;
                }
            }
            return list.OrderBy(m => m.Name).ToList();
        }
    }
}
