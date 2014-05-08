using De.Osthus.Ambeth.Collections;
using System;

namespace De.Osthus.Ambeth.Util
{
    public class Def2Entry<V> : IListElem<Def2Entry<V>>, IComparable<Def2Entry<V>>
    {
        public IListElem<Def2Entry<V>> Prev { get; set; }

        public IListElem<Def2Entry<V>> Next { get; set; }

        public Object ListHandle { get; set; }

        public readonly V extension;

        public readonly Type sourceType, targetType;

        public readonly int sourceDistance;

        public readonly int targetDistance;

        public Def2Entry(V extension, Type sourceType, Type targetType, int sourceDistance, int targetDistance)
        {
            this.extension = extension;
            this.sourceType = sourceType;
            this.targetType = targetType;
            this.sourceDistance = sourceDistance;
            this.targetDistance = targetDistance;
        }

        public Def2Entry<V> ElemValue
        {
            get
            {
                return this;
            }
            set
            {
                throw new NotSupportedException();
            }
        }

        public int CompareTo(Def2Entry<V> o)
        {
            if (o.sourceDistance > sourceDistance)
            {
                return 1;
            }
            if (o.sourceDistance == sourceDistance)
            {
                if (o.targetDistance > targetDistance)
                {
                    return 1;
                }
                if (o.targetDistance == targetDistance)
                {
                    return 0;
                }
            }
            return -1;
        }
    }
}