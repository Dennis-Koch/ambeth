using System;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Orm
{
    public class RelationConfigLegathy : IRelationConfig
    {
        public String Name { get; private set; }

        public bool ExplicitlyNotMergeRelevant { get; set; }

        public bool ToOne { get; private set; }

        public Type LinkedEntityType { get; set; }

        public bool DoDelete { get; set; }

        public bool MayDelete { get; set; }

        public String ConstraintName { get; set; }

        public String JoinTableName { get; set; }

        public String FromFieldName { get; set; }

        public String ToFieldName { get; set; }

        public String ToAttributeName { get; set; }

        public RelationConfigLegathy(String name, bool toOne)
        {
            ParamChecker.AssertParamNotNullOrEmpty(name, "name");

            this.Name = name;
            this.ToOne = toOne;
        }

        public override int GetHashCode()
        {
            return Name.GetHashCode();
        }

        public override bool Equals(Object obj)
        {
            if (obj is RelationConfigLegathy)
            {
                RelationConfigLegathy other = (RelationConfigLegathy)obj;
                return Name.Equals(other.Name);
            }
            else
            {
                return false;
            }
        }
    }
}
