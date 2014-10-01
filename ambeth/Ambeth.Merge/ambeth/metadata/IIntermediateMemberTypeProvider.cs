using System;

namespace De.Osthus.Ambeth.Metadata
{
    public interface IIntermediateMemberTypeProvider
    {
	    IntermediatePrimitiveMember GetIntermediatePrimitiveMember(Type entityType, String propertyName);

	    IntermediateRelationMember GetIntermediateRelationMember(Type entityType, String propertyName);
    }
}
