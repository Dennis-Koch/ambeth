using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.Mapping
{
    public class MappingObjRefProvider : IObjRefProvider
    {
        protected readonly ITypeInfoItem refBOBuidMember;

        protected readonly ITypeInfoItem refBOVersionMember;

        protected readonly sbyte refBOBuidIndex;

        public MappingObjRefProvider(ITypeInfoItem refBOBuidMember, ITypeInfoItem refBOVersionMember, sbyte refBOBuidIndex)
        {
            this.refBOBuidMember = refBOBuidMember;
            this.refBOVersionMember = refBOVersionMember;
            this.refBOBuidIndex = refBOBuidIndex;
        }

        public IObjRef GetORI(Object obj, IEntityMetaData metaData)
        {
            String buid = (String)refBOBuidMember.GetValue(obj, false);
            Object version = refBOVersionMember != null ? refBOVersionMember.GetValue(obj, true) : null;
            IObjRef ori = new ObjRef(metaData.EntityType, refBOBuidIndex, buid, version);
            return ori;
        }
    }
}