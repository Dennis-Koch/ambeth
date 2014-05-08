using System;
using De.Osthus.Ambeth.Privilege.Model;

namespace De.Osthus.Ambeth.Privilege
{
    public class PrivilegeItem : IPrivilegeItem
    {
        public const int CREATE_INDEX = 0;

        public const int UPDATE_INDEX = 1;

        public const int DELETE_INDEX = 2;

        public const int READ_INDEX = 3;

        protected readonly PrivilegeEnum[] privileges;

        public PrivilegeItem(PrivilegeEnum[] privileges)
        {
            this.privileges = privileges;
        }

        public bool IsCreateAllowed()
        {
            return PrivilegeEnum.CREATE_ALLOWED.Equals(privileges[CREATE_INDEX]);
        }

        public bool IsUpdateAllowed()
        {
            return PrivilegeEnum.UPDATE_ALLOWED.Equals(privileges[UPDATE_INDEX]);
        }

        public bool IsDeleteAllowed()
        {
            return PrivilegeEnum.DELETE_ALLOWED.Equals(privileges[DELETE_INDEX]);
        }

        public bool IsReadAllowed()
        {
            return PrivilegeEnum.READ_ALLOWED.Equals(privileges[READ_INDEX]);
        }
    }
}
