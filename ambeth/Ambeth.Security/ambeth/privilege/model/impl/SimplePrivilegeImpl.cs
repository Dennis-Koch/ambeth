using De.Osthus.Ambeth.Privilege.Transfer;
using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public sealed class SimplePrivilegeImpl : AbstractPrivilege
    {
        private static readonly SimplePrivilegeImpl[] array = new SimplePrivilegeImpl[ArraySizeForIndex()];

	    static SimplePrivilegeImpl()
	    {
		    Put1();
	    }

	    private static void Put1()
	    {
		    Put2(true);
		    Put2(false);
	    }

	    private static void Put2(bool create)
	    {
		    Put3(create, true);
		    Put3(create, false);
	    }

	    private static void Put3(bool create, bool read)
	    {
		    Put4(create, read, true);
		    Put4(create, read, false);
	    }

	    private static void Put4(bool create, bool read, bool update)
	    {
		    Put5(create, read, update, true);
		    Put5(create, read, update, false);
	    }

	    private static void Put5(bool create, bool read, bool update, bool delete)
	    {
		    Put(create, read, update, delete, true);
		    Put(create, read, update, delete, false);
	    }

	    public static new int ArraySizeForIndex()
	    {
		    return 1 << 5;
	    }

	    private static void Put(bool create, bool read, bool update, bool delete, bool execute)
	    {
		    IPropertyPrivilege propertyPrivilege = PropertyPrivilegeImpl.Create(create, read, update, delete);
		    int index = CalcIndex(create, read, update, delete, execute);
		    array[index] = new SimplePrivilegeImpl(create, read, update, delete, execute, propertyPrivilege);
	    }

	    public static IPrivilege CreateFrom(IPrivilegeOfService privilegeOfService)
	    {
		    return Create(privilegeOfService.CreateAllowed, privilegeOfService.ReadAllowed, privilegeOfService.UpdateAllowed,
				    privilegeOfService.DeleteAllowed, privilegeOfService.ExecuteAllowed);
	    }

	    public static IPrivilege Create(bool create, bool read, bool update, bool delete, bool execute)
	    {
		    int index = CalcIndex(create, read, update, delete, execute);
		    return array[index];
	    }

        private readonly bool create, read, update, delete, execute;

        private readonly IPropertyPrivilege defaultPropertyPrivileges;

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

        public override IPropertyPrivilege GetPrimitivePropertyPrivilege(int primitiveIndex)
        {
            return defaultPropertyPrivileges;
        }

        public override IPropertyPrivilege GetRelationPropertyPrivilege(int relationIndex)
        {
            return defaultPropertyPrivileges;
        }

        public override IPropertyPrivilege GetDefaultPropertyPrivilegeIfValid()
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