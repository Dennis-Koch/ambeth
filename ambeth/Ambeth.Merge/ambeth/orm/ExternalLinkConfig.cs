using System;

namespace De.Osthus.Ambeth.Orm
{
    public class ExternalLinkConfig : LinkConfig, ILinkConfig
    {
        public String SourceColumn { get; set; }

        public String TargetMember { get; set; }

        public ExternalLinkConfig(String source)
            : base(source)
        {
        }
    }
}
