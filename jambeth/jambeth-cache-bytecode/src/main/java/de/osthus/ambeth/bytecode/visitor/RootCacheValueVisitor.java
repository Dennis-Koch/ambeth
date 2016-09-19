package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.Script;
import de.osthus.ambeth.bytecode.ScriptWithIndex;
import de.osthus.ambeth.cache.rootcachevalue.RootCacheValue;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Label;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.GeneratorAdapter;
import de.osthus.ambeth.util.WrapperTypeSet;
import de.osthus.ambeth.util.ReflectUtil;

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
		MethodInstance template_m_getEntityType = new MethodInstance(null, RootCacheValue.class, Class.class, "getEntityType");

		MethodGenerator mv = visitMethod(template_m_getEntityType);
		mv.push(Type.getType(metaData.getEntityType()));
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementId()
	{
		MethodInstance template_m_getId = new MethodInstance(null, RootCacheValue.class, Object.class, "getId");
		MethodInstance template_m_setId = new MethodInstance(null, RootCacheValue.class, void.class, "setId", Object.class);

		CacheMapEntryVisitor.implementNativeField(this, metaData.getIdMember(), template_m_getId, template_m_setId);
	}

	protected void implementVersion()
	{
		MethodInstance template_m_getVersion = new MethodInstance(null, RootCacheValue.class, Object.class, "getVersion");
		MethodInstance template_m_setVersion = new MethodInstance(null, RootCacheValue.class, void.class, "setVersion", Object.class);

		CacheMapEntryVisitor.implementNativeField(this, metaData.getVersionMember(), template_m_getVersion, template_m_setVersion);
	}

	protected void implementPrimitives()
	{
		Member[] primitiveMembers = metaData.getPrimitiveMembers();
		FieldInstance[] f_primitives = new FieldInstance[primitiveMembers.length];
		FieldInstance[] f_nullFlags = new FieldInstance[primitiveMembers.length];
		Class<?>[] fieldType = new Class<?>[primitiveMembers.length];

		for (int primitiveIndex = 0, size = primitiveMembers.length; primitiveIndex < size; primitiveIndex++)
		{
			Member member = primitiveMembers[primitiveIndex];
			Class<?> realType = member.getRealType();
			Class<?> nativeType = WrapperTypeSet.getUnwrappedType(realType);
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

	protected void implementGetPrimitive(Member[] primitiveMember, final FieldInstance[] f_primitives, final FieldInstance[] f_nullFlags)
	{
		MethodInstance template_m_getPrimitive = new MethodInstance(null, RootCacheValue.class, Object.class, "getPrimitive", int.class);

		implementSwitchByIndex(template_m_getPrimitive, "Given primitiveIndex not known", f_primitives.length, new ScriptWithIndex()
		{
			@Override
			public void execute(MethodGenerator mg, int primitiveIndex)
			{
				FieldInstance f_primitive = f_primitives[primitiveIndex];
				FieldInstance f_nullFlag = f_nullFlags[primitiveIndex];

				Label l_fieldIsNull = null;

				if (f_nullFlag != null)
				{
					l_fieldIsNull = mg.newLabel();
					// only do something if the field is non-null
					mg.getThisField(f_nullFlag);
					mg.ifZCmp(GeneratorAdapter.NE, l_fieldIsNull);
				}
				mg.getThisField(f_primitive);
				mg.box(f_primitive.getType());
				mg.returnValue();

				if (f_nullFlag != null)
				{
					mg.mark(l_fieldIsNull);
					mg.pushNull();
				}
				mg.returnValue();
			}
		});
	}

	protected void implementGetPrimitives(Member[] primitiveMembers, FieldInstance[] f_primitives, FieldInstance[] f_nullFlags)
	{
		MethodInstance template_m_getPrimitives = new MethodInstance(null, RootCacheValue.class, Object[].class, "getPrimitives");

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

			mv.valueOf(f_primitive.getType());
			mv.arrayStore(objType);

			if (f_nullFlag != null)
			{
				mv.mark(l_fieldIsNull);
			}
		}
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementSetPrimitives(Member[] primitiveMembers, FieldInstance[] f_primitives, FieldInstance[] f_nullFlags)
	{
		MethodInstance template_m_setPrimitives = new MethodInstance(null, RootCacheValue.class, void.class, "setPrimitives", Object[].class);

		MethodGenerator mv = visitMethod(template_m_setPrimitives);
		final int loc_item = mv.newLocal(objType);

		for (int primitiveIndex = 0, size = f_primitives.length; primitiveIndex < size; primitiveIndex++)
		{
			final FieldInstance f_primitive = f_primitives[primitiveIndex];
			FieldInstance f_nullFlag = f_nullFlags[primitiveIndex];
			Member member = primitiveMembers[primitiveIndex];
			final Class<?> originalType = member.getRealType();

			final int fPrimitiveIndex = primitiveIndex;

			final Script script_loadArrayValue = new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					mg.loadArg(0);
					mg.push(fPrimitiveIndex);
					mg.arrayLoad(objType);
				}
			};

			Label l_finish = mv.newLabel();

			if (f_nullFlag == null)
			{
				if (!originalType.isPrimitive())
				{
					mv.putThisField(f_primitive, script_loadArrayValue);
					continue;
				}
				script_loadArrayValue.execute(mv);
				mv.storeLocal(loc_item);
				mv.loadLocal(loc_item);
				mv.ifNull(l_finish);

				mv.putThisField(f_primitive, new Script()
				{
					@Override
					public void execute(MethodGenerator mg)
					{
						mg.loadLocal(loc_item);
						mg.unbox(f_primitive.getType());
					}
				});

				mv.mark(l_finish);
				continue;
			}
			Label l_itemIsNull = mv.newLabel();

			script_loadArrayValue.execute(mv);
			mv.storeLocal(loc_item);

			mv.loadLocal(loc_item);
			mv.ifNull(l_itemIsNull);

			mv.putThisField(f_primitive, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					if (java.util.Date.class.equals(originalType))
					{
						// simple unboxing would result in a ClassCast with Date beeing casted to Number
						// to deal with this specific case:
						mg.ifThisInstanceOf(java.util.Date.class, new Script()
						{
							@Override
							public void execute(MethodGenerator mg)
							{
								mg.loadLocal(loc_item);
							}
						}, new Script()
						{
							@Override
							public void execute(MethodGenerator mg)
							{
								mg.loadLocal(loc_item);
								mg.checkCast(java.util.Date.class);
								mg.invokeVirtual(new MethodInstance(ReflectUtil.getDeclaredMethod(false, java.util.Date.class, long.class, "getTime")));
							}
						}, new Script()
						{
							@Override
							public void execute(MethodGenerator mg)
							{
								mg.loadLocal(loc_item);
								mg.unbox(f_primitive.getType());
							}
						});
					}
					else
					{
						mg.loadLocal(loc_item);
						mg.unbox(f_primitive.getType());
					}
				}
			});

			// field is a nullable numeric value in the entity, but a native numeric value in our RCV
			mv.putThisField(f_nullFlag, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					mg.push(false);
				}
			});

			mv.goTo(l_finish);
			mv.mark(l_itemIsNull);

			// field is a nullable numeric value in the entity, but a native numeric value in our RCV
			mv.putThisField(f_nullFlag, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					mg.push(true);
				}
			});
			mv.mark(l_finish);
		}
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementRelations()
	{
		RelationMember[] relationMembers = metaData.getRelationMembers();
		FieldInstance[] f_relations = new FieldInstance[relationMembers.length];

		for (int relationIndex = 0, size = relationMembers.length; relationIndex < size; relationIndex++)
		{
			RelationMember member = relationMembers[relationIndex];
			FieldInstance f_relation = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, CacheMapEntryVisitor.getFieldName(member), null, IObjRef[].class));
			f_relations[relationIndex] = f_relation;
		}
		implementGetRelations(relationMembers, f_relations);
		implementSetRelations(relationMembers, f_relations);
		implementGetRelation(relationMembers, f_relations);
		implementSetRelation(relationMembers, f_relations);
	}

	protected void implementGetRelation(RelationMember[] relationMembers, final FieldInstance[] f_relations)
	{
		MethodInstance template_m_getRelation = new MethodInstance(null, RootCacheValue.class, IObjRef[].class, "getRelation", int.class);

		implementSwitchByIndex(template_m_getRelation, "Given relationIndex not known", f_relations.length, new ScriptWithIndex()
		{
			@Override
			public void execute(MethodGenerator mg, int relationIndex)
			{
				FieldInstance f_relation = f_relations[relationIndex];

				mg.getThisField(f_relation);
				mg.returnValue();
			}
		});
	}

	protected void implementSetRelation(RelationMember[] relationMembers, FieldInstance[] f_relations)
	{
		MethodInstance template_m_setRelation = new MethodInstance(null, RootCacheValue.class, void.class, "setRelation", int.class, IObjRef[].class);

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

	protected void implementSetRelations(RelationMember[] relationMembers, FieldInstance[] fields)
	{
		MethodInstance template_m_setRelations = new MethodInstance(null, RootCacheValue.class, void.class, "setRelations", IObjRef[][].class);

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

	protected void implementGetRelations(RelationMember[] relationMembers, FieldInstance[] f_relations)
	{
		MethodInstance template_m_getRelations = new MethodInstance(null, RootCacheValue.class, IObjRef[][].class, "getRelations");

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