using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;
using System.Reflection;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.Annotation
{
    public class AnnotationUtil
    {
        public static bool IsAnnotationPresent<A>(MemberInfo type, bool inherit) where A : Attribute
        {
            return GetAnnotation<A>(type, inherit) != null;
        }

        public static A GetAnnotation<A>(MemberInfo member, bool inherit) where A : Attribute
        {
            Object[] annotations = member.GetCustomAttributes(typeof(A), inherit);
            if (annotations == null || annotations.Length == 0)
            {
                return default(A);
            }
            return (A)annotations[0];
        }

        public static Attribute GetAnnotation(Type annotationType, MemberInfo member, bool inherit)
        {
            Object[] annotations = member.GetCustomAttributes(annotationType, inherit);
            if (annotations == null || annotations.Length == 0)
            {
                return null;
            }
            return (Attribute)annotations[0];
        }

        public static IList<Attribute> GetAnnotations(Type annotationType, MemberInfo member, bool inherit)
        {
            Object[] annotations = member.GetCustomAttributes(annotationType, inherit);
            if (annotations == null || annotations.Length == 0)
            {
                return EmptyList.Empty<Attribute>();
            }
            List<Attribute> targetAnnotations = new List<Attribute>(annotations.Length);
            foreach (Object annotation in annotations)
            {
                targetAnnotations.Add((Attribute)annotation);
            }
            return targetAnnotations;
        }

        public static IList<V> GetAnnotations<V>(MemberInfo member, bool inherit) where V : Attribute
        {
            Object[] annotations;
            try
            {
                annotations = member.GetCustomAttributes(typeof(V), inherit);
            }
            catch (Exception e)
            {
                if (member == null)
                {
                    throw new Exception("Error getting attribute '" + typeof(V).ToString() + "' from null", e);
                }
                else if (member.DeclaringType == null)
                {
                    throw new Exception("Error getting attribute '" + typeof(V).ToString() + "' from: " + member.Name, e);
                }
                else
                {
                    throw new Exception("Error getting attribute '" + typeof(V).ToString() + "' from: " + member.DeclaringType.FullName + "." + member.Name, e);
                }
            }

            if (annotations == null || annotations.Length == 0)
            {
                return EmptyList.Empty<V>();
            }
            List<V> targetAnnotations = new List<V>(annotations.Length);
            foreach (Object annotation in annotations)
            {
                targetAnnotations.Add((V)annotation);
            }
            return targetAnnotations;
        }

        public static A GetAnnotation<A>(ITypeInfoItem member, bool inherit) where A : Attribute
        {
            return member.GetAnnotation<A>();
        }
    }
}
