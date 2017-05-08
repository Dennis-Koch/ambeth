package com.koch.ambeth.merge.server;

import java.util.function.Consumer;

import com.koch.ambeth.dot.IDotNode;
import com.koch.ambeth.dot.IDotWriter;
import com.koch.ambeth.merge.IFimExtension;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IDirectedLinkMetaData;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.database.IDatabaseMappedListener;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.collections.ArrayList;

public class DatabaseFimExtension implements IFimExtension, IDatabaseMappedListener {

	protected String sqlFieldFillColor = "#d0771eaa";

	private final ArrayList<IDatabaseMetaData> databases = new ArrayList<IDatabaseMetaData>();

	@Override
	public void databaseMapped(IDatabaseMetaData database) {
		synchronized (databases) {
			databases.add(database);
		}
	}

	@Override
	public Consumer<IDotWriter> extendEntityMetaDataGraph(final IEntityMetaData metaData) {
		synchronized (databases) {
			final ArrayList<ITableMetaData> tables = new ArrayList<>();
			for (IDatabaseMetaData database : databases) {
				final ITableMetaData table =
						database.getTableByType(metaData.getEntityType(), true);
				if (table != null) {
					tables.add(table);
					continue;
				}
			}
			if (tables.isEmpty()) {
				return null;
			}
			return new Consumer<IDotWriter>() {
				@Override
				public void accept(IDotWriter dot) {
					for (ITableMetaData table : tables) {
						IDotNode node = dot.openNode(table);
						node.attribute("label", table.getName().replace('.', '\n'));
						node.attribute("shape", "cylinder");
						node.attribute("style", "filled");
						node.attribute("fontcolor", "#ffffffff");
						node.attribute("fillcolor", "#d0771eaa");
						node.endNode();

						IDotNode sequenceNode = dot.openNode((Object) table.getSequenceName());
						sequenceNode.attribute("label", table.getSequenceName().replace('.', '\n'));
						sequenceNode.attribute("shape", "cylinder");
						sequenceNode.attribute("style", "filled");
						sequenceNode.attribute("fontcolor", "#ffffffff");
						sequenceNode.attribute("fillcolor", "#d00000aa");
						sequenceNode.endNode();

						dot.openEdge(metaData, table).attribute("arrowhead", "none").endEdge();
						dot.openEdge(table, table.getSequenceName()).attribute("arrowhead", "none")
								.endEdge();
					}
				}
			};
		}
	}

	@Override
	public IDotNodeCallback extendEntityMetaDataNode(IEntityMetaData metaData) {
		return null;
	}

	@Override
	public Consumer<IDotWriter> extendPrimitiveMemberGraph(IEntityMetaData metaData,
			Member member) {
		return null;
	}

	@Override
	public IDotNodeCallback extendPrimitiveMemberNode(IEntityMetaData metaData, Member member) {
		synchronized (databases) {
			final ArrayList<IFieldMetaData> fields = new ArrayList<>();
			for (IDatabaseMetaData database : databases) {
				final ITableMetaData table =
						database.getTableByType(metaData.getEntityType(), true);
				if (table == null) {
					continue;
				}
				IFieldMetaData field = table.getFieldByPropertyName(member.getName());
				if (field != null) {
					fields.add(field);
				}
			}
			if (fields.isEmpty()) {
				return null;
			}
			return new IDotNodeCallback() {
				@Override
				public void accept(IDotNode node, StringBuilder sb) {
					for (IFieldMetaData field : fields) {
						sb.append('\n');
						sb.append(field.getName()).append("::").append(field.getOriginalTypeName());
					}
				}
			};
		}
	}

	@Override
	public Consumer<IDotWriter> extendRelationMemberGraph(IEntityMetaData metaData,
			final Member member, IEntityMetaData targetMetaData) {
		// synchronized (databases) {
		// final ArrayList<IDirectedLinkMetaData> links = new ArrayList<>();
		// for (IDatabaseMetaData database : databases) {
		// final ITableMetaData table =
		// database.getTableByType(metaData.getEntityType(), true);
		// if (table == null) {
		// continue;
		// }
		// IDirectedLinkMetaData link = table.getLinkByMemberName(member.getName());
		// if (link != null) {
		// links.add(link);
		// }
		// }
		// if (links.isEmpty()) {
		// return null;
		// }
		// return new Consumer<IDotWriter>() {
		// @Override
		// public void accept(IDotWriter dot) {
		// for (IDirectedLinkMetaData link : links) {
		// IFieldMetaData fromField = link.getFromField();
		// IFieldMetaData toField = link.getToField();
		//
		// {
		// IDotNode node = dot.openNode(link);
		// node.attribute("label", "default");
		// node.attribute("shape", "rectangle");
		// node.attribute("style", "filled");
		// node.attribute("fontcolor", "#ffffffff");
		// node.attribute("fillcolor", "#77d01eaa");
		// node.endNode();
		// }
		//
		// dot.openEdge(member, link).attribute("arrowhead", "none").endEdge();
		//
		// if (fromField != null && !dot.hasNode(fromField)) {
		// IDotNode node = dot.openNode(fromField);
		// node.attribute("label", fromField.getName());
		// node.attribute("shape", "rectangle");
		// node.attribute("style", "filled");
		// node.attribute("fontcolor", "#ffffffff");
		// node.attribute("fillcolor", sqlFieldFillColor);
		// node.endNode();
		//
		// dot.openEdge(fromField, link).attribute("arrowhead", "none").endEdge();
		// dot.openEdge(fromField.getTable(), fromField)
		// .attribute("arrowhead", "none").endEdge();
		// }
		// if (toField != null && !dot.hasNode(toField)) {
		// IDotNode node = dot.openNode(toField);
		// node.attribute("label", toField.getName());
		// node.attribute("shape", "rectangle");
		// node.attribute("style", "filled");
		// node.attribute("fontcolor", "#ffffffff");
		// node.attribute("fillcolor", sqlFieldFillColor);
		// node.endNode();
		//
		// dot.openEdge(toField, link).attribute("arrowhead", "none").endEdge();
		// dot.openEdge(toField.getTable(), toField).attribute("arrowhead", "none")
		// .endEdge();
		// }
		// }
		// }
		// };
		// }
		return null;
	}

	@Override
	public IDotNodeCallback extendRelationMemberNode(IEntityMetaData metaData, Member member,
			IEntityMetaData targetMetaData) {
		synchronized (databases) {
			final ArrayList<IDirectedLinkMetaData> links = new ArrayList<>();
			for (IDatabaseMetaData database : databases) {
				final ITableMetaData table =
						database.getTableByType(metaData.getEntityType(), true);
				if (table == null) {
					continue;
				}
				IDirectedLinkMetaData link = table.getLinkByMemberName(member.getName());
				if (link != null) {
					links.add(link);
				}
			}
			if (links.isEmpty()) {
				return null;
			}
			return new IDotNodeCallback() {
				@Override
				public void accept(IDotNode node, StringBuilder sb) {
					for (IDirectedLinkMetaData link : links) {
						IFieldMetaData fromField = link.getFromField();
						IFieldMetaData toField = link.getToField();

						sb.append('\n');
						if (fromField != null) {
							sb.append(fromField.getName()).append("::")
									.append(fromField.getFieldType().getSimpleName()).append("::")
									.append(fromField.getOriginalTypeName());
						} else {
							sb.append("n/a");
						}
						sb.append('\n');
						if (toField != null) {
							sb.append(toField.getName()).append("::")
									.append(toField.getFieldType().getSimpleName()).append("::")
									.append(toField.getOriginalTypeName());
						} else {
							sb.append("n/a");
						}
					}
				}
			};
		}
	}

	@Override
	public void newTableMetaData(ITableMetaData newTable) {
		// intended blank
	}
}
