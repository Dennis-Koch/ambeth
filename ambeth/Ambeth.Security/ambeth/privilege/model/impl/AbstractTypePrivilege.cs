using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
    public abstract class AbstractTypePrivilege : ITypePrivilege, IPrintable, IImmutableType
    {
        public static int ArraySizeForIndex()
        {
            return 1 << 8;
        }

        public static int CalcIndex(bool? create, bool? read, bool? update, bool? delete, bool? execute)
        {
            return ToBitValue(create, 1, 1 * 2) + ToBitValue(read, 3, 3 * 2) + ToBitValue(update, 9, 9 * 2) + ToBitValue(delete, 27, 27 * 2)
                    + ToBitValue(execute, 81, 81 * 2);
        }

        public static int ToBitValue(bool? value, int valueIfTrue, int valueIfFalse)
        {
            if (!value.HasValue)
            {
                return 0;
            }
            return value.Value ? valueIfTrue : valueIfFalse;
        }

        public AbstractTypePrivilege(bool? create, bool? read, bool? update, bool? delete, bool? execute,
                ITypePropertyPrivilege[] primitivePropertyPrivileges, ITypePropertyPrivilege[] relationPropertyPrivileges)
        {
            // intended blank
        }


        public abstract ITypePropertyPrivilege GetPrimitivePropertyPrivilege(int primitiveIndex);

        public abstract ITypePropertyPrivilege GetRelationPropertyPrivilege(int relationIndex);

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

        public abstract ITypePropertyPrivilege GetDefaultPropertyPrivilegeIfValid();
    }
}