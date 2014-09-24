using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Template;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class EmbeddedTypeVisitor : ClassVisitor
    {
        public static readonly Type templateType = typeof(EmbeddedTypeTemplate);

        public static readonly String templatePropertyName = "__" + templateType.Name;

        public static readonly PropertyInstance t_p_Parent = PropertyInstance.FindByTemplate(typeof(IEmbeddedType), "Parent", typeof(Object), false);

        public static readonly PropertyInstance t_p_Root = PropertyInstance.FindByTemplate(typeof(IEmbeddedType), "Root", typeof(Object), false);

        public static readonly MethodInstance m_getRoot = new MethodInstance(null, templateType, typeof(Object), "GetRoot", typeof(IEmbeddedType));

        private static readonly String parentFieldName = "f_" + t_p_Parent.Name;

        public static FieldInstance GetParentObjectField(IClassVisitor cv)
        {
            FieldInstance f_parentObject = State.GetAlreadyImplementedField(parentFieldName);
            if (f_parentObject != null)
            {
                return f_parentObject;
            }
            Type parentObjectType = EmbeddedEnhancementHint.GetParentObjectType(State.Context);
            f_parentObject = new FieldInstance(FieldAttributes.Private | FieldAttributes.InitOnly, parentFieldName, parentObjectType);
            return cv.ImplementField(f_parentObject);
        }

        public static PropertyInstance GetParentObjectProperty(IClassVisitor cv)
        {
            PropertyInstance p_parent = PropertyInstance.FindByTemplate(t_p_Parent, true);
            if (p_parent != null)
            {
                return p_parent;
            }
            FieldInstance f_parentObject = GetParentObjectField(cv);
            p_parent = cv.ImplementGetter(t_p_Parent, f_parentObject);

            if (p_parent == null)
            {
                throw new Exception("Must never happen");
            }
            return p_parent;
        }

        public static PropertyInstance GetEmbeddedTypeTemplateProperty(IClassVisitor cv)
        {
            Object bean = State.BeanContext.GetService(templateType);
            PropertyInstance p_embeddedTypeTemplate = PropertyInstance.FindByTemplate(templatePropertyName, NewType.GetType(bean.GetType()), true);
            if (p_embeddedTypeTemplate != null)
            {
                return p_embeddedTypeTemplate;
            }
            return cv.ImplementAssignedReadonlyProperty(templatePropertyName, bean);
        }

        public static PropertyInstance GetRootEntityProperty(IClassVisitor cv)
        {
            PropertyInstance p_root = PropertyInstance.FindByTemplate(t_p_Root, true);
            if (p_root != null)
            {
                return p_root;
            }
            PropertyInstance p_embeddedTypeTemplate = GetEmbeddedTypeTemplateProperty(cv);
            p_root = cv.ImplementProperty(t_p_Root, delegate(IMethodVisitor mv)
            {
                mv.CallThisGetter(p_embeddedTypeTemplate);
                mv.LoadThis();
                mv.InvokeVirtual(m_getRoot);
                mv.ReturnValue();
            }, null);

            if (p_root == null)
            {
                throw new Exception("Must never happen");
            }
            return p_root;
        }

        public EmbeddedTypeVisitor(IClassVisitor cv)
            : base(cv)
        {
            // Intended blank
        }

        public override void VisitEnd()
        {
            // force implementation
            FieldInstance f_parentEntity = GetParentObjectField(this);
            GetParentObjectProperty(this);
            GetRootEntityProperty(this);
            ImplementEmbeddedConstructor(f_parentEntity);
            base.VisitEnd();
        }

        protected void ImplementEmbeddedConstructor(FieldInstance f_parentObject)
        {
            OverrideConstructors(delegate(IClassVisitor cv, ConstructorInstance superConstructor)
            {
                ImplementEmbeddedConstructor(f_parentObject, superConstructor);
            });
        }

        protected void ImplementEmbeddedConstructor(FieldInstance f_parentObject, ConstructorInstance superConstructor)
        {
            if (superConstructor.Parameters.Length > 0 && superConstructor.Parameters[0].Equals(f_parentObject.Type))
            {
                // super constructor already enhanced
                return;
            }
            bool baseIsEnhanced = false;//EntityEnhancer.IsEnhancedType(vs.CurrentType);
            NewType[] parameters = superConstructor.Parameters;
            NewType[] types;
            if (baseIsEnhanced)
            {
                // Only Pass-through constructors necessary. So signature remains the same
                types = null;//TypeUtil.GetClassesToTypes(..GetTypes(parameters);
            }
            else
            {
                types = new NewType[parameters.Length + 1];
                for (int a = parameters.Length + 1; a-- > 1; )
                {
                    types[a] = parameters[a - 1];
                }
                types[0] = f_parentObject.Type;
            }
            IMethodVisitor mv = VisitMethod(new ConstructorInstance(MethodAttributes.Public, types));
            mv.LoadThis();
            for (int a = 1, size = types.Length; a < size; a++)
            {
                mv.LoadArg(a); // Load constructor argument one by one, starting with the 2nd constructor argument
            }
            mv.InvokeConstructor(superConstructor);
            mv.PutThisField(f_parentObject, delegate(IMethodVisitor mv2)
            {
                mv2.LoadArg(0);
            });
            mv.ReturnValue();
            mv.EndMethod();
        }
    }
}