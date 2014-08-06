using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public class DenyAllPrivilege : AbstractPrivilege
    {
        public static IPrivilege INSTANCE = new DenyAllPrivilege();

        private static IPropertyPrivilege denyAllPropertyPrivilege = PropertyPrivilegeImpl.Create(false, false, false, false);

        private DenyAllPrivilege()
            : base(false, false, false, false, false, null, null)
        {
            // intended blank
        }

        public override IPropertyPrivilege getDefaultPropertyPrivilegeIfValid()
        {
            return denyAllPropertyPrivilege;
        }

        public override IPropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex)
        {
            return denyAllPropertyPrivilege;
        }

        public override IPropertyPrivilege getRelationPropertyPrivilege(int relationIndex)
        {
            return denyAllPropertyPrivilege;
        }

        public override bool CreateAllowed
        {
            get { return false; }
        }

        public override bool ReadAllowed
        {
            get { return false; }
        }

        public override bool UpdateAllowed
        {
            get { return false; }
        }

        public override bool DeleteAllowed
        {
            get { return false; }
        }

        public override bool ExecuteAllowed
        {
            get { return false; }
        }
    }
}