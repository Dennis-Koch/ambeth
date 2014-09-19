using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using De.Osthus.Ambeth.Collections.Specialized;
using De.Osthus.Ambeth.Typeinfo;

namespace De.Osthus.Ambeth.Util
{
    public class ListUtil
    {
        public static readonly Type listType = typeof(List<Object>).GetGenericTypeDefinition();

        public static readonly Type setType = typeof(HashSet<Object>).GetGenericTypeDefinition();

        public static readonly Type obsListType = typeof(UsableObservableCollection<Object>).GetGenericTypeDefinition();

        protected static readonly ConstructorInfo listTypeC, setTypeC, obsListTypeC, sizeListTypeC, sizeSetTypeC, sizeObsListTypeC;

        static ListUtil()
        {
            listTypeC = listType.GetConstructor(new Type[0]);
            setTypeC = setType.GetConstructor(new Type[0]);
            obsListTypeC = obsListType.GetConstructor(new Type[0]);
            sizeListTypeC = listType.GetConstructor(new Type[] { typeof(int) });
            sizeSetTypeC = null;
            sizeObsListTypeC = null;
        }

        public static void AddAll<T>(IList<T> targetList, int targetIndex, IList<T> sourceList)
        {
            for (int a = 0, size = sourceList.Count; a < size; a++)
            {
                targetList.Insert(a + targetIndex, sourceList[a]);
            }
        }

        public static bool IsCollection(Type type)
        {
            return typeof(IEnumerable).IsAssignableFrom(type) && !typeof(String).Equals(type);
        }

        public static T[] ToArray<T>(ICollection<T> list)
        {
            if (list == null)
            {
                return null;
            }
            if (list.GetType().IsArray)
            {
                return (T[])list;
            }
            T[] array = new T[list.Count];
            list.CopyTo(array, 0);
            return array;
        }

        public static T[] ToArray<T>(ICollection list)
        {
            if (list == null)
            {
                return null;
            }
            if (list.GetType().IsArray)
            {
                return (T[])list;
            }
            T[] array = new T[list.Count];
            list.CopyTo(array, 0);
            return array;
        }

        public static IList<T> ToList<T>(IEnumerable<T> enumerable)
        {
            if (enumerable == null)
            {
                return null;
            }
            if (enumerable is IList<T>)
            {
                return (IList<T>)enumerable;
            }
            List<T> list = new List<T>();

            foreach (T item in enumerable)
            {
                list.Add(item);
            }
            return list;
        }

        public static Object CreateCollectionOfType(Type expectedCollectionType)
        {
            return CreateCollectionOfType(expectedCollectionType, -1);
        }

        public static Object CreateObservableCollectionOfType(Type expectedCollectionType)
        {
            return CreateObservableCollectionOfType(expectedCollectionType, -1);
        }

        public static Object CreateCollectionOfType(Type expectedCollectionType, int size)
        {
            return CreateCollectionOfType(expectedCollectionType, size, setType, listType);
        }

        public static Object CreateObservableCollectionOfType(Type expectedCollectionType, int size)
        {
            return CreateCollectionOfType(expectedCollectionType, size, setType, obsListType);
        }

        public static Object CreateCollectionOfType(Type expectedCollectionType, Type setType, Type listType)
        {
            return CreateCollectionOfType(expectedCollectionType, -1, setType, listType);
        }

        public static Object CreateCollectionOfType(Type expectedCollectionType, int size, Type setType, Type listType)
        {
            if (!expectedCollectionType.IsInterface)
            {
                return (IList)Activator.CreateInstance(expectedCollectionType);
            }
            Type[] genericArguments = expectedCollectionType.GetGenericArguments();
            Type genericArgument = typeof(Object);
            Type compareType = expectedCollectionType;
            if (genericArguments != null && genericArguments.Length > 0)
            {
                genericArgument = genericArguments[0];
                compareType = expectedCollectionType.GetGenericTypeDefinition();
            }
            if (typeof(ISet<>).IsAssignableFrom(compareType))
            {
                Type genericSetType = setType.MakeGenericType(genericArguments);
                if (size != -1)
                {
                    // .NET Set has no size hint
                }
                return Activator.CreateInstance(genericSetType);
            }
            Type genericListType = listType.MakeGenericType(genericArguments);
            if (size != -1)
            {
                return (IList)Activator.CreateInstance(genericListType, size);
            }
            return (IList)Activator.CreateInstance(genericListType);
        }

        public static IList<Object> AnyToList(Object obj)
        {
            return (IList<Object>)AnyToList(obj, typeof(Object));
        }

        public static Object AnyToList(Object obj, Type targetElementType)
        {
            Object list;
            Type targetListType = typeof(List<>).MakeGenericType(new Type[] { targetElementType });
            try
            {
                if (obj == null)
                {
                    list = Activator.CreateInstance(targetListType);
                }
                else if (obj.GetType().IsArray)
                {
                    list = AnyToList(((Array)obj).GetEnumerator());
                }
                else if (targetListType.IsAssignableFrom(obj.GetType()))
                {
                    list = obj;
                }
                else if (obj is IList)
                {
                    IList sourceList = (IList)obj;
                    MethodInfo addMethod = targetListType.GetMethod("Add");
                    list = Activator.CreateInstance(targetListType, sourceList.Count);
                    for (int a = 0, size = sourceList.Count; a < size; a++)
                    {
                        addMethod.Invoke(list, new Object[] { sourceList[a] });
                    }
                }
                else if (obj is IEnumerable && !(obj is String))
                {
                    list = AnyToList(((IEnumerable)obj).GetEnumerator());
                }
                else if (obj is IEnumerator)
                {
                    IEnumerator enumerator = (IEnumerator)obj;
                    MethodInfo addMethod = targetListType.GetMethod("Add");
                    list = Activator.CreateInstance(targetListType);
                    while (enumerator.MoveNext())
                    {
                        addMethod.Invoke(list, new Object[] { enumerator.Current });
                    }
                }
                else
                {
                    MethodInfo addMethod = targetListType.GetMethod("Add");
                    list = Activator.CreateInstance(targetListType, 1);
                    addMethod.Invoke(list, new Object[] { obj });
                }
                return list;
            }
            catch (Exception e)
            {
                String typename = obj == null ? "null" : obj.GetType().Name;
                throw new Exception("Invalid Cast of \"" + obj + "\": Types \"" + typename + "\" -> \"" + targetElementType.Name + "\".", e);
            }
        }

        //public static IList<Object> AnyToList(Object obj)
        //{
        //    IList<Object> list;

        //    if (obj == null)
        //    {
        //        list = new List<Object>(0);
        //    }
        //    else if (obj.GetType().IsArray)
        //    {
        //        list = AnyToList(((Array)obj).GetEnumerator());
        //    }
        //    else if (obj is IList<Object>)
        //    {
        //        list = (IList<Object>)obj;
        //    }
        //    else if (obj is IList)
        //    {
        //        IList sourceList = (IList)obj;
        //        list = new List<Object>(sourceList.Count);
        //        for (int a = 0, size = sourceList.Count; a < size; a++)
        //        {
        //            list.Add(sourceList[a]);
        //        }
        //    }
        //    else if (obj is IEnumerable && !(obj is String))
        //    {
        //        list = AnyToList(((IEnumerable)obj).GetEnumerator());
        //    }
        //    else if (obj is IEnumerator)
        //    {
        //        IEnumerator enumerator = (IEnumerator)obj;
        //        list = new List<Object>();
        //        while (enumerator.MoveNext())
        //        {
        //            list.Add(enumerator.Current);
        //        }
        //    }
        //    else
        //    {
        //        list = new List<Object>(1);
        //        list.Add(obj);
        //    }

        //    return list;
        //}

        public static ISet<Object> AnyToSet(Object obj)
        {
            ISet<Object> set;

            set = new HashSet<Object>(AnyToList(obj));

            return set;
        }

        public static Object AnyToArray(Object obj)
        {
            return AnyToArray(obj, typeof(Object));
        }
        public static Object AnyToArray(Object obj, Type targetElementType)
        {
            if (obj == null)
            {
                return null;
            }
            Type objType = obj.GetType();
            if (objType.IsArray)
            {
                if (targetElementType.Equals(objType.GetElementType()))
                {
                    return obj;
                }
                //We decide to go on and try to convert the elements (maybe we have a object-array which contains only <someclass> elements)
                Array sourceArray = (Array)obj;
                int length = sourceArray.Length;
                Array resultArray = Array.CreateInstance(targetElementType, length);
                try
                {
                    for (int i = length; i-- > 0; )
                    {
                        Object source = sourceArray.GetValue(i);
                        resultArray.SetValue(source, i);
                    }
                }
                catch (Exception e)
                {
                    throw new Exception("Element Types of this this array are not compatible \"" + objType.GetElementType().Name + "\" -> \"" + targetElementType.Name + "\".", e);
                }
                return resultArray;
            }
            MethodInfo toArrayMethod = objType.GetMethod("ToArray");
            if (toArrayMethod == null)
            {
                Object list = AnyToList(obj, targetElementType);
                Type listType = list.GetType();
                toArrayMethod = listType.GetMethod("ToArray");
            }
            if (toArrayMethod != null)
            {
                return AnyToArray(toArrayMethod.Invoke(obj, null), targetElementType);
            }
            throw new Exception("Failed to convert \"" + obj + "\" of type \"" + objType + "\" to Array of type \"" + targetElementType + "\".");
        }

        public static void ClearList(Object list)
        {
            MemberCallDelegate clearMethod = TypeUtility.GetMemberCallDelegate(list.GetType(), "Clear");
            clearMethod(list);
        }

        public static void ClearAndFillList(Object list, IEnumerable values)
        {
            Type type = list.GetType();
            MemberCallDelegate clearMethod = TypeUtility.GetMemberCallDelegate(type, "Clear");
            MemberSetDelegate addMethod = TypeUtility.GetMemberSetDelegate(type, "Add");
            clearMethod(list);
            foreach (Object item in values)
            {
                addMethod(list, item);
            }
        }

        public static void FillList(Object list, IEnumerable values)
        {
            //MemberSetDelegate addMethod = TypeUtility.GetMemberSetDelegate(list.GetType(), "Add");
            MethodInfo addMethod = list.GetType().GetMethod("Add");
            Object[] args = new Object[1];
            foreach (Object item in values)
            {
                //addMethod(list, item);
                args[0] = item;
                addMethod.Invoke(list, args);
            }
        }
    }
}