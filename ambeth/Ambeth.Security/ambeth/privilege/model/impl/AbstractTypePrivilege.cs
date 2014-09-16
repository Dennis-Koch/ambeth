using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public abstract class AbstractTypePrivilege : ITypePrivilege, IPrintable, IImmutableType
    {
        public AbstractTypePrivilege(bool? create, bool? read, bool? update, bool? delete, bool? execute,
                ITypePropertyPrivilege[] primitivePropertyPrivileges, ITypePropertyPrivilege[] relationPropertyPrivileges)
        {
            // intended blank
        }


        public abstract ITypePropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex);

        public abstract ITypePropertyPrivilege getRelationPropertyPrivilege(int relationIndex);

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
            sb.Append(AbstractPrivilege.upperOrLower(ExecuteAllowed, 'e'));
        }

        public abstract bool? CreateAllowed { get; }

        public abstract bool? ReadAllowed { get; }

        public abstract bool? UpdateAllowed { get; }

        public abstract bool? DeleteAllowed { get; }

        public abstract bool? ExecuteAllowed { get; }

        public abstract ITypePropertyPrivilege getDefaultPropertyPrivilegeIfValid();
    }
}