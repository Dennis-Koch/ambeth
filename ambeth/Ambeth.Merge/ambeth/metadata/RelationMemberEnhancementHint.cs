using System;

namespace De.Osthus.Ambeth.Metadata
{
    public class RelationMemberEnhancementHint : MemberEnhancementHint
    {
        public RelationMemberEnhancementHint(Type entityType, String memberName)
            : base(entityType, memberName)
        {
            // intended blank
        }

        public override Object Unwrap(Type includedHintType)
        {
            T hint = base.Unwrap(includedHintType);
            if (hint != null)
            {
                return hint;
            }
            if (typeof(RelationMemberEnhancementHint).Equals(includedHintType))
            {
                return this;
            }
            return null;
        }

        public override String getTargetName(Type typeToEnhance)
        {
            return entityType.FullName + "$" + typeof(RelationMember).Name + "$" + memberName;
        }
    }
}