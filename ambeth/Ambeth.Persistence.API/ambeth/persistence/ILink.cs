using System;
using System.Collections;

namespace De.Osthus.Ambeth.Persistence
{
    public interface ILink
    {
        ITable FromTable { get; }

        ITable ToTable { get; }

        IField FromField { get; }

        IField ToField { get; }

        bool Nullable { get; }

        bool HasLinkTable { get; }

        IDirectedLink DirectedLink { get; }

        IDirectedLink ReverseDirectedLink { get; }

        String Name { get; }

        String TableName { get; }

        /**
         * Getter for the table name which is in quotes to allow to include the value directly in a query string
         * 
         * @return
         */
        String FullqualifiedEscapedTableName { get; }

        String ArchiveTableName { get; }

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