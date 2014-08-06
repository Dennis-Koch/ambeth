using System;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public class DefaultTypePrivilegeSimpleImpl : AbstractTypePrivilege
    {
        protected readonly bool? read, create, update, delete, execute;

        protected readonly ITypePropertyPrivilege propertyPrivilege;

        public DefaultTypePrivilegeSimpleImpl(bool? create, bool? read, bool? update, bool? delete, bool? execute,
                ITypePropertyPrivilege propertyPrivilege)
            : base(create, read, update, delete, execute, null, null)
        {
            this.create = create;
            this.read = read;
            this.update = update;
            this.delete = delete;
            this.execute = execute;
            this.propertyPrivilege = propertyPrivilege;
        }

        public override ITypePropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex)
        {
            return propertyPrivilege;
        }

        public override ITypePropertyPrivilege getRelationPropertyPrivilege(int relationIndex)
        {
            return propertyPrivilege;
        }

        public override ITypePropertyPrivilege getDefaultPropertyPrivilegeIfValid()
        {
            return propertyPrivilege;
        }

        public override bool? CreateAllowed
        {
            get { return create; }
        }

        public override bool? ReadAllowed
        {
            get { return read; }
        }

        public override bool? UpdateAllowed
        {
            get { return update; }
        }

        public override bool? DeleteAllowed
        {
            get { return delete; }
        }

        public override bool? ExecuteAllowed
        {
            get { return execute; }
        }
    }
}