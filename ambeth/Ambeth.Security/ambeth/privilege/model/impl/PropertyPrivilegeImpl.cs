using De.Osthus.Ambeth.Privilege.Transfer;
using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public class PropertyPrivilegeImpl : IPropertyPrivilege, IPrintable, IImmutableType
    {
        public static readonly IPropertyPrivilege[] EMPTY_PROPERTY_PRIVILEGES = new IPropertyPrivilege[0];

        private static readonly PropertyPrivilegeImpl[] array = new PropertyPrivilegeImpl[1 << 4];

        static PropertyPrivilegeImpl()
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
            Put(create, read, update, true);
            Put(create, read, update, false);
        }

        public static int ToBitValue(bool value, int startingBit)
        {
            return value ? 1 << startingBit : 0;
        }

        public static int ToBitValue(bool create, bool read, bool update, bool delete, bool execute)
        {
            return ToBitValue(create, 0) + ToBitValue(read, 1) + ToBitValue(update, 2) + ToBitValue(delete, 3) + ToBitValue(execute, 4);
        }

        private static void Put(bool create, bool read, bool update, bool delete)
        {
            int index = ToBitValue(create, read, update, delete, false);
            array[index] = new PropertyPrivilegeImpl(create, read, update, delete);
        }

        public static IPropertyPrivilege Create(bool create, bool read, bool update, bool delete)
        {
            int index = ToBitValue(create, read, update, delete, false);
            return array[index];
        }

        public static IPropertyPrivilege createFrom(IPrivilege privilegeAsTemplate)
        {
            return Create(privilegeAsTemplate.CreateAllowed, privilegeAsTemplate.ReadAllowed, privilegeAsTemplate.UpdateAllowed,
                    privilegeAsTemplate.DeleteAllowed);
        }

        public static IPropertyPrivilege createFrom(IPrivilegeOfService privilegeOfService)
        {
            return Create(privilegeOfService.CreateAllowed, privilegeOfService.ReadAllowed, privilegeOfService.UpdateAllowed,
                    privilegeOfService.DeleteAllowed);
        }

        public static IPropertyPrivilege createFrom(IPropertyPrivilegeOfService propertyPrivilegeResult)
        {
            return Create(propertyPrivilegeResult.CreateAllowed, propertyPrivilegeResult.ReadAllowed, propertyPrivilegeResult.UpdateAllowed,
                    propertyPrivilegeResult.DeleteAllowed);
        }

        private bool create;
        private bool read;
        private bool update;
        private bool delete;

        private PropertyPrivilegeImpl(bool create, bool read, bool update, bool delete)
        {
            this.create = create;
            this.read = read;
            this.update = update;
            this.delete = delete;
        }

        public bool CreateAllowed
        {
            get { return create; }
        }

        public bool ReadAllowed
        {
            get { return read; }
        }

        public bool UpdateAllowed
        {
            get { return update; }
        }

        public bool DeleteAllowed
        {
            get { return delete; }
        }

        public override bool Equals(Object obj)
        {
            if (Object.ReferenceEquals(obj, this))
            {
                return true;
            }
            if (!(obj is PropertyPrivilegeImpl))
            {
                return false;
            }
            PropertyPrivilegeImpl other = (PropertyPrivilegeImpl)obj;
            return create == other.create && read == other.read && update == other.update && delete == other.delete;
        }

        public override int GetHashCode()
        {
            return ToBitValue(create, read, update, delete, false);
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