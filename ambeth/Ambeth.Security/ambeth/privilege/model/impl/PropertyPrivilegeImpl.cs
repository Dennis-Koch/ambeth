using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Privilege.Transfer;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public class PropertyPrivilegeImpl : IPropertyPrivilege, IPrintable
    {
        private static readonly CHashSet<PropertyPrivilegeImpl> set = new CHashSet<PropertyPrivilegeImpl>();

        static PropertyPrivilegeImpl()
        {
            set.Add(new PropertyPrivilegeImpl(false, false, false, false));
            set.Add(new PropertyPrivilegeImpl(false, false, false, true));
            set.Add(new PropertyPrivilegeImpl(false, false, true, false));
            set.Add(new PropertyPrivilegeImpl(false, false, true, true));
            set.Add(new PropertyPrivilegeImpl(false, true, false, false));
            set.Add(new PropertyPrivilegeImpl(false, true, false, true));
            set.Add(new PropertyPrivilegeImpl(false, true, true, false));
            set.Add(new PropertyPrivilegeImpl(false, true, true, true));
            set.Add(new PropertyPrivilegeImpl(true, false, false, false));
            set.Add(new PropertyPrivilegeImpl(true, false, false, true));
            set.Add(new PropertyPrivilegeImpl(true, false, true, false));
            set.Add(new PropertyPrivilegeImpl(true, false, true, true));
            set.Add(new PropertyPrivilegeImpl(true, true, false, false));
            set.Add(new PropertyPrivilegeImpl(true, true, false, true));
            set.Add(new PropertyPrivilegeImpl(true, true, true, false));
            set.Add(new PropertyPrivilegeImpl(true, true, true, true));
        }

        public static PropertyPrivilegeImpl Create(bool create, bool read, bool update, bool delete)
        {
            return set.Get(new PropertyPrivilegeImpl(create, read, update, delete));
        }

        public static PropertyPrivilegeImpl CreateFrom(IPropertyPrivilegeOfService propertyPrivilegeResult)
        {
            return Create(propertyPrivilegeResult.CreateAllowed, propertyPrivilegeResult.ReadAllowed, propertyPrivilegeResult.UpdateAllowed,
                    propertyPrivilegeResult.DeleteAllowed);
        }

        private readonly bool create;
        private readonly bool read;
        private readonly bool update;
        private readonly bool delete;

        private PropertyPrivilegeImpl(bool create, bool read, bool update, bool delete)
        {
            this.create = create;
            this.read = read;
            this.update = update;
            this.delete = delete;
        }

        public bool CreateAllowed
        {
            get
            {
                return create;
            }
        }

        public bool ReadAllowed
        {
            get
            {
                return read;
            }
        }

        public bool UpdateAllowed
        {
            get
            {
                return update;
            }
        }

        public bool DeleteAllowed
        {
            get
            {
                return delete;
            }
        }

        public override bool Equals(Object obj)
        {
            if (obj == this)
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
            return (create ? 1 : 0) * 7 ^ (read ? 1 : 0) * 17 ^ (update ? 1 : 0) * 11 ^ (delete ? 1 : 0) * 13;
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