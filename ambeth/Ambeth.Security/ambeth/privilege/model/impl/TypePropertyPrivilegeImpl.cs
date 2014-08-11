using De.Osthus.Ambeth.Privilege.Transfer;
using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public class TypePropertyPrivilegeImpl : ITypePropertyPrivilege, IPrintable, IImmutableType
    {
        public static ITypePropertyPrivilege[] EMPTY_PROPERTY_PRIVILEGES = new ITypePropertyPrivilege[0];

        private static TypePropertyPrivilegeImpl[] array = new TypePropertyPrivilegeImpl[1 << 8];

        static TypePropertyPrivilegeImpl()
        {
            put1();
        }

        private static void put1()
        {
            put2(null);
            put2(false);
            put2(true);
        }

        private static void put2(bool? create)
        {
            put3(create, null);
            put3(create, false);
            put3(create, true);
        }

        private static void put3(bool? create, bool? read)
        {
            put4(create, read, null);
            put4(create, read, false);
            put4(create, read, true);
        }

        private static void put4(bool? create, bool? read, bool? update)
        {
            put(create, read, update, null);
            put(create, read, update, false);
            put(create, read, update, true);
        }

        public static int ToBitValue(bool? value, int startingBit)
        {
            if (!value.HasValue)
            {
                return 0;
            }
            return value.Value ? 1 << startingBit : 1 << (startingBit + 1);
        }

        public static int ToBitValue(bool? create, bool? read, bool? update, bool? delete, bool? execute)
        {
            return ToBitValue(create, 0) + ToBitValue(read, 2) + ToBitValue(update, 4) + ToBitValue(delete, 6) + ToBitValue(execute, 8);
        }

        private static void put(bool? create, bool? read, bool? update, bool? delete)
        {
            int index = ToBitValue(create, read, update, delete, null);
            array[index] = new TypePropertyPrivilegeImpl(create, read, update, delete);
        }

        public static ITypePropertyPrivilege Create(bool? create, bool? read, bool? update, bool? delete)
        {
            int index = ToBitValue(create, read, update, delete, null);
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

        public override bool Equals(Object obj)
        {
            if (Object.ReferenceEquals(obj, this))
            {
                return true;
            }
            if (!(obj is TypePropertyPrivilegeImpl))
            {
                return false;
            }
            TypePropertyPrivilegeImpl other = (TypePropertyPrivilegeImpl)obj;
            return create == other.create && read == other.read && update == other.update && delete == other.delete;
        }


        public override int GetHashCode()
        {
            return ToBitValue(create, read, update, delete, null);
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