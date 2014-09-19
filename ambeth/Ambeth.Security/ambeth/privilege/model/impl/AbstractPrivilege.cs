using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public abstract class AbstractPrivilege : IPrivilege, IPrintable, IImmutableType
    {
        public static char upperOrLower(bool flag, char oneChar)
        {
            if (flag)
            {
                return Char.ToUpperInvariant(oneChar);
            }
            return Char.ToLowerInvariant(oneChar);
        }

        public static char upperOrLower(bool? flag, char oneChar)
        {
            if (!flag.HasValue)
            {
                return '_';
            }
            if (flag.Value)
            {
                return Char.ToUpperInvariant(oneChar);
            }
            return Char.ToLowerInvariant(oneChar);
        }

        public AbstractPrivilege(bool create, bool read, bool update, bool delete, bool execute, IPropertyPrivilege[] primitivePropertyPrivileges,
                IPropertyPrivilege[] relationPropertyPrivileges)
        {
            // intended blank
        }

        public abstract IPropertyPrivilege GetPrimitivePropertyPrivilege(int primitiveIndex);

        public abstract IPropertyPrivilege GetRelationPropertyPrivilege(int relationIndex);

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append(upperOrLower(CreateAllowed, 'c'));
            sb.Append(upperOrLower(ReadAllowed, 'r'));
            sb.Append(upperOrLower(UpdateAllowed, 'u'));
            sb.Append(upperOrLower(DeleteAllowed, 'd'));
            sb.Append(upperOrLower(ExecuteAllowed, 'e'));
        }

        public abstract bool CreateAllowed { get; }

        public abstract bool ReadAllowed { get; }

        public abstract bool UpdateAllowed { get; }

        public abstract bool DeleteAllowed { get; }

        public abstract bool ExecuteAllowed { get; }

        public abstract IPropertyPrivilege GetDefaultPropertyPrivilegeIfValid();
    }
}