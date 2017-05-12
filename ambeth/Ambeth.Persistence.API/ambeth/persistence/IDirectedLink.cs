using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Metadata;
using System;
using System.Collections;
namespace De.Osthus.Ambeth.Persistence
{
    public interface IDirectedLink
    {
        IDirectedLinkMetaData MetaData { get; }

        ITable FromTable { get; }
        
        ITable ToTable { get; }

        ILink Link { get; }

        IDirectedLink ReverseLink { get; }
        
        ILinkCursor FindLinked(Object fromId);

        ILinkCursor FindLinkedTo(Object toId);

        ILinkCursor FindAllLinked(IList fromIds);

        void LinkIds(Object fromId, IList toIds);

        void UpdateLink(Object fromId, Object toId);

        void UnlinkIds(Object fromId, IList toIds);

        void UnlinkAllIds(Object fromId);
    }
}