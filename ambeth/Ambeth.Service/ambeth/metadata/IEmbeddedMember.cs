using System;

namespace De.Osthus.Ambeth.Metadata
{
    public interface IEmbeddedMember
    {
        Member[] GetMemberPath();

        String GetMemberPathString();

        String[] GetMemberPathToken();

        Member ChildMember { get; }
    }
}