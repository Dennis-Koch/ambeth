using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Cache.Collections;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class CacheMapEntryVisitor : ClassVisitor
    {
        private static readonly MethodInstance template_m_getEntityType = new MethodInstance(null, typeof(CacheMapEntry), typeof(Type), "get_EntityType");
        private static readonly MethodInstance template_m_getIdIndex = new MethodInstance(null, typeof(CacheMapEntry), typeof(sbyte), "get_IdIndex");
        private static readonly MethodInstance template_m_getId = new MethodInstance(null, typeof(CacheMapEntry), typeof(Object), "get_Id");
        private static readonly MethodInstance template_m_setId = new MethodInstance(null, typeof(CacheMapEntry), typeof(void), "set_Id", typeof(Object));
        private static readonly MethodInstance template_m_isEqualTo = new MethodInstance(null, typeof(CacheMapEntry), typeof(bool), "IsEqualTo", typeof(Type), typeof(sbyte), typeof(Object));

        protected readonly IEntityMetaData metaData;

        protected readonly sbyte idIndex;

        public CacheMapEntryVisitor(IClassVisitor cv, IEntityMetaData metaData, sbyte idIndex)
            : base(cv)
        {
            this.metaData = metaData;
            this.idIndex = idIndex;
        }

        public override void VisitEnd()
        {
            Type entityType = metaData.EntityType;
            {
                IMethodVisitor mv = VisitMethod(template_m_getEntityType);
                mv.Push(entityType);
                mv.ReturnValue();
                mv.EndMethod();
            }

            {
                IMethodVisitor mv = VisitMethod(template_m_getIdIndex);
                mv.Push(idIndex);
                mv.ReturnValue();
                mv.EndMethod();
            }

            FieldInstance f_id = ImplementNativeField(this, metaData.GetIdMemberByIdIndex(idIndex), template_m_getId, template_m_setId);

            if (f_id.Type.Type.IsPrimitive)
            {
                // id is a primitive type. So we use an improved version of the 3-tuple equals without boxing the id
                IMethodVisitor mv = VisitMethod(template_m_isEqualTo);
			    Label l_notEqual = mv.NewLabel();

			    mv.Push(entityType);
			    mv.LoadArg(0);
			    mv.IfCmp(typeof(Type), CompareOperator.NE, l_notEqual);

			    mv.Push(idIndex);
			    mv.LoadArg(1);
			    mv.IfCmp(typeof(bool), CompareOperator.NE, l_notEqual);

                mv.GetThisField(f_id);
                mv.LoadArg(2);
                mv.Unbox(f_id.Type.Type);
                mv.IfCmp(f_id.Type.Type, CompareOperator.NE, l_notEqual);

                mv.Push(true);
			    mv.ReturnValue();
			    mv.Mark(l_notEqual);
			    mv.Push(false);
			    mv.ReturnValue();
			    mv.EndMethod();
		    }
            base.VisitEnd();
        }

        public static String GetFieldName(Member member)
        {
            return "$" + member.Name.Replace('.', '_');
        }

        public static FieldInstance ImplementNativeField(IClassVisitor cv, Member member, MethodInstance m_get, MethodInstance m_set)
        {
            if (member == null)
            {
                // NoOp implementation
                {
                    IMethodVisitor mv = cv.VisitMethod(m_get);
                    mv.PushNull();
                    mv.ReturnValue();
                    mv.EndMethod();
                }
                {
                    IMethodVisitor mv = cv.VisitMethod(m_set);
                    mv.ReturnValue();
                    mv.EndMethod();
                }
                return null;
            }
            if (member is CompositeIdMember
                    || (!member.RealType.IsPrimitive && ImmutableTypeSet.GetUnwrappedType(member.RealType) == null))
            {
                // no business case for any complex efforts
                FieldInstance f_id2 = cv.ImplementField(new FieldInstance(FieldAttributes.Private, GetFieldName(member), typeof(Object)));
                cv.ImplementGetter(m_get, f_id2);
                cv.ImplementSetter(m_set, f_id2);
                return f_id2;
            }

            Type nativeType = member.RealType;
            if (!nativeType.IsPrimitive)
            {
                nativeType = ImmutableTypeSet.GetUnwrappedType(nativeType);
            }
            FieldInstance f_id = cv.ImplementField(new FieldInstance(FieldAttributes.Private, GetFieldName(member), nativeType));

            {
                IMethodVisitor mv = cv.VisitMethod(m_get);
                mv.GetThisField(f_id);
                mv.ValueOf(nativeType);
                mv.ReturnValue();
                mv.EndMethod();
            }
            {
                IMethodVisitor mv = cv.VisitMethod(m_set);
                mv.PutThisField(f_id, delegate(IMethodVisitor mg)
                {
                    Label l_isNotNull = mg.NewLabel();
                    Label l_finish = mg.NewLabel();

                    mg.LoadArg(0);
                    mg.IfNonNull(l_isNotNull);
                    mg.PushNullOrZero(nativeType);
                    mg.GoTo(l_finish);
                    mg.Mark(l_isNotNull);
                    mg.LoadArg(0);
                    mg.Unbox(nativeType);
                    mg.Mark(l_finish);
                });
                mv.ReturnValue();
                mv.EndMethod();
            }
            return f_id;
        }
    }
}