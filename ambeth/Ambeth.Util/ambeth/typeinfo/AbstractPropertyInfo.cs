using System;
using System.Collections.Generic;
using System.Reflection;
using System.Text;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Typeinfo
{
    public abstract class AbstractPropertyInfo : IPropertyInfoIntern, IPrintable
    {
        protected static readonly Attribute[] EMPTY_ANNOTATIONS = new Attribute[0];

        protected ILinkedMap<Type, Attribute[]> annotations;

        public Type ElementType { get; set; }

        public String Name { get; protected set; }

        public Type PropertyType { get; protected set; }

        public Type EntityType { get; set; }

        public FieldInfo BackingField { get; protected set; }

        public Type DeclaringType { get; protected set; }

        public int Modifiers { get; protected set; }

        public bool IsReadable { get; protected set; }

        public bool IsWritable { get; protected set; }

		public bool IsFieldWritable { get; protected set; }

        public AbstractPropertyInfo(Type entityType)
        {
            this.EntityType = entityType;
        }

        protected virtual void Init()
        {
            //if (backingField != null)
            //{
            //    modifiers = backingField.getModifiers();
            //}
            ParamChecker.AssertNotNull(EntityType, "entityType");
            ParamChecker.AssertNotNull(Name, "name");
            ParamChecker.AssertNotNull(DeclaringType, "declaringType");
            ParamChecker.AssertNotNull(ElementType, "elementType");
            ParamChecker.AssertNotNull(PropertyType, "propertyType");
        }

        public abstract Object GetValue(Object obj);

        public abstract void SetValue(Object obj, Object value);

        public void PutAnnotations(ICustomAttributeProvider obj)
        {
            if (obj is MethodInfo)
            {
                MethodInfo m = (MethodInfo)obj;
                Type[] parameters = new Type[m.GetParameters().Length];
                for (int a = m.GetParameters().Length; a-- > 0; )
                {
                    parameters[a] = m.GetParameters()[a].ParameterType;
                }
                Type baseType = m.DeclaringType.BaseType;
                MethodInfo overriddenMethod = baseType != null ? ReflectUtil.GetDeclaredMethod(true, baseType, m.ReturnType, m.Name, parameters) : null;
                if (overriddenMethod != null)
                {
                    PutAnnotations(overriddenMethod);
                }
            }
            Object[] annotations = obj.GetCustomAttributes(true);
            foreach (Object anno in annotations)
            {
                Attribute annotation = (Attribute)anno;
                AttributeUsageAttribute attributeUsage = AnnotationUtil.GetAnnotation<AttributeUsageAttribute>(annotation.GetType(), true);
                Type type = annotation.GetType();
                if (this.annotations == null)
                {
                    this.annotations = new LinkedHashMap<Type, Attribute[]>();
                }
                Attribute[] existingAttributes = this.annotations.Get(type);
                if (existingAttributes == null)
                {
                    existingAttributes = new Attribute[0];
                }
                Attribute[] newAttributes = new Attribute[existingAttributes.Length + 1];
                Array.Copy(existingAttributes, newAttributes, existingAttributes.Length);
                newAttributes[existingAttributes.Length] = annotation;
                this.annotations.Put(type, newAttributes);
            }
        }

        public virtual void RefreshAccessors(Type realType)
	    {
		    // intended blank
	    }

        public Attribute[] GetAnnotations()
        {
            ILinkedMap<Type, Attribute[]> annotations = this.annotations;
            if (annotations == null)
            {
                return EMPTY_ANNOTATIONS;
            }
            List<Attribute> allAnnotations = new List<Attribute>();
            foreach (Entry<Type, Attribute[]> entry in annotations)
            {
                allAnnotations.AddRange(entry.Value);
            }
            return allAnnotations.ToArray();
        }

        public V GetAnnotation<V>() where V : Attribute
        {
            return (V)GetAnnotation(typeof(V));
        }

        public Attribute GetAnnotation(Type annotationType)
        {
            IMap<Type, Attribute[]> annotations = this.annotations;
            if (annotations == null)
            {
                return null;
            }
            Attribute[] annotationArray = annotations.Get(annotationType);
            return annotationArray != null ? annotationArray[0] : null;
        }

        public bool IsAnnotationPresent<V>() where V : Attribute
        {
            return IsAnnotationPresent(typeof(V));
        }

        public bool IsAnnotationPresent(Type annotationType)
        {
            Attribute attribute = GetAnnotation(annotationType);
            return attribute != null;
        }

        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append(DeclaringType.Name).Append('.').Append(Name);
        }
   }
}