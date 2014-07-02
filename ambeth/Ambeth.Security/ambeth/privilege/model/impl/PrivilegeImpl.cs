using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public class PrivilegeImpl : IPrivilege, IPrintable
    {
        public static readonly String[] EMPTY_PROPERTY_NAMES = new String[0];

        protected readonly bool read, create, update, delete, execute;

        protected readonly IMap<String, IPropertyPrivilege> propertyPrivilegeMap;

        protected readonly String[] propertyNames;

        public PrivilegeImpl(bool read, bool create, bool update, bool delete, bool execute)
            : this(read, create, update, delete, execute, EmptyMap<String, IPropertyPrivilege>.Empty(), EMPTY_PROPERTY_NAMES)
        {
            // intended blank
        }

        public PrivilegeImpl(bool read, bool create, bool update, bool delete, bool execute,
                IMap<String, IPropertyPrivilege> propertyPrivilegeMap, String[] propertyNames)
        {
            this.read = read;
            this.create = create;
            this.update = update;
            this.delete = delete;
            this.execute = execute;
            this.propertyPrivilegeMap = propertyPrivilegeMap;
            this.propertyNames = propertyNames;
        }

        public void SetPropertyPrivilege(String propertyName, IPropertyPrivilege propertyPrivilege)
        {
            propertyPrivilegeMap.Put(propertyName, propertyPrivilege);
        }

        public IPropertyPrivilege GetPropertyPrivilege(String propertyName)
        {
            return propertyPrivilegeMap.Get(propertyName);
        }

        public bool CreateAllowed
        {
            get
            {
                return create;
            }
        }

        public bool ReadAllowed
        {
            get
            {
                return read;
            }
        }

        public bool UpdateAllowed
        {
            get
            {
                return update;
            }
        }

        public bool DeleteAllowed
        {
            get
            {
                return delete;
            }
        }

        public bool ExecutionAllowed
        {
            get
            {
                return execute;
            }
        }

        public String[] ConfiguredPropertyNames
        {
            get
            {
                return propertyNames;
            }
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append(ReadAllowed ? "+R" : "-R");
            sb.Append(CreateAllowed ? "+C" : "-C");
            sb.Append(UpdateAllowed ? "+U" : "-U");
            sb.Append(DeleteAllowed ? "+D" : "-D");
            sb.Append(ExecutionAllowed ? "+X" : "-X");

            foreach (String configuredPropertyName in ConfiguredPropertyNames)
            {
                IPropertyPrivilege propertyPrivilege = propertyPrivilegeMap.Get(configuredPropertyName);
                StringBuilderUtil.AppendPrintable(sb, propertyPrivilege);
            }
        }
    }
}