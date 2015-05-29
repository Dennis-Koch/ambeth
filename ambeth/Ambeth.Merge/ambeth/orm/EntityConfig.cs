using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.Orm
{
    public class EntityConfig : IEntityConfig
    {
        public Type EntityType { get; private set; }

        public Type RealType { get; private set; }

        public bool Local { get; set; }

        public String TableName { get; set; }

        public String PermissionGroupName { get; set; }

        public String SequenceName { get; set; }

        public IMemberConfig IdMemberConfig { get; set; }

        public IMemberConfig VersionMemberConfig { get; set; }

		public String DescriminatorName { get; set; }

        public bool VersionRequired { get; set; }

        public IMemberConfig CreatedByMemberConfig { get; set; }

        public IMemberConfig CreatedOnMemberConfig { get; set; }

        public IMemberConfig UpdatedByMemberConfig { get; set; }

        public IMemberConfig UpdatedOnMemberConfig { get; set; }

        private IISet<IMemberConfig> memberConfigs = new LinkedHashSet<IMemberConfig>();

        private IISet<IRelationConfig> relationConfigs = new LinkedHashSet<IRelationConfig>();

        [Obsolete]
        public EntityConfig(Type entityType) : this(entityType, entityType)
        {
            // Intended blank
        }

        public EntityConfig(Type entityType, Type realType)
        {
            this.EntityType = entityType;
            this.RealType = realType;
            VersionRequired = true;
        }

        public IEnumerable<IMemberConfig> GetMemberConfigIterable()
        {
            return memberConfigs;
        }

        public void AddMemberConfig(IMemberConfig memberConfig)
        {
            if (!memberConfigs.Add(memberConfig))
            {
                throw new ArgumentException("Duplicate member configuration for '" + EntityType.Name + "'.'" + memberConfig.Name + "'");
            }
        }

        public IEnumerable<IRelationConfig> GetRelationConfigIterable()
        {
            return relationConfigs;
        }

        public void AddRelationConfig(IRelationConfig relationConfig)
        {
            if (!relationConfigs.Add(relationConfig))
            {
                throw new ArgumentException("Duplicate relation configuration for '" + EntityType.Name + "'.'" + relationConfig.Name + "'");
            }
        }

        public override int GetHashCode()
        {
            return EntityType.GetHashCode();
        }

        public override bool Equals(Object obj)
        {
            if (obj is EntityConfig)
            {
                EntityConfig other = (EntityConfig)obj;
                return EntityType.Equals(other.EntityType);
            }
            else
            {
                return false;
            }
        }
    }
}
