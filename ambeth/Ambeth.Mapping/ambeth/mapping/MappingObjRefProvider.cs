using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.Mapping
{
    public class MappingObjRefProvider : IObjRefProvider
    {
        protected readonly Member refBOBuidMember;

        protected readonly Member refBOVersionMember;

        protected readonly sbyte refBOBuidIndex;

        public MappingObjRefProvider(Member refBOBuidMember, Member refBOVersionMember, sbyte refBOBuidIndex)
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