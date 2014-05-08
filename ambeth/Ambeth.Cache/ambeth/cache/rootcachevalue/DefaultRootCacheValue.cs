using De.Osthus.Ambeth.Merge.Model;
using System;

namespace De.Osthus.Ambeth.Cache.Rootcachevalue
{
	public class DefaultRootCacheValue : RootCacheValue
	{
		protected readonly Type entityType;

		protected Object[] primitives;

		protected IObjRef[][] relations;

		private Object id;

		private Object version;

		public DefaultRootCacheValue(Type entityType) : base(entityType)
		{
			this.entityType = entityType;
		}

        public override Object Id
        {
            get
            {
                return id;
            }
            set
            {
                this.id = value;
            }
        }

        public override Object Version
        {
            get
            {
                return version;
            }
            set
            {
                this.version = value;
            }
        }
		
		public override void SetPrimitives(Object[] primitives)
		{
			this.primitives = primitives;
		}

		public override Object[] GetPrimitives()
		{
			return primitives;
		}

        public override object GetPrimitive(int primitiveIndex)
        {
            return primitives[primitiveIndex];
        }

		public override IObjRef[][] GetRelations()
		{
			return relations;
		}

		public override void SetRelations(IObjRef[][] relations)
		{
			this.relations = relations;
		}

        public override IObjRef[] GetRelation(int relationindex)
        {
            return relations[relationindex];
        }

		public override void SetRelation(int relationIndex, IObjRef[] relationsOfMember)
		{
			relations[relationIndex] = relationsOfMember;
		}

		public override Type EntityType
		{
            get
            {
                return entityType;
            }
		}
	}
}