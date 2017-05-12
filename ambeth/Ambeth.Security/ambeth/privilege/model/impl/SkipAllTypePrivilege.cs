using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public class SkipAllTypePrivilege : AbstractTypePrivilege
    {
        public static ITypePrivilege INSTANCE = new SkipAllTypePrivilege();

        private static ITypePropertyPrivilege skipAllPropertyPrivilege = TypePropertyPrivilegeImpl.Create(null, null, null, null);

        private SkipAllTypePrivilege() : base(null, null, null, null, null, null, null)
        {
            // intended blank
        }

        public override ITypePropertyPrivilege GetDefaultPropertyPrivilegeIfValid()
        {
            return skipAllPropertyPrivilege;
        }

        public override ITypePropertyPrivilege GetPrimitivePropertyPrivilege(int primitiveIndex)
        {
            return skipAllPropertyPrivilege;
        }

        public override ITypePropertyPrivilege GetRelationPropertyPrivilege(int relationIndex)
        {
            return skipAllPropertyPrivilege;
        }

        public override bool? CreateAllowed
        {
            get { return null; }
        }

        public override bool? ReadAllowed
        {
            get { return null; }
        }

        public override bool? UpdateAllowed
        {
            get { return null; }
        }

        public override bool? DeleteAllowed
        {
            get { return null; }
        }

        public override bool? ExecuteAllowed
        {
            get { return null; }
        }
    }
}