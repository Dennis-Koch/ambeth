using System;

namespace De.Osthus.Ambeth.Metadata
{
    public interface IPrimitiveMemberWrite
    {
        void SetTechnicalMember(bool technicalMember);

		void SetTransient(bool isTransient);

		void SetDefinedBy(PrimitiveMember definedBy);
    }
}