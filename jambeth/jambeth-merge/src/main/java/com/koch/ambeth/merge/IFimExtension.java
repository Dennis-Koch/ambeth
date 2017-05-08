package com.koch.ambeth.merge;

import java.util.function.Consumer;

import com.koch.ambeth.dot.IDotNode;
import com.koch.ambeth.dot.IDotWriter;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;

public interface IFimExtension {

	interface IDotNodeCallback {
		void accept(IDotNode node, StringBuilder labelSB);
	}

	Consumer<IDotWriter> extendEntityMetaDataGraph(IEntityMetaData metaData);

	IDotNodeCallback extendEntityMetaDataNode(IEntityMetaData metaData);

	Consumer<IDotWriter> extendPrimitiveMemberGraph(IEntityMetaData metaData, Member member);

	IDotNodeCallback extendPrimitiveMemberNode(IEntityMetaData metaData, Member member);

	Consumer<IDotWriter> extendRelationMemberGraph(IEntityMetaData metaData, Member member,
			IEntityMetaData targetMetaData);

	IDotNodeCallback extendRelationMemberNode(IEntityMetaData metaData, Member member,
			IEntityMetaData targetMetaData);
}
