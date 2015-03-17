using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Merge.Transfer;
using System;

namespace De.Osthus.Ambeth.Objrefstore
{
    public class ObjRefStoreEnhancementHint : IEnhancementHint, ITargetNameEnhancementHint
    {
        protected readonly Type entityType;

        protected readonly int idIndex;

        public ObjRefStoreEnhancementHint(Type entityType, int idIndex)
        {
            this.entityType = entityType;
            this.idIndex = idIndex;
        }

        public Type EntityType
        {
            get
            {
                return entityType;
            }
        }

        public int IdIndex
        {
            get
            {
                return idIndex;
            }
        }

        public override bool Equals(Object obj)
        {
            if (Object.ReferenceEquals(obj, this))
            {
                return true;
            }
            if (!GetType().Equals(obj.GetType()))
            {
                return false;
            }
            ObjRefStoreEnhancementHint other = (ObjRefStoreEnhancementHint)obj;
            return EntityType.Equals(other.EntityType) && idIndex == other.IdIndex;
        }

        public override int GetHashCode()
        {
            return GetType().GetHashCode() ^ EntityType.GetHashCode() ^ IdIndex;
        }

        public T Unwrap<T>() where T : IEnhancementHint
        {
            return (T)Unwrap(typeof(T));
        }

        public virtual Object Unwrap(Type includedHintType)
        {
            if (typeof(ObjRefStoreEnhancementHint).Equals(includedHintType))
            {
                return this;
            }
            return null;
        }

        public String GetTargetName(Type typeToEnhance)
        {
            return entityType.FullName + "$" + typeof(ObjRefStore).Name + "$" + (idIndex == ObjRef.PRIMARY_KEY_INDEX ? "PK" : "AK" + idIndex);
        }


        public override String ToString()
        {
            return GetType().Name + ": " + entityType.Name + "-" + (idIndex == ObjRef.PRIMARY_KEY_INDEX ? "PK" : "AK" + idIndex);
        }
    }
}