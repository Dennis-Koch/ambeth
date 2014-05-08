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
    public class CollectionProxy
    {
        public interface IMember
        {
            String Name { get; }
        }

        [DebuggerDisplay("{Length}", Name = "{Name,nq}", Type = "{Type.ToString(),nq}")]
        public class Member : IMember
        {
            [DebuggerBrowsable(DebuggerBrowsableState.Never)]
            public String Name { get; set; }

            [DebuggerBrowsable(DebuggerBrowsableState.RootHidden)]
            public Object Value;

            [DebuggerBrowsable(DebuggerBrowsableState.Never)]
            public int Length;

            [DebuggerBrowsable(DebuggerBrowsableState.Never)]
            public Type Type;

            public Member(String name, Object value, int length, Type type)
            {
                Name = name;
                Value = value;
                Length = length;
                Type = type;
            }
        }
        
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

        public CollectionProxy(Object target)
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
            Type currType = _target.GetType();
            PropertyInfo p_count = currType.GetProperty("Count");
            Object value = p_count.GetValue(_target, null);
            list.Add(new Member(p_count.Name, _target, (int) value, p_count.PropertyType));
            return list.OrderBy(m => m.Name).ToList();
        }
    }
}
