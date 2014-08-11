using System;
namespace De.Osthus.Ambeth.Util
{
    public class IdTypeTuple
    {
        public readonly sbyte idNameIndex;

        public readonly Object persistentId;

        public readonly Type type;

        public IdTypeTuple(Type type, sbyte idNameIndex, Object persistentId)
        {
            this.type = type;
            this.idNameIndex = idNameIndex;
            this.persistentId = persistentId;
        }

        public override int GetHashCode()
        {
            return persistentId.GetHashCode() ^ type.GetHashCode();
        }

        public override bool Equals(Object obj)
        {
            if (!(obj is IdTypeTuple))
            {
                return false;
            }
            IdTypeTuple other = (IdTypeTuple)obj;
            bool idEqual;
            if (persistentId is IComparable && persistentId.GetType().Equals(other.persistentId.GetType()))
            {
                idEqual = ((IComparable)persistentId).CompareTo(other.persistentId) == 0;
            }
            else
            {
                idEqual = persistentId.Equals(other.persistentId);
            }
            return idEqual && type.Equals(other.type) && idNameIndex == other.idNameIndex;
        }
    }
}