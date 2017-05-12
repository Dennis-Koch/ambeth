namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public class DefaultPrivilegeImpl : AbstractPrivilege
    {
        protected readonly bool read, create, update, delete, execute;

        protected readonly IPropertyPrivilege[] primitivePropertyPrivileges;

        protected readonly IPropertyPrivilege[] relationPropertyPrivileges;

        public DefaultPrivilegeImpl(bool create, bool read, bool update, bool delete, bool execute,
                IPropertyPrivilege[] primitivePropertyPrivileges, IPropertyPrivilege[] relationPropertyPrivileges)
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

        public override IPropertyPrivilege GetPrimitivePropertyPrivilege(int primitiveIndex)
        {
            return primitivePropertyPrivileges[primitiveIndex];
        }

        public override IPropertyPrivilege GetRelationPropertyPrivilege(int relationIndex)
        {
            return relationPropertyPrivileges[relationIndex];
        }

        public override IPropertyPrivilege GetDefaultPropertyPrivilegeIfValid()
        {
            return null;
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