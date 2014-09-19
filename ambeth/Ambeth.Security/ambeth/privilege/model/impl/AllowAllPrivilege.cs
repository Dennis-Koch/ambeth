using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public class AllowAllPrivilege : AbstractPrivilege
    {
        public static IPrivilege INSTANCE = new AllowAllPrivilege();

        private static IPropertyPrivilege allowAllPropertyPrivilege = PropertyPrivilegeImpl.Create(true, true, true, true);

        private AllowAllPrivilege() : base(true, true, true, true, true, null, null)
        {
            // intended blank
        }

        public override IPropertyPrivilege GetDefaultPropertyPrivilegeIfValid()
        {
            return allowAllPropertyPrivilege;
        }

        public override IPropertyPrivilege GetPrimitivePropertyPrivilege(int primitiveIndex)
        {
            return allowAllPropertyPrivilege;
        }

        public override IPropertyPrivilege GetRelationPropertyPrivilege(int relationIndex)
        {
            return allowAllPropertyPrivilege;
        }

        public override bool CreateAllowed
        {
            get { return true; }
        }

        public override bool ReadAllowed
        {
            get { return true; }
        }

        public override bool UpdateAllowed
        {
            get { return true; }
        }

        public override bool DeleteAllowed
        {
            get { return true; }
        }

        public override bool ExecuteAllowed
        {
            get { return true; }
        }
    }
}