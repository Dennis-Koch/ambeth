using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Util;
using System;
using System.Runtime.Serialization;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Transfer
{
    [DataContract(Name = "TypePropertyPrivilegeOfService", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class TypePropertyPrivilegeOfService : ITypePropertyPrivilegeOfService, IPrintable
    {
        private static readonly TypePropertyPrivilegeOfService[] array = new TypePropertyPrivilegeOfService[1 << 8];

        static TypePropertyPrivilegeOfService()
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

        protected static int ToBitValue(bool? value, int startingBit)
        {
            if (!value.HasValue)
            {
                return 0;
            }
            return value.Value ? 1 << startingBit : 1 << (startingBit + 1);
        }

        private static void Put(bool? create, bool? read, bool? update, bool? delete)
        {
            int index = ToBitValue(create, 0) + ToBitValue(read, 2) + ToBitValue(update, 4) + ToBitValue(delete, 6);
            array[index] = new TypePropertyPrivilegeOfService(create, read, update, delete);
        }

        public static ITypePropertyPrivilegeOfService Create(bool? create, bool? read, bool? update, bool? delete)
        {
            int index = ToBitValue(create, 0) + ToBitValue(read, 2) + ToBitValue(update, 4) + ToBitValue(delete, 6);
            return array[index];
        }

        private readonly bool? create;
        private readonly bool? read;
        private readonly bool? update;
        private readonly bool? delete;

        private TypePropertyPrivilegeOfService(bool? create, bool? read, bool? update, bool? delete)
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

        public override bool Equals(object obj)
        {
            if (Object.ReferenceEquals(obj, this))
            {
                return true;
            }
            if (!(obj is TypePropertyPrivilegeOfService))
            {
                return false;
            }
            TypePropertyPrivilegeOfService other = (TypePropertyPrivilegeOfService)obj;
            int index = ToBitValue(create, 0) + ToBitValue(read, 2) + ToBitValue(update, 4) + ToBitValue(delete, 6);
            int otherIndex = ToBitValue(other.create, 0) + ToBitValue(other.read, 2) + ToBitValue(other.update, 4) + ToBitValue(other.delete, 6);
            return index == otherIndex;
        }

        public override int GetHashCode()
        {
            return ToBitValue(create, 0) + ToBitValue(read, 2) + ToBitValue(update, 4) + ToBitValue(delete, 6);
        }

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append(ReadAllowed.HasValue ? ReadAllowed.Value ? "+R" : "-R" : "nR");
            sb.Append(CreateAllowed.HasValue ? CreateAllowed.Value ? "+C" : "-C" : "nC");
            sb.Append(UpdateAllowed.HasValue ? UpdateAllowed.Value ? "+U" : "-U" : "nU");
            sb.Append(DeleteAllowed.HasValue ? DeleteAllowed.Value ? "+D" : "-D" : "nD");
        }
    }
}