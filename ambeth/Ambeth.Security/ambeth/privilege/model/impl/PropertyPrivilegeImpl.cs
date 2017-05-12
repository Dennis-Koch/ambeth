using De.Osthus.Ambeth.Privilege.Transfer;
using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public sealed class PropertyPrivilegeImpl : IPropertyPrivilege, IPrintable, IImmutableType
    {
        public static readonly IPropertyPrivilege[] EMPTY_PROPERTY_PRIVILEGES = new IPropertyPrivilege[0];

        private static readonly PropertyPrivilegeImpl[] array = new PropertyPrivilegeImpl[ArraySizeForIndex()];

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

        public static int ArraySizeForIndex()
        {
            return 1 << 4;
        }

        public static int CalcIndex(bool create, bool read, bool update, bool delete)
        {
            return AbstractPrivilege.ToBitValue(create, 0) + AbstractPrivilege.ToBitValue(read, 1) + AbstractPrivilege.ToBitValue(update, 2)
                    + AbstractPrivilege.ToBitValue(delete, 3);
        }

        private static void Put(bool create, bool read, bool update, bool delete)
        {
            int index = CalcIndex(create, read, update, delete);
            array[index] = new PropertyPrivilegeImpl(create, read, update, delete);
        }

        public static IPropertyPrivilege Create(bool create, bool read, bool update, bool delete)
        {
            int index = CalcIndex(create, read, update, delete);
            return array[index];
        }
        
        public static IPropertyPrivilege CreateFrom(IPrivilege privilegeAsTemplate)
        {
            return Create(privilegeAsTemplate.CreateAllowed, privilegeAsTemplate.ReadAllowed, privilegeAsTemplate.UpdateAllowed,
                    privilegeAsTemplate.DeleteAllowed);
        }

        public static IPropertyPrivilege CreateFrom(IPrivilegeOfService privilegeOfService)
        {
            return Create(privilegeOfService.CreateAllowed, privilegeOfService.ReadAllowed, privilegeOfService.UpdateAllowed,
                    privilegeOfService.DeleteAllowed);
        }

        public static IPropertyPrivilege CreateFrom(IPropertyPrivilegeOfService propertyPrivilegeResult)
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