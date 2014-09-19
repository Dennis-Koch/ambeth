using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Template;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class InitializeEmbeddedMemberVisitor : ClassVisitor
    {
        public static readonly Type templateType = typeof(EmbeddedMemberTemplate);

        public static readonly String templatePropertyName = templateType.Name;

        protected static readonly MethodInstance template_m_createEmbeddedObject = new MethodInstance(null, templateType, typeof(Object), "CreateEmbeddedObject",
                typeof(Type), typeof(Type), typeof(Object), typeof(String));

        public static PropertyInstance GetEmbeddedMemberTemplatePI(IClassVisitor cv)
        {
            Object bean = State.BeanContext.GetService(templateType);
            PropertyInstance pi = State.GetProperty(templatePropertyName, NewType.GetType(bean.GetType()));
            if (pi != null)
            {
                return pi;
            }
            return cv.ImplementAssignedReadonlyProperty(templatePropertyName, bean);
        }

        public static bool IsEmbeddedMember(IEntityMetaData metaData, String name)
        {
            String[] nameSplit = name.Split('.');
            foreach (Member member in metaData.PrimitiveMembers)
            {
                if (!(member is IEmbeddedMember))
                {
                    continue;
                }
                if (((IEmbeddedMember)member).GetMemberPath()[0].Name.Equals(nameSplit[0]))
                {
                    return true;
                }
            }
            foreach (RelationMember member in metaData.RelationMembers)
            {
                if (!(member is IEmbeddedMember))
                {
                    continue;
                }
                if (((IEmbeddedMember)member).GetMemberPath()[0].Name.Equals(nameSplit[0]))
                {
                    return true;
                }
            }
            return false;
        }

        protected IPropertyInfoProvider propertyInfoProvider;

        protected IEntityMetaData metaData;

        protected String memberPath;

        protected String[] memberPathSplit;

        public InitializeEmbeddedMemberVisitor(IClassVisitor cv, IEntityMetaData metaData, String memberPath, IPropertyInfoProvider propertyInfoProvider)
            : base(cv)
        {
            this.metaData = metaData;
            this.memberPath = memberPath;
            this.memberPathSplit = memberPath != null ? memberPath.Split('.') : null;
            this.propertyInfoProvider = propertyInfoProvider;
        }

        public override void VisitEnd()
        {
            PropertyInstance p_embeddedMemberTemplate = GetEmbeddedMemberTemplatePI(this);

            IdentityHashSet<Member> alreadyHandledFirstMembers = new IdentityHashSet<Member>();

            foreach (Member member in metaData.PrimitiveMembers)
            {
                HandleMember(p_embeddedMemberTemplate, member, alreadyHandledFirstMembers);
            }
            foreach (RelationMember member in metaData.RelationMembers)
            {
                HandleMember(p_embeddedMemberTemplate, member, alreadyHandledFirstMembers);
            }
            base.VisitEnd();
        }

        protected void HandleMember(PropertyInstance p_embeddedMemberTemplate, Member member, ISet<Member> alreadyHandledFirstMembers)
        {
            if (!(member is IEmbeddedMember))
            {
                return;
            }
            Member[] memberPath = ((IEmbeddedMember)member).GetMemberPath();

            Member firstMember;
            if (memberPathSplit != null)
            {
                if (memberPath.Length > memberPathSplit.Length)
                {
                    firstMember = memberPath[memberPathSplit.Length];
                }
                else
                {
                    // nothing to do in this case
                    return;
                }
            }
            else
            {
                firstMember = memberPath[0];
            }
            if (!alreadyHandledFirstMembers.Add(firstMember))
            {
                return;
            }
            ImplementGetter(p_embeddedMemberTemplate, firstMember, this.memberPath != null ? this.memberPath : firstMember.Name);
        }

        // protected void implementGetter(IProperty)
        // {
        // MethodGenerator mv = visitMethod(template_m_getValue);
        //
        // for (int a = 0, size = propertyPath.length; a < size; a++)
        // {
        // IPropertyInfo property = propertyPath[a];
        // if (property instanceof MethodPropertyInfo && ((MethodPropertyInfo) property).getGetter() == null)
        // {
        // throw new IllegalStateException("Property not readable: " + property.getDeclaringType().getName() + "." + property.getName());
        // }
        // }
        // Label l_pathHasNull = null;
        // mv.loadArg(0);
        // mv.checkCast(propertyPath[0].getDeclaringType());
        // for (int a = 0, size = propertyPath.length - 1; a < size; a++)
        // {
        // if (l_pathHasNull == null)
        // {
        // l_pathHasNull = mv.newLabel();
        // }
        // invokeGetProperty(mv, propertyPath[a]);
        // mv.dup();
        // mv.ifNull(l_pathHasNull);
        // }
        // IPropertyInfo lastProperty = propertyPath[propertyPath.length - 1];
        // Type lastPropertyType = Type.getType(lastProperty.getPropertyType());
        // invokeGetProperty(mv, lastProperty);
        // mv.valueOf(lastPropertyType);
        // mv.returnValue();
        //
        // if (l_pathHasNull != null)
        // {
        // mv.mark(l_pathHasNull);
        // if (lastProperty.getPropertyType().isPrimitive())
        // {
        // mv.pop(); // remove the current null value
        // mv.pushNullOrZero(lastPropertyType);
        // mv.valueOf(lastPropertyType);
        // }
        // mv.returnValue();
        // }
        // mv.endMethod();
        // }

        protected void ImplementGetter(PropertyInstance p_embeddedMemberTemplate, Member firstMember, String memberPath)
        {
            PropertyInstance property = PropertyInstance.FindByTemplate(firstMember.Name, firstMember.RealType, false);

            PropertyInstance p_rootEntity = memberPathSplit == null ? null : EmbeddedTypeVisitor.GetRootEntityProperty(this);

            IMethodVisitor mv = VisitMethod(property.Getter);
            Label l_valueIsValid = mv.NewLabel();

            mv.LoadThis();
            mv.InvokeSuperOfCurrentMethod();
            mv.Dup(); // cache member value

            mv.IfNonNull(l_valueIsValid);

            mv.Pop(); // remove remaining null value

            mv.CallThisSetter(property, delegate(IMethodVisitor mg)
                {
                    // Object p_embeddedMemberTemplate.createEmbeddedObject(Class<?> embeddedType, Class<?> entityType, Object parentObject, String memberPath)
                    mg.CallThisGetter(p_embeddedMemberTemplate);

                    mg.Push(firstMember.RealType); // embeddedType

                    if (p_rootEntity != null)
                    {
                        mg.CallThisGetter(p_rootEntity);
                        mg.CheckCast(EntityMetaDataHolderVisitor.m_template_getEntityMetaData.Owner);
                        mg.InvokeInterface(EntityMetaDataHolderVisitor.m_template_getEntityMetaData);
                    }
                    else
                    {
                        mg.CallThisGetter(EntityMetaDataHolderVisitor.m_template_getEntityMetaData);
                    }
                    mg.InvokeInterface(new MethodInstance(null, typeof(IEntityMetaData), typeof(Type), "get_EnhancedType"));
                    mg.LoadThis(); // parentObject
                    mg.Push(memberPath);

                    mg.InvokeVirtual(template_m_createEmbeddedObject);
                    mg.CheckCast(firstMember.RealType);
                });
            mv.LoadThis();
            mv.InvokeSuperOfCurrentMethod();
            mv.Mark(l_valueIsValid);
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void InvokeGetProperty(IMethodVisitor mv, IPropertyInfo property)
        {
            if (property is MethodPropertyInfo)
            {
                MethodInfo method = ((MethodPropertyInfo)property).Getter;
                mv.InvokeVirtual(new MethodInstance(method));
            }
            else
            {
                FieldInfo field = ((FieldPropertyInfo)property).BackingField;
                mv.GetField(new FieldInstance(field));
            }
        }

        protected void InvokeSetProperty(IMethodVisitor mv, IPropertyInfo property)
        {
            if (property is MethodPropertyInfo)
            {
                MethodInfo method = ((MethodPropertyInfo)property).Setter;
                mv.InvokeVirtual(new MethodInstance(method));
            }
            else
            {
                FieldInfo field = ((FieldPropertyInfo)property).BackingField;
                mv.PutField(new FieldInstance(field));
            }
        }
    }
}
