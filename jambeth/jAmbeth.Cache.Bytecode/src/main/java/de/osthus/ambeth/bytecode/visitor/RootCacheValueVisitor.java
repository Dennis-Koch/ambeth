package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.Script;
import de.osthus.ambeth.cache.rootcachevalue.RootCacheValue;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Label;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.GeneratorAdapter;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.ImmutableTypeSet;

public class RootCacheValueVisitor extends ClassGenerator
{
	private static final Type objType = Type.getType(Object.class);

	private static final Type objRefArrayType = Type.getType(IObjRef[].class);

	protected final IEntityMetaData metaData;

	public RootCacheValueVisitor(ClassVisitor cv, IEntityMetaData metaData)
	{
		super(cv);
		this.metaData = metaData;
	}

	@Override
	public void visitEnd()
	{
		implementGetEntityType();
		implementId();
		implementVersion();
		implementPrimitives();
		implementRelations();
		super.visitEnd();
	}

	protected void implementGetEntityType()
	{
		MethodInstance template_m_getEntityType = new MethodInstance(null, RootCacheValue.class, "getEntityType");

		MethodGenerator mv = visitMethod(template_m_getEntityType);
		mv.push(Type.getType(metaData.getEntityType()));
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementId()
	{
		MethodInstance template_m_getId = new MethodInstance(null, RootCacheValue.class, "getId");
		MethodInstance template_m_setId = new MethodInstance(null, RootCacheValue.class, "setId", Object.class);

		CacheMapEntryVisitor.implementNativeField(this, metaData.getIdMember(), template_m_getId, template_m_setId);
	}

	protected void implementVersion()
	{
		MethodInstance template_m_getVersion = new MethodInstance(null, RootCacheValue.class, "getVersion");
		MethodInstance template_m_setVersion = new MethodInstance(null, RootCacheValue.class, "setVersion", Object.class);

		CacheMapEntryVisitor.implementNativeField(this, metaData.getVersionMember(), template_m_getVersion, template_m_setVersion);
	}

	protected void implementPrimitives()
	{
		ITypeInfoItem[] primitiveMembers = metaData.getPrimitiveMembers();
		FieldInstance[] f_primitives = new FieldInstance[primitiveMembers.length];
		FieldInstance[] f_nullFlags = new FieldInstance[primitiveMembers.length];
		Class<?>[] fieldType = new Class<?>[primitiveMembers.length];

		for (int primitiveIndex = 0, size = primitiveMembers.length; primitiveIndex < size; primitiveIndex++)
		{
			ITypeInfoItem member = primitiveMembers[primitiveIndex];
			Class<?> realType = member.getRealType();
			Class<?> nativeType = ImmutableTypeSet.getUnwrappedType(realType);
			boolean isNullable = true;
			if (nativeType == null)
			{
				nativeType = realType;
				isNullable = false;
			}
			if (java.util.Date.class.isAssignableFrom(nativeType))
			{
				nativeType = long.class;
				isNullable = true;
			}
			if (!nativeType.isPrimitive())
			{
				nativeType = Object.class;
			}
			if (isNullable)
			{
				// field is a nullable numeric field. We need a flag field to handle true null case
				FieldInstance f_nullFlag = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, CacheMapEntryVisitor.getFieldName(member) + "$isNull", null,
						boolean.class));
				f_nullFlags[primitiveIndex] = f_nullFlag;
			}
			fieldType[primitiveIndex] = nativeType;
			FieldInstance f_primitive = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, CacheMapEntryVisitor.getFieldName(member), null, nativeType));
			f_primitives[primitiveIndex] = f_primitive;
		}
		implementGetPrimitives(primitiveMembers, f_primitives, f_nullFlags);
		implementSetPrimitives(primitiveMembers, f_primitives, f_nullFlags);
		implementGetPrimitive(primitiveMembers, f_primitives, f_nullFlags);
	}

	protected void implementGetPrimitive(ITypeInfoItem[] primitiveMember, FieldInstance[] f_primitives, FieldInstance[] f_nullFlags)
	{
		MethodInstance template_m_getPrimitive = new MethodInstance(null, RootCacheValue.class, "getPrimitive", int.class);

		MethodGenerator mv = visitMethod(template_m_getPrimitive);

		if (f_primitives.length > 0)
		{
			Label l_default = mv.newLabel();
			Label[] l_primitives = new Label[f_primitives.length];
			for (int primitiveIndex = 0, size = f_primitives.length; primitiveIndex < size; primitiveIndex++)
			{
				l_primitives[primitiveIndex] = mv.newLabel();
			}

			mv.loadArg(0);
			mv.visitTableSwitchInsn(0, l_primitives.length - 1, l_default, l_primitives);

			for (int primitiveIndex = 0, size = f_primitives.length; primitiveIndex < size; primitiveIndex++)
			{
				FieldInstance f_primitive = f_primitives[primitiveIndex];
				FieldInstance f_nullFlag = f_nullFlags[primitiveIndex];

				mv.mark(l_primitives[primitiveIndex]);

				Label l_fieldIsNull = null;

				if (f_nullFlag != null)
				{
					l_fieldIsNull = mv.newLabel();
					// only do something if the field is non-null
					mv.getThisField(f_nullFlag);
					mv.ifZCmp(GeneratorAdapter.NE, l_fieldIsNull);
				}
				mv.getThisField(f_primitive);
				mv.box(f_primitive.getType());
				mv.returnValue();

				if (f_nullFlag != null)
				{
					mv.mark(l_fieldIsNull);
					mv.pushNull();
				}
				mv.returnValue();
			}
			mv.mark(l_default);
		}
		mv.throwException(Type.getType(IllegalArgumentException.class), "Given relationIndex not known");
		mv.pushNull();
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementGetPrimitives(ITypeInfoItem[] primitiveMembers, FieldInstance[] f_primitives, FieldInstance[] f_nullFlags)
	{
		MethodInstance template_m_getPrimitives = new MethodInstance(null, RootCacheValue.class, "getPrimitives");

		MethodGenerator mv = visitMethod(template_m_getPrimitives);

		mv.push(f_primitives.length);
		mv.newArray(objType);

		for (int primitiveIndex = 0, size = f_primitives.length; primitiveIndex < size; primitiveIndex++)
		{
			FieldInstance f_primitive = f_primitives[primitiveIndex];
			FieldInstance f_nullFlag = f_nullFlags[primitiveIndex];

			Label l_fieldIsNull = null;

			if (f_nullFlag != null)
			{
				l_fieldIsNull = mv.newLabel();
				// only do something if the field is non-null
				mv.getThisField(f_nullFlag);
				mv.ifZCmp(GeneratorAdapter.NE, l_fieldIsNull);
			}
			// duplicate array instance on stack
			mv.dup();

			mv.push(primitiveIndex);
			mv.getThisField(f_primitive);

			mv.box(f_primitive.getType());
			mv.arrayStore(objType);

			if (f_nullFlag != null)
			{
				mv.mark(l_fieldIsNull);
			}
		}
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementSetPrimitives(ITypeInfoItem[] primitiveMembers, FieldInstance[] f_primitives, FieldInstance[] f_nullFlags)
	{
		MethodInstance template_m_setPrimitives = new MethodInstance(null, RootCacheValue.class, "setPrimitives", Object[].class);

		MethodGenerator mv = visitMethod(template_m_setPrimitives);
		final int loc_item = mv.newLocal(objType);

		for (int primitiveIndex = 0, size = f_primitives.length; primitiveIndex < size; primitiveIndex++)
		{
			final FieldInstance f_primitive = f_primitives[primitiveIndex];
			FieldInstance f_nullFlag = f_nullFlags[primitiveIndex];

			Label l_itemIsNull = mv.newLabel();
			Label l_finish = mv.newLabel();

			mv.loadArg(0);
			mv.push(primitiveIndex);
			mv.arrayLoad(objType);
			mv.storeLocal(loc_item);

			mv.loadLocal(loc_item);
			mv.ifNull(l_itemIsNull);

			mv.putThisField(f_primitive, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					mg.loadLocal(loc_item);
					mg.unbox(f_primitive.getType());
				}
			});

			if (f_nullFlag != null)
			{
				// field is a nullable numeric value in the entity, but a native numeric value in our RCV
				mv.putThisField(f_nullFlag, new Script()
				{
					@Override
					public void execute(MethodGenerator mg)
					{
						mg.push(false);
					}
				});
			}

			mv.goTo(l_finish);
			mv.mark(l_itemIsNull);

			if (f_nullFlag != null)
			{
				// field is a nullable numeric value in the entity, but a native numeric value in our RCV
				mv.putThisField(f_nullFlag, new Script()
				{
					@Override
					public void execute(MethodGenerator mg)
					{
						mg.push(true);
					}
				});
			}
			else
			{
				mv.putThisField(f_primitive, new Script()
				{
					@Override
					public void execute(MethodGenerator mg)
					{
						mg.pushNullOrZero(f_primitive.getType());
					}
				});
			}
			mv.mark(l_finish);
		}
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementRelations()
	{
		IRelationInfoItem[] relationMembers = metaData.getRelationMembers();
		FieldInstance[] f_relations = new FieldInstance[relationMembers.length];

		for (int relationIndex = 0, size = relationMembers.length; relationIndex < size; relationIndex++)
		{
			IRelationInfoItem member = relationMembers[relationIndex];
			FieldInstance f_relation = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, CacheMapEntryVisitor.getFieldName(member), null, IObjRef[].class));
			f_relations[relationIndex] = f_relation;
		}
		implementGetRelations(relationMembers, f_relations);
		implementSetRelations(relationMembers, f_relations);
		implementGetRelation(relationMembers, f_relations);
		implementSetRelation(relationMembers, f_relations);
	}

	protected void implementGetRelation(IRelationInfoItem[] relationMembers, FieldInstance[] f_relations)
	{
		MethodInstance template_m_getRelation = new MethodInstance(null, RootCacheValue.class, "getRelation", int.class);

		MethodGenerator mv = visitMethod(template_m_getRelation);

		if (f_relations.length > 0)
		{
			Label l_default = mv.newLabel();
			Label[] l_relations = new Label[f_relations.length];
			for (int relationIndex = 0, size = l_relations.length; relationIndex < size; relationIndex++)
			{
				l_relations[relationIndex] = mv.newLabel();
			}

			mv.loadArg(0);
			mv.visitTableSwitchInsn(0, l_relations.length - 1, l_default, l_relations);

			for (int relationIndex = 0, size = f_relations.length; relationIndex < size; relationIndex++)
			{
				FieldInstance f_relation = f_relations[relationIndex];

				mv.mark(l_relations[relationIndex]);
				mv.getThisField(f_relation);
				mv.returnValue();
			}
			mv.mark(l_default);
		}
		mv.throwException(Type.getType(IllegalArgumentException.class), "Given relationIndex not known");
		mv.pushNull();
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementSetRelation(IRelationInfoItem[] relationMembers, FieldInstance[] f_relations)
	{
		MethodInstance template_m_setRelation = new MethodInstance(null, RootCacheValue.class, "setRelation", int.class, IObjRef[].class);

		MethodGenerator mv = visitMethod(template_m_setRelation);
		Label l_finish = mv.newLabel();

		for (int relationIndex = 0, size = f_relations.length; relationIndex < size; relationIndex++)
		{
			FieldInstance f_relation = f_relations[relationIndex];

			Label l_notEqual = mv.newLabel();

			mv.loadArg(0);
			mv.push(relationIndex);

			mv.ifCmp(int.class, GeneratorAdapter.NE, l_notEqual);

			mv.putThisField(f_relation, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					mg.loadArg(1);
				}
			});
			mv.goTo(l_finish);
			mv.mark(l_notEqual);
		}
		mv.throwException(Type.getType(IllegalArgumentException.class), "Given relationIndex not known");
		mv.mark(l_finish);
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementSetRelations(IRelationInfoItem[] relationMembers, FieldInstance[] fields)
	{
		MethodInstance template_m_setRelations = new MethodInstance(null, RootCacheValue.class, "setRelations", IObjRef[][].class);

		MethodGenerator mv = visitMethod(template_m_setRelations);

		for (int relationIndex = 0, size = fields.length; relationIndex < size; relationIndex++)
		{
			FieldInstance f_relation = fields[relationIndex];
			final int f_relationIndex = relationIndex;

			mv.putThisField(f_relation, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					mg.loadArg(0);
					mg.push(f_relationIndex);
					mg.arrayLoad(objRefArrayType);
				}
			});
		}
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementGetRelations(IRelationInfoItem[] relationMembers, FieldInstance[] f_relations)
	{
		MethodInstance template_m_getRelations = new MethodInstance(null, RootCacheValue.class, "getRelations");

		MethodGenerator mv = visitMethod(template_m_getRelations);

		mv.push(f_relations.length);
		mv.newArray(objRefArrayType);

		for (int relationIndex = 0, size = f_relations.length; relationIndex < size; relationIndex++)
		{
			FieldInstance f_primitive = f_relations[relationIndex];

			// duplicate array instance on stack
			mv.dup();

			mv.push(relationIndex);
			mv.getThisField(f_primitive);
			mv.arrayStore(objRefArrayType);
		}
		mv.returnValue();
		mv.endMethod();
	}

}