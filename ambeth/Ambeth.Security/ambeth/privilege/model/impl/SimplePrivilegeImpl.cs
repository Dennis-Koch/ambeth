using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public class SimplePrivilegeImpl : AbstractPrivilege
    {
        protected readonly bool create, read, update, delete, execute;

        protected readonly IPropertyPrivilege defaultPropertyPrivileges;

        public SimplePrivilegeImpl(bool create, bool read, bool update, bool delete, bool execute, IPropertyPrivilege defaultPropertyPrivileges)
            : base(create, read, update, delete, execute, null, null)
        {
            this.create = create;
            this.read = read;
            this.update = update;
            this.delete = delete;
            this.execute = execute;
            this.defaultPropertyPrivileges = defaultPropertyPrivileges;
        }

        public override IPropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex)
        {
            return defaultPropertyPrivileges;
        }

        public override IPropertyPrivilege getRelationPropertyPrivilege(int relationIndex)
        {
            return defaultPropertyPrivileges;
        }

        public override IPropertyPrivilege getDefaultPropertyPrivilegeIfValid()
        {
            return defaultPropertyPrivileges;
        }

        public override bool CreateAllowed
        {
            get { return create; }
        }

        public override bool ReadAllowed
        {
            get { return read; }
        }

        public override bool UpdateAllowed
        {
            get { return update; }
        }

        public override bool DeleteAllowed
        {
            get { return delete; }
        }

        public override bool ExecuteAllowed
        {
            get { return execute; }
        }
    }
}