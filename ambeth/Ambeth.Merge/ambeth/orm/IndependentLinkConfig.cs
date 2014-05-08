using System;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Orm
{
    public class IndependentLinkConfig : LinkConfig, ILinkConfig
    {
        public Type Left { get; set; }
        public Type Right { get; set; }

        public IndependentLinkConfig(String alias)
        {
            ParamChecker.AssertParamNotNullOrEmpty(alias, "Alias");
            this.Alias = alias;
        }
    }
}
