using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Template;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class DataObjectVisitor : ClassVisitor
    {
        public static readonly Type templateType = typeof(DataObjectTemplate);

        protected static readonly String templatePropertyName = "__" + templateType.Name;

        public static readonly MethodInstance m_toBeUpdatedChanged = new MethodInstance(null, typeof(DataObjectTemplate), typeof(void), "ToBeUpdatedChanged",
            typeof(IDataObject), typeof(bool), typeof(bool));

        public static readonly PropertyInstance p_hasPendingChanges = PropertyInstance.FindByTemplate(typeof(IDataObject), "HasPendingChanges", typeof(bool), false);

        public static readonly PropertyInstance template_p_toBeCreated = PropertyInstance.FindByTemplate(typeof(IDataObject), "ToBeCreated", typeof(bool), false);

        public static readonly PropertyInstance template_p_toBeUpdated = PropertyInstance.FindByTemplate(typeof(IDataObject), "ToBeUpdated", typeof(bool), false);

        public static readonly PropertyInstance template_p_toBeDeleted = PropertyInstance.FindByTemplate(typeof(IDataObject), "ToBeDeleted", typeof(bool), false);

        public static readonly ConstructorInfo c_ignoreToBeUpdated = typeof(IgnoreToBeUpdated).GetConstructor(Type.EmptyTypes);

        public static PropertyInstance GetDataObjectTemplatePI(IClassVisitor cv)
        {
            Object bean = State.BeanContext.GetService(templateType);
            PropertyInstance p_dataObjectTemplate = State.GetProperty(templatePropertyName, NewType.GetType(bean.GetType()));
            if (p_dataObjectTemplate != null)
            {
                return p_dataObjectTemplate;
            }
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

            PropertyInstance p_toBeUpdated = ImplementToBeUpdated();

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

        protected PropertyInstance ImplementToBeUpdated()
        {
            PropertyInstance p_dataObjectTemplate = GetDataObjectTemplatePI(this);

            FieldInstance f_toBeUpdated = ImplementField(new FieldInstance(FieldAttributes.Private, "$toBeUpdated", template_p_toBeUpdated.PropertyType));

            bool atLeastOneToManyMember = false;
            List<RelationMember> parentChildMembers = new List<RelationMember>();
            foreach (RelationMember relationMember in metaData.RelationMembers)
            {
                if (relationMember.GetAnnotation(typeof(ParentChild)) != null)
                {
                    parentChildMembers.Add(relationMember);
                    if (relationMember.IsToMany)
                    {
                        atLeastOneToManyMember = true;
                    }
                }
            }
            bool fAtLeastOneToManyMember = atLeastOneToManyMember;
            PropertyInstance p_toBeUpdated = ImplementProperty(template_p_toBeUpdated, delegate(IMethodVisitor mg)
                {
                    if (parentChildMembers.Count == 0)
                    {
                        mg.GetThisField(f_toBeUpdated);
                        mg.ReturnValue();
                    }
                    else
                    {
                        LocalVariableInfo loc_iterator = null;
                        if (fAtLeastOneToManyMember)
                        {
                            loc_iterator = mg.NewLocal(typeof(IEnumerator));
                        }
                        // we have to check the toBeUpdated-State for our "parentChild" members to decide our own toBeUpdate-State by OR-concatenation
                        LocalVariableInfo loc_parentChildValue = mg.NewLocal(typeof(Object));
                        Label trueLabel = mg.NewLabel();

                        mg.GetThisField(f_toBeUpdated);
                        mg.IfZCmp(CompareOperator.NE, trueLabel);

                        foreach (RelationMember parentChildMember in parentChildMembers)
                        {
                            int relationIndex = metaData.GetIndexByRelationName(parentChildMember.Name);
                            Label l_valueIsNull = mg.NewLabel();
                            // load this RelationMember at runtime to be able to call its "getValue(Object obj)"

                            mg.LoadThis();
                            mg.Push(relationIndex);

                            mg.InvokeVirtual(MethodInstance.FindByTemplate(RelationsGetterVisitor.m_template_isInitialized_Member, false));

                            mg.IfZCmp(CompareOperator.EQ, l_valueIsNull); // skip this member if it is not initialized

                            mg.LoadThis();
                            mg.Push(relationIndex);
                            mg.InvokeVirtual(MethodInstance.FindByTemplate(RelationsGetterVisitor.m_template_getValueDirect_Member, false));

                            mg.StoreLocal(loc_parentChildValue);

                            mg.LoadLocal(loc_parentChildValue);
                            mg.IfNull(l_valueIsNull);

                            mg.LoadLocal(loc_parentChildValue);

                            if (parentChildMember.IsToMany)
                            {
                                Label l_startLoop = mg.NewLabel();
                                Label l_endLoop = mg.NewLabel();

                                mg.CheckCast(typeof(IEnumerable));
                                mg.InvokeInterface(new MethodInstance(null, typeof(IEnumerable), typeof(IEnumerator), "GetEnumerator"));
                                mg.StoreLocal(loc_iterator);

                                mg.Mark(l_startLoop);
                                mg.LoadLocal(loc_iterator);
                                mg.InvokeInterface(new MethodInstance(null, typeof(IEnumerator), typeof(bool), "MoveNext"));

                                mg.IfZCmp(CompareOperator.EQ, l_endLoop);
                                mg.LoadLocal(loc_iterator);
                                mg.InvokeInterface(new MethodInstance(null, typeof(IEnumerator), typeof(Object), "get_Current"));

                                mg.CheckCast(typeof(IDataObject));
                                mg.InvokeInterface(template_p_toBeUpdated.Getter);
                                mg.IfZCmp(CompareOperator.NE, trueLabel);

                                mg.GoTo(l_startLoop);
                                mg.Mark(l_endLoop);
                            }
                            else
                            {
                                mg.CheckCast(typeof(IDataObject));
                                mg.InvokeInterface(template_p_toBeUpdated.Getter);
                                mg.IfZCmp(CompareOperator.NE, trueLabel);
                            }
                            mg.Mark(l_valueIsNull);
                        }

                        mg.Push(false);
                        mg.ReturnValue();

                        mg.Mark(trueLabel);
                        mg.Push(true);
                        mg.ReturnValue();
                    }
                }, delegate(IMethodVisitor mv)
                {
                    LocalVariableInfo loc_existingValue = mv.NewLocal(typeof(bool));
                    Label l_finish = mv.NewLabel();
                    mv.GetThisField(f_toBeUpdated);
                    mv.StoreLocal(loc_existingValue);

                    mv.LoadLocal(loc_existingValue);
                    mv.LoadArg(0);
                    mv.IfCmp(typeof(bool), CompareOperator.EQ, l_finish);

                    mv.PutThisField(f_toBeUpdated, delegate(IMethodVisitor mg2)
                        {
                            mg2.LoadArg(0);
                        });

                    // call dataObjectTemplate
                    mv.CallThisGetter(p_dataObjectTemplate);
                    // "this" argument
                    mv.LoadThis();
                    // oldValue argument
                    mv.LoadLocal(loc_existingValue);
                    // newValue argument
                    mv.LoadArg(0);
                    mv.InvokeVirtual(m_toBeUpdatedChanged);

                    mv.Mark(l_finish);
                    mv.ReturnValue();
                });
            p_toBeUpdated.AddAnnotation(c_ignoreToBeUpdated);
            return p_toBeUpdated;
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
            Member idMember = metaData.IdMember;
            if (idMember is CompositeIdMember)
            {
                List<String> names = new List<String>();
                foreach (Member itemMember in ((CompositeIdMember)idMember).Members)
                {
                    names.Add(itemMember.Name);
                }
                p_toBeCreated.AddAnnotation(c_fireThisOPC, new Object[] { names.ToArray() });
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