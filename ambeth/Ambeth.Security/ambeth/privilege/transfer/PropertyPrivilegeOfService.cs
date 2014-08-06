using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Transfer
{
    [DataContract(Name = "PropertyPrivilegeOfService", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class PropertyPrivilegeOfService : IPropertyPrivilegeOfService, IPrintable
    {
        private static readonly PropertyPrivilegeOfService[] array = new PropertyPrivilegeOfService[1 << 4];

        static PropertyPrivilegeOfService()
        {
            Put1();
        }

        private static void Put1()
        {
            put2(true);
            put2(false);
        }

        private static void put2(bool create)
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

        protected static int ToBitValue(bool value, int startingBit)
        {
            return value ? 1 << startingBit : 0;
        }

        private static void Put(bool create, bool read, bool update, bool delete)
        {
            int index = ToBitValue(create, 0) + ToBitValue(read, 1) + ToBitValue(update, 2) + ToBitValue(delete, 3);
            array[index] = new PropertyPrivilegeOfService(create, read, update, delete);
        }

        public static IPropertyPrivilegeOfService Create(bool create, bool read, bool update, bool delete)
        {
            int index = ToBitValue(create, 0) + ToBitValue(read, 1) + ToBitValue(update, 2) + ToBitValue(delete, 3);
            return array[index];
        }

        private readonly bool create;
        private readonly bool read;
        private readonly bool update;
        private readonly bool delete;

        private PropertyPrivilegeOfService(bool create, bool read, bool update, bool delete)
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

        public override bool Equals(object obj)
        {
            if (Object.ReferenceEquals(obj, this))
            {
                return true;
            }
            if (!(obj is PropertyPrivilegeOfService))
            {
                return false;
            }
            PropertyPrivilegeOfService other = (PropertyPrivilegeOfService)obj;
            return create == other.create && read == other.read && update == other.update && delete == other.delete;
        }

        public override int GetHashCode()
        {
            return ToBitValue(create, 0) + ToBitValue(read, 1) + ToBitValue(update, 2) + ToBitValue(delete, 3);
        }

        public override String ToString()
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
        }
    }
}