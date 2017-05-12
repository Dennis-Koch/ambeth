using De.Osthus.Ambeth.Util;
using System;

namespace De.Osthus.Ambeth.Orm
{
    public abstract class AbstractMemberConfig : IMemberConfig
    {
        private readonly String name;

		public String DefinedBy { get; set; }

        public bool AlternateId { get; set; }

        public bool Ignore { get; set; }

        public bool ExplicitlyNotMergeRelevant { get; set; }

		public bool Transient { get; set; }

        public AbstractMemberConfig(String name)
        {
            ParamChecker.AssertParamNotNullOrEmpty(name, "name");
            this.name = name;
        }

        public String Name
        {
            get
            {
                return name;
            }
        }

        public override int GetHashCode()
        {
            return name.GetHashCode();
        }

        public abstract override bool Equals(Object obj);

        public bool Equals(AbstractMemberConfig other)
        {
            return Name.Equals(other.Name);
        }
    }
}