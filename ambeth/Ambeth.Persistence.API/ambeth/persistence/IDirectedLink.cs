using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Collections;
namespace De.Osthus.Ambeth.Persistence
{
    public interface IDirectedLink
    {
        ITable FromTable { get; }

        IField FromField { get; }

        Type FromEntityType { get; }

        sbyte FromIdIndex { get; }

        ITypeInfoItem FromMember { get; }

        ITable ToTable { get; }

        IField ToField { get; }

        Type ToEntityType { get; }

        sbyte ToIdIndex { get; }

        ITypeInfoItem ToMember { get; }

        String Name { get; }

        bool Nullable { get; }

        bool Reverse { get; }

        bool PersistingLink { get; }

        /**
         * Link _not_ persisted in this table?
         * 
         * @return Standalone status
         */
        bool StandaloneLink { get; }

        bool CascadeDelete { get; }

        Type EntityType { get; }

        IRelationInfoItem Member { get; }

        ILink Link { get; }

        IDirectedLink ReverseLink { get; }

        CascadeLoadMode CascadeLoadMode { get; }

        ILinkCursor FindLinked(Object fromId);

        ILinkCursor FindLinkedTo(Object toId);

        ILinkCursor FindAllLinked(IList fromIds);

        void LinkIds(Object fromId, IList toIds);

        void UpdateLink(Object fromId, Object toId);

        void UnlinkIds(Object fromId, IList toIds);

        void UnlinkAllIds(Object fromId);
    }
}