using System;

namespace De.Osthus.Ambeth.Privilege.Model
{
    public class PrivilegeItem : IPrivilegeItem
    {
        public static readonly PrivilegeItem DENY_ALL = new PrivilegeItem(new PrivilegeEnum[4]);

        public const int CREATE_INDEX = 0;

        public const int UPDATE_INDEX = 1;

        public const int DELETE_INDEX = 2;

        public const int READ_INDEX = 3;

        public const int EXECUTION_INDEX = 4;

        protected readonly PrivilegeEnum[] privileges;

        public PrivilegeItem(PrivilegeEnum[] privileges)
        {
            this.privileges = privileges;
        }

        public bool CreateAllowed
        {
            get
            {
                return PrivilegeEnum.CREATE_ALLOWED.Equals(privileges[CREATE_INDEX]);
            }
        }

        public bool UpdateAllowed
        {
            get
            {
                return PrivilegeEnum.UPDATE_ALLOWED.Equals(privileges[UPDATE_INDEX]);
            }
        }

        public bool DeleteAllowed
        {
            get
            {
                return PrivilegeEnum.DELETE_ALLOWED.Equals(privileges[DELETE_INDEX]);
            }
        }

        public bool ReadAllowed
        {
            get
            {
                return PrivilegeEnum.READ_ALLOWED.Equals(privileges[READ_INDEX]);
            }
        }

        public bool ExecutionAllowed
        {
            get
            {
                return PrivilegeEnum.EXECUTE_ALLOWED.Equals(privileges[EXECUTION_INDEX]);
            }
        }
    }
}
