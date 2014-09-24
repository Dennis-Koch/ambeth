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

	public static MethodInstance getImplementedGetEntityMetaData(ClassGenerator cv, IEntityMetaData metaData)
	{
		MethodInstance method = MethodInstance.findByTemplate(m_template_getEntityMetaData, true);
		if (method != null)
		{
			// already implemented
			return method;
		}
		FieldInstance f_entityMetaData = cv.implementStaticAssignedField("sf__entityMetaData", metaData);

		return cv.implementGetter(m_template_getEntityMetaData, f_entityMetaData);
	}

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
		getImplementedGetEntityMetaData(this, metaData);
	}
}