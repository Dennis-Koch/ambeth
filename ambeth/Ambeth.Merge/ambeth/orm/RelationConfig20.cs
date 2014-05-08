using System;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Orm
{
    public class RelationConfig20 : IRelationConfig
    {
        public String Name { get; private set; }

        public ILinkConfig Link { get; private set; }

        public EntityIdentifier EntityIdentifier { get; set; }

        public bool ExplicitlyNotMergeRelevant { get; set; }

        public RelationConfig20(String name, ILinkConfig link)
        {
            ParamChecker.AssertParamNotNullOrEmpty(Name, "Name");
            ParamChecker.AssertParamNotNull(Link, "Link");

            this.Name = name;
            this.Link = link;
        }

        public override int GetHashCode()
        {
            return Name.GetHashCode();
        }

        public override bool Equals(Object obj)
        {
            if (obj is RelationConfig20)
            {
                IRelationConfig other = (IRelationConfig)obj;
                return Name.Equals(other.Name);
            }
            else
            {
                return false;
            }
        }
    }
}
