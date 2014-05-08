using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.Util
{
    public class ImmutableTypeSet
    {
	    protected static readonly CHashSet<Type> valueTypeSet = new CHashSet<Type>(0.5f);

        protected static readonly HashMap<Type, Type> wrapperTypesMap = new HashMap<Type, Type>(0.5f);

	    static ImmutableTypeSet()
	    {
            wrapperTypesMap.Put(typeof(Int64?), typeof(Int64));
            wrapperTypesMap.Put(typeof(UInt64?), typeof(UInt64));
            wrapperTypesMap.Put(typeof(Int32?), typeof(Int32));
            wrapperTypesMap.Put(typeof(UInt32?), typeof(UInt32));
            wrapperTypesMap.Put(typeof(Int16?), typeof(Int16));
            wrapperTypesMap.Put(typeof(UInt16?), typeof(UInt16));
            wrapperTypesMap.Put(typeof(Byte?), typeof(Byte));
            wrapperTypesMap.Put(typeof(SByte?), typeof(SByte));
            wrapperTypesMap.Put(typeof(Double?), typeof(Double));
            wrapperTypesMap.Put(typeof(Single?), typeof(Single));
            wrapperTypesMap.Put(typeof(Char?), typeof(Char));
            wrapperTypesMap.Put(typeof(Boolean?), typeof(Boolean));

            valueTypeSet.Add(typeof(Int64));
            valueTypeSet.Add(typeof(Int64?));
            valueTypeSet.Add(typeof(Int32));
            valueTypeSet.Add(typeof(Int32?));
            valueTypeSet.Add(typeof(Int16));
            valueTypeSet.Add(typeof(Int16?));
            valueTypeSet.Add(typeof(UInt64));
            valueTypeSet.Add(typeof(UInt64?));
            valueTypeSet.Add(typeof(UInt32));
            valueTypeSet.Add(typeof(UInt32?));
            valueTypeSet.Add(typeof(UInt16));
            valueTypeSet.Add(typeof(UInt16?));
            valueTypeSet.Add(typeof(Byte));
            valueTypeSet.Add(typeof(Byte?));
            valueTypeSet.Add(typeof(SByte));
            valueTypeSet.Add(typeof(SByte?));
            valueTypeSet.Add(typeof(Char));
            valueTypeSet.Add(typeof(Char?));
            valueTypeSet.Add(typeof(Boolean));
            valueTypeSet.Add(typeof(Boolean?));
            valueTypeSet.Add(typeof(Double));
            valueTypeSet.Add(typeof(Double?));
            valueTypeSet.Add(typeof(Single));
            valueTypeSet.Add(typeof(Single?));
            valueTypeSet.Add(typeof(String));
            valueTypeSet.Add(typeof(Type));
            valueTypeSet.Add(typeof(void));
	    }

	    public static void AddImmutableTypesTo(IList<Type> collection)
	    {
            foreach (Type valueType in valueTypeSet)
            {
		        collection.Add(valueType);
            }
	    }

        public static void AddImmutableTypesTo(IICollection<Type> collection)
        {
            foreach (Type valueType in valueTypeSet)
            {
                collection.Add(valueType);
            }
        }

	    public static bool IsImmutableType(Type type)
	    {
		    return type.IsPrimitive || type.IsValueType || type.IsEnum || valueTypeSet.Contains(type) || "RuntimeType".Equals(type.Name);
	    }

	    private ImmutableTypeSet()
	    {
		    // Intended blank
	    }

        public static Type GetUnwrappedType(Type wrapperType)
	    {
		    return wrapperTypesMap.Get(wrapperType);
	    }
    }
}
