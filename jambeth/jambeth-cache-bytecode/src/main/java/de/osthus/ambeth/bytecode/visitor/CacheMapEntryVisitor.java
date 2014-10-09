package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.Script;
import de.osthus.ambeth.cache.collections.CacheMapEntry;
import de.osthus.ambeth.compositeid.CompositeIdMember;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Label;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.GeneratorAdapter;
import de.osthus.ambeth.util.ImmutableTypeSet;
public class CacheMapEntryVisitor extends ClassGenerator
{
	private static final MethodInstance template_m_getEntityType = new MethodInstance(null, CacheMapEntry.class, Class.class, "getEntityType");
	private static final MethodInstance template_m_getIdIndex = new MethodInstance(null, CacheMapEntry.class, byte.class, "getIdIndex");
	private static final MethodInstance template_m_getId = new MethodInstance(null, CacheMapEntry.class, Object.class, "getId");
	private static final MethodInstance template_m_setId = new MethodInstance(null, CacheMapEntry.class, void.class, "setId", Object.class);
	private static final MethodInstance template_m_isEqualTo = new MethodInstance(null, CacheMapEntry.class, boolean.class, "isEqualTo", Class.class,
			byte.class, Object.class);

	protected final IEntityMetaData metaData;

	protected final byte idIndex;

	public CacheMapEntryVisitor(ClassVisitor cv, IEntityMetaData metaData, byte idIndex)
	{
		super(cv);
		this.metaData = metaData;
		this.idIndex = idIndex;
	}

	@Override
	public void visitEnd()
	{
		Type entityType = Type.getType(metaData.getEntityType());
		{
			MethodGenerator mv = visitMethod(template_m_getEntityType);
			mv.push(entityType);
			mv.returnValue();
			mv.endMethod();
		}

		{
			MethodGenerator mv = visitMethod(template_m_getIdIndex);
			mv.push(idIndex);
			mv.returnValue();
			mv.endMethod();
		}

		FieldInstance f_id = implementNativeField(this, metaData.getIdMemberByIdIndex(idIndex), template_m_getId, template_m_setId);

		if (f_id.getType().getOpcode(Opcodes.IRETURN) != Opcodes.ARETURN)
		{
			// id is a primitive type. So we use an improved version of the 3-tuple equals without boxing the id
			MethodGenerator mv = visitMethod(template_m_isEqualTo);
			Label l_notEqual = mv.newLabel();

			mv.push(entityType);
			mv.loadArg(0);
			mv.ifCmp(Type.getType(Class.class), GeneratorAdapter.NE, l_notEqual);

			mv.push(idIndex);
			mv.loadArg(1);
			mv.ifCmp(Type.BOOLEAN_TYPE, GeneratorAdapter.NE, l_notEqual);

			mv.getThisField(f_id);
			mv.loadArg(2);
			mv.unbox(f_id.getType());
			mv.ifCmp(f_id.getType(), GeneratorAdapter.NE, l_notEqual);

			mv.push(true);
			mv.returnValue();
			mv.mark(l_notEqual);
			mv.push(false);
			mv.returnValue();
			mv.endMethod();
		}
		super.visitEnd();
	}

	public static String getFieldName(Member member)
	{
		return "$" + member.getName().replaceAll("\\.", "_");
	}

	public static FieldInstance implementNativeField(ClassGenerator cv, Member member, MethodInstance m_get, MethodInstance m_set)
	{
		if (member == null)
		{
			// NoOp implementation
			{
				MethodGenerator mv = cv.visitMethod(m_get);
				mv.pushNull();
				mv.returnValue();
				mv.endMethod();
			}
			{
				MethodGenerator mv = cv.visitMethod(m_set);
				mv.returnValue();
				mv.endMethod();
			}
			return null;
		}
		if (member instanceof CompositeIdMember || (!member.getRealType().isPrimitive() && ImmutableTypeSet.getUnwrappedType(member.getRealType()) == null))
		{
			// no business case for any complex efforts
			FieldInstance f_id = cv.implementField(new FieldInstance(Opcodes.ACC_PRIVATE, getFieldName(member), null, Object.class));
			m_get = cv.implementGetter(m_get, f_id);
			m_set = cv.implementSetter(m_set, f_id);
			return f_id;
		}

		Class<?> nativeType = member.getRealType();
		if (!nativeType.isPrimitive())
		{
			nativeType = ImmutableTypeSet.getUnwrappedType(nativeType);
		}
		FieldInstance f_id = cv.implementField(new FieldInstance(Opcodes.ACC_PRIVATE, getFieldName(member), null, nativeType));

		final Type nativeTypeHandle = Type.getType(nativeType);
		{
			MethodGenerator mv = cv.visitMethod(m_get);
			mv.getThisField(f_id);
			mv.valueOf(nativeTypeHandle);
			mv.returnValue();
			mv.endMethod();
		}
		{
			MethodGenerator mv = cv.visitMethod(m_set);
			mv.putThisField(f_id, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					Label l_isNotNull = mg.newLabel();
					Label l_finish = mg.newLabel();

					mg.loadArg(0);
					mg.ifNonNull(l_isNotNull);
					mg.pushNullOrZero(nativeTypeHandle);
					mg.goTo(l_finish);
					mg.mark(l_isNotNull);
					mg.loadArg(0);
					mg.unbox(nativeTypeHandle);
					mg.mark(l_finish);
				}
			});
			mv.returnValue();
			mv.endMethod();
		}
		return f_id;
	}
}