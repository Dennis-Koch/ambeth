using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Template;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Collections.Generic;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class DataObjectVisitor : ClassVisitor
    {
        public static readonly Type templateType = typeof(DataObjectTemplate);

        protected static readonly String templatePropertyName = templateType.Name;

        public static readonly MethodInstance m_toBeUpdatedChanged = new MethodInstance(null, typeof(DataObjectTemplate), "ToBeUpdatedChanged",
            typeof(IDataObject), typeof(bool), typeof(bool));

        public static readonly PropertyInstance p_hasPendingChanges = PropertyInstance.FindByTemplate(typeof(IDataObject), "HasPendingChanges", false);

        public static readonly PropertyInstance template_p_toBeCreated = PropertyInstance.FindByTemplate(typeof(IDataObject), "ToBeCreated", false);

        public static readonly PropertyInstance template_p_toBeUpdated = PropertyInstance.FindByTemplate(typeof(IDataObject), "ToBeUpdated", false);

        public static readonly PropertyInstance template_p_toBeDeleted = PropertyInstance.FindByTemplate(typeof(IDataObject), "ToBeDeleted", false);

        public static readonly ConstructorInfo c_ignoreToBeUpdated = typeof(IgnoreToBeUpdated).GetConstructor(Type.EmptyTypes);

        public static PropertyInstance GetDataObjectTemplatePI(IClassVisitor cv)
        {
            PropertyInstance p_dataObjectTemplate = State.GetProperty(templatePropertyName);
            if (p_dataObjectTemplate != null)
            {
                return p_dataObjectTemplate;
            }
            Object bean = State.BeanContext.GetService<DataObjectTemplate>();
            return cv.ImplementAssignedReadonlyProperty(templatePropertyName, bean);
        }

        protected readonly IEntityMetaData metaData;

        protected readonly IPropertyInfoProvider propertyInfoProvider;

        public DataObjectVisitor(IClassVisitor cv, IEntityMetaData metaData, IPropertyInfoProvider propertyInfoProvider)
            : base(cv)
        {
            this.metaData = metaData;
            this.propertyInfoProvider = propertyInfoProvider;
        }

        public override void VisitEnd()
        {
            PropertyInstance p_toBeCreated = ImplementToBeCreated(template_p_toBeCreated);

            // ToBeUpdated
            FieldInstance f_toBeUpdated = ImplementField(new FieldInstance(FieldAttributes.Private, "toBeUpdated", template_p_toBeUpdated.PropertyType));

            PropertyInstance p_dataObjectTemplate = GetDataObjectTemplatePI(this);

            PropertyInstance p_toBeUpdated = ImplementProperty(template_p_toBeUpdated, delegate(IMethodVisitor mv)
            {
                mv.GetThisField(f_toBeUpdated);
                mv.ReturnValue();
            }, delegate(IMethodVisitor mv)
            {
                LocalVariableInfo loc_existingValue = mv.NewLocal<bool>();
                Label l_newValueIsTrue = mv.NewLabel();
                mv.GetThisField(f_toBeUpdated);
                mv.StoreLocal(loc_existingValue);

                mv.PutThisField(f_toBeUpdated, delegate(IMethodVisitor mv2)
                {
                    mv2.LoadArg(0);
                });

                mv.LoadArg(0);
                mv.IfZCmp(CompareOperator.NE, l_newValueIsTrue);

                // call dataObjectTemplate
                mv.CallThisGetter(p_dataObjectTemplate);
                // "this" argument
                mv.LoadThis();
                // oldValue argument
                mv.LoadLocal(loc_existingValue);
                // newValue argument
                mv.LoadArg(0);
                mv.InvokeOnExactOwner(m_toBeUpdatedChanged);

                mv.Mark(l_newValueIsTrue);
                mv.ReturnValue();
            });
            p_toBeUpdated.AddAnnotation(c_ignoreToBeUpdated);

            // ToBeDeleted
            FieldInstance f_toBeDeleted = ImplementField(new FieldInstance(FieldAttributes.Private, "toBeDeleted", template_p_toBeDeleted.PropertyType));
            PropertyInstance p_toBeDeleted = ImplementProperty(template_p_toBeDeleted, delegate(IMethodVisitor mv)
            {
                mv.GetThisField(f_toBeDeleted);
                mv.ReturnValue();
            }, delegate(IMethodVisitor mv)
            {
                mv.PutThisField(f_toBeDeleted, delegate(IMethodVisitor mv2)
                {
                    mv2.LoadArg(0);
                });
                mv.ReturnValue();
            });
            p_toBeDeleted.AddAnnotation(c_ignoreToBeUpdated);

            ImplementHasPendingChanges(p_hasPendingChanges, p_toBeUpdated, p_toBeCreated, p_toBeDeleted);

            base.VisitEnd();
        }

        /**
         * public boolean isToBeCreated() { return get__Id() == null; }
         * 
         * @param owner
         */
        protected PropertyInstance ImplementToBeCreated(PropertyInstance p_toBeCreated)
        {
            IMethodVisitor mg = VisitMethod(p_toBeCreated.Getter);
            p_toBeCreated = PropertyInstance.FindByTemplate(p_toBeCreated, false);
            ITypeInfoItem idMember = metaData.IdMember;
            if (idMember is CompositeIdTypeInfoItem)
            {
                List<String> names = new List<String>();
                foreach (ITypeInfoItem itemMember in ((CompositeIdTypeInfoItem)idMember).Members)
                {
                    names.Add(itemMember.Name);   
                }
                p_toBeCreated.AddAnnotation(c_fireThisOPC, new Object[] { names.ToArray() } );
            }
            else
            {
                p_toBeCreated.AddAnnotation(c_fireThisOPC, idMember.Name);
            }

            Label trueLabel = mg.NewLabel();

            mg.LoadThis();
            mg.InvokeVirtual(GetIdMethodCreator.GetGetId());

            mg.IfNull(trueLabel);

            mg.Push(false);
            mg.ReturnValue();

            mg.Mark(trueLabel);

            mg.Push(true);
            mg.ReturnValue();
            mg.EndMethod();
            return p_toBeCreated;
        }

        /**
         * public boolean hasPendingChanges() { return isToBeUpdated() || isToBeCreated() || isToBeDeleted(); }
         */
        protected void ImplementHasPendingChanges(PropertyInstance p_hasPendingChanges, PropertyInstance p_ToBeUpdated, PropertyInstance p_ToBeCreated,
                PropertyInstance p_ToBeDeleted)
        {
            IMethodVisitor mg = VisitMethod(p_hasPendingChanges.Getter);
            p_hasPendingChanges = PropertyInstance.FindByTemplate(p_hasPendingChanges, false);
            p_hasPendingChanges.AddAnnotation(c_ignoreToBeUpdated);
            p_hasPendingChanges.AddAnnotation(c_fireThisOPC, p_ToBeCreated.Name);
            p_hasPendingChanges.AddAnnotation(c_fireThisOPC, p_ToBeUpdated.Name);
            p_hasPendingChanges.AddAnnotation(c_fireThisOPC, p_ToBeDeleted.Name);

            Label trueLabel = mg.NewLabel();

            mg.LoadThis();
            mg.InvokeVirtual(p_ToBeUpdated.Getter);
            mg.IfZCmp(CompareOperator.NE, trueLabel);

            mg.LoadThis();
            mg.InvokeVirtual(p_ToBeCreated.Getter);
            mg.IfZCmp(CompareOperator.NE, trueLabel);

            mg.LoadThis();
            mg.InvokeVirtual(p_ToBeDeleted.Getter);
            mg.IfZCmp(CompareOperator.NE, trueLabel);

            mg.Push(false);
            mg.ReturnValue();

            mg.Mark(trueLabel);
            mg.Push(true);
            mg.ReturnValue();
            mg.EndMethod();
        }
    }
}