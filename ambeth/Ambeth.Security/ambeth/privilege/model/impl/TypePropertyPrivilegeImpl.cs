using De.Osthus.Ambeth.Privilege.Transfer;
using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public sealed class TypePropertyPrivilegeImpl : ITypePropertyPrivilege, IPrintable, IImmutableType
    {
        public static ITypePropertyPrivilege[] EMPTY_PROPERTY_PRIVILEGES = new ITypePropertyPrivilege[0];

        private static TypePropertyPrivilegeImpl[] array = new TypePropertyPrivilegeImpl[ArraySizeForIndex()];

        static TypePropertyPrivilegeImpl()
        {
            Put1();
        }

        private static void Put1()
        {
            Put2(null);
            Put2(false);
            Put2(true);
        }

        private static void Put2(bool? create)
        {
            Put3(create, null);
            Put3(create, false);
            Put3(create, true);
        }

        private static void Put3(bool? create, bool? read)
        {
            Put4(create, read, null);
            Put4(create, read, false);
            Put4(create, read, true);
        }

        private static void Put4(bool? create, bool? read, bool? update)
        {
            Put(create, read, update, null);
            Put(create, read, update, false);
            Put(create, read, update, true);
        }

        public static int ArraySizeForIndex()
        {
            return 1 << 7;
        }

        public static int CalcIndex(bool? create, bool? read, bool? update, bool? delete)
        {
            return AbstractTypePrivilege.ToBitValue(create, 1, 1 * 2) + AbstractTypePrivilege.ToBitValue(read, 3, 3 * 2)
                    + AbstractTypePrivilege.ToBitValue(update, 9, 9 * 2) + AbstractTypePrivilege.ToBitValue(delete, 27, 27 * 2);
        }

        private static void Put(bool? create, bool? read, bool? update, bool? delete)
        {
            int index = CalcIndex(create, read, update, delete);
            array[index] = new TypePropertyPrivilegeImpl(create, read, update, delete);
        }

        public static ITypePropertyPrivilege Create(bool? create, bool? read, bool? update, bool? delete)
        {
            int index = CalcIndex(create, read, update, delete);
            return array[index];
        }

        public static ITypePropertyPrivilege CreateFrom(ITypePrivilege privilegeAsTemplate)
        {
            return Create(privilegeAsTemplate.CreateAllowed, privilegeAsTemplate.ReadAllowed, privilegeAsTemplate.UpdateAllowed,
                    privilegeAsTemplate.DeleteAllowed);
        }

        public static ITypePropertyPrivilege CreateFrom(ITypePrivilegeOfService privilegeOfService)
        {
            return Create(privilegeOfService.CreateAllowed, privilegeOfService.ReadAllowed, privilegeOfService.UpdateAllowed,
                    privilegeOfService.DeleteAllowed);
        }

        public static ITypePropertyPrivilege CreateFrom(ITypePropertyPrivilegeOfService propertyPrivilegeResult)
        {
            return Create(propertyPrivilegeResult.CreateAllowed, propertyPrivilegeResult.ReadAllowed, propertyPrivilegeResult.UpdateAllowed,
                    propertyPrivilegeResult.DeleteAllowed);
        }

        private readonly bool? create;

        private readonly bool? read;

        private readonly bool? update;

        private readonly bool? delete;

        private TypePropertyPrivilegeImpl(bool? create, bool? read, bool? update, bool? delete)
        {
            this.create = create;
            this.read = read;
            this.update = update;
            this.delete = delete;
        }

        public bool? CreateAllowed
        {
            get { return create; }
        }

        public bool? ReadAllowed
        {
            get { return read; }
        }

        public bool? UpdateAllowed
        {
            get { return update; }
        }

        public bool? DeleteAllowed
        {
            get { return delete; }
        }
        
        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append(AbstractPrivilege.upperOrLower(CreateAllowed, 'c'));
            sb.Append(AbstractPrivilege.upperOrLower(ReadAllowed, 'r'));
            sb.Append(AbstractPrivilege.upperOrLower(UpdateAllowed, 'u'));
            sb.Append(AbstractPrivilege.upperOrLower(DeleteAllowed, 'd'));
        }
    }
}