using System;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Orm
{
    public class LinkConfig : ILinkConfig
    {
        public String Source { get; private set; }

        public String Alias { get; set; }

        public CascadeDeleteDirection CascadeDeleteDirection { get; set; }

        public LinkConfig(String source)
            : this()
        {
            ParamChecker.AssertParamNotNull(source, "Source");
            this.Source = source;
        }

        protected LinkConfig()
        {
            CascadeDeleteDirection = CascadeDeleteDirection.NONE;
        }
    }
}
