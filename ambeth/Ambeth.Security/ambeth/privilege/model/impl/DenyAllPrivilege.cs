using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public class DenyAllPrivilege : IPrivilege, IPrintable
    {
        public static readonly IPrivilege INSTANCE = new DenyAllPrivilege();

        private DenyAllPrivilege()
        {
            // intended blank
        }

        public IPropertyPrivilege GetPropertyPrivilege(String propertyName)
        {
            return null;
        }

        public bool ReadAllowed
        {
            get
            {
                return false;
            }
        }

        public bool CreateAllowed
        {
            get
            {
                return false;
            }
        }

        public bool UpdateAllowed
        {
            get
            {
                return false;
            }
        }

        public bool DeleteAllowed
        {
            get
            {
                return false;
            }
        }

        public bool ExecutionAllowed
        {
            get
            {
                return false;
            }
        }

        public String[] ConfiguredPropertyNames
        {
            get
            {
                return PrivilegeImpl.EMPTY_PROPERTY_NAMES;
            }
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
            sb.Append(ExecutionAllowed ? "+X" : "-X");
        }
    }
}