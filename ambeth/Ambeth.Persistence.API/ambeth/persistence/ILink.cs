using System;
using System.Collections;

namespace De.Osthus.Ambeth.Persistence
{
    public interface ILink
    {
        ILinkMetaData MetaData { get; }

        ITable FromTable { get; }

        ITable ToTable { get; }
        
        IDirectedLink DirectedLink { get; }

        IDirectedLink ReverseDirectedLink { get; }
        
        ILinkCursor FindAllLinked(IDirectedLink fromLink, IList fromIds);

        ILinkCursor FindAllLinkedTo(IDirectedLink fromLink, IList toIds);

        ILinkCursor FindLinked(IDirectedLink fromLink, Object fromId);

        ILinkCursor FindLinkedTo(IDirectedLink fromLink, Object toId);

        void LinkIds(IDirectedLink fromLink, Object fromId, IList toIds);

        void UpdateLink(IDirectedLink fromLink, Object fromId, Object toIds);

        void UnlinkIds(IDirectedLink fromLink, Object fromId, IList toIds);

        void UnlinkAllIds(IDirectedLink fromLink, Object fromId);

        void UnlinkAllIds();

        void StartBatch();

        int[] FinishBatch();

        void ClearBatch();
    }
}