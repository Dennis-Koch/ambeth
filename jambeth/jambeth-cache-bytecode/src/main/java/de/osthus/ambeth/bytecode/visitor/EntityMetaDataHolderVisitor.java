package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public class EntityMetaDataHolderVisitor extends ClassGenerator
{
	public static final MethodInstance m_template_getEntityMetaData = new MethodInstance(null, IEntityMetaDataHolder.class, IEntityMetaData.class,
			"get__EntityMetaData");

	protected IEntityMetaData metaData;

	public EntityMetaDataHolderVisitor(ClassVisitor cv, IEntityMetaData metaData)
	{
		super(cv);
		this.metaData = metaData;
	}

	@Override
	public void visitEnd()
	{
		implementGetEntityMetaData();
		super.visitEnd();
	}

	protected void implementGetEntityMetaData()
	{
		MethodInstance method = MethodInstance.findByTemplate(m_template_getEntityMetaData, true);
		if (method != null)
		{
			// already implemented
			return;
		}
		FieldInstance f_entityMetaData = implementStaticAssignedField("$entityMetaData", metaData);

		implementGetter(m_template_getEntityMetaData, f_entityMetaData);
	}
}