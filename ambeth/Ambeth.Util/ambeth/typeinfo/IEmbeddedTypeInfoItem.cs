using System;

namespace De.Osthus.Ambeth.Typeinfo
{
	public interface IEmbeddedTypeInfoItem : ITypeInfoItem
	{
        ITypeInfoItem[] MemberPath { get; }

        String MemberPathString { get; }

        String[] MemberPathToken { get; }

        ITypeInfoItem ChildMember { get; }
	}
}