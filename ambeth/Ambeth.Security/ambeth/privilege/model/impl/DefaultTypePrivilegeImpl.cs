namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public class DefaultTypePrivilegeImpl : AbstractTypePrivilege
    {
        protected readonly bool? read, create, update, delete, execute;

        protected readonly ITypePropertyPrivilege[] primitivePropertyPrivileges;

        protected readonly ITypePropertyPrivilege[] relationPropertyPrivileges;

        public DefaultTypePrivilegeImpl(bool? create, bool? read, bool? update, bool? delete, bool? execute,
                ITypePropertyPrivilege[] primitivePropertyPrivileges, ITypePropertyPrivilege[] relationPropertyPrivileges)
            : base(create, read, update, delete, execute, primitivePropertyPrivileges, relationPropertyPrivileges)
        {
            this.create = create;
            this.read = read;
            this.update = update;
            this.delete = delete;
            this.execute = execute;
            this.primitivePropertyPrivileges = primitivePropertyPrivileges;
            this.relationPropertyPrivileges = relationPropertyPrivileges;
        }

        public override ITypePropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex)
        {
            return primitivePropertyPrivileges[primitiveIndex];
        }

        public override ITypePropertyPrivilege getRelationPropertyPrivilege(int relationIndex)
        {
            return relationPropertyPrivileges[relationIndex];
        }

        public override ITypePropertyPrivilege getDefaultPropertyPrivilegeIfValid()
        {
            return null;
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