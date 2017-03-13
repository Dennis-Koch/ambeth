package com.koch.ambeth.mapping;

import com.koch.ambeth.merge.IObjRefProvider;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;

public class MappingObjRefProvider implements IObjRefProvider
{
	protected final Member refBOBuidMember;

	protected final Member refBOVersionMember;

	protected final byte refBOBuidIndex;

	public MappingObjRefProvider(Member refBOBuidMember, Member refBOVersionMember, byte refBOBuidIndex)
	{
		this.refBOBuidMember = refBOBuidMember;
		this.refBOVersionMember = refBOVersionMember;
		this.refBOBuidIndex = refBOBuidIndex;
	}

	@Override
	public IObjRef getORI(Object obj, IEntityMetaData metaData)
	{
		Object buid = refBOBuidMember.getValue(obj, false);
		Object version = refBOVersionMember != null ? refBOVersionMember.getValue(obj, true) : null;
		IObjRef ori = new ObjRef(metaData.getEntityType(), refBOBuidIndex, buid, version);
		return ori;
	}
}
