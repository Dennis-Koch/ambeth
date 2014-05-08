using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Log;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
    public class ClassTupleEntry<V> : HashMap<ConversionKey, Object>
    {
        public readonly HashMap<ConversionKey, Object> typeToDefEntryMap = new HashMap<ConversionKey, Object>(0.5f);

        public readonly HashMap<Strong2Key<V>, List<Def2Entry<V>>> definitionReverseMap = new HashMap<Strong2Key<V>, List<Def2Entry<V>>>(0.5f);

        public ClassTupleEntry() : base(0.5f)
        {
            // Intended blank
        }
    }

    public class ClassTupleExtendableContainer<V> : MapExtendableContainer<ConversionKey, V>
    {
        protected static readonly Object alreadyHandled = new Object();

        protected volatile ClassTupleEntry<V> classEntry = new ClassTupleEntry<V>();

        public ClassTupleExtendableContainer(String message, String keyMessage)
            : base(message, keyMessage)
        {
            // Intended blank
        }

        public ClassTupleExtendableContainer(String message, String keyMessage, bool multiValue)
            : base(message, keyMessage, multiValue)
        {
            // Intended blank
        }

        public V GetExtension(Type sourceType, Type targetType)
        {
            return GetExtension(new ConversionKey(sourceType, targetType));
        }

        public override V GetExtension(ConversionKey key)
        {
            Object extension = this.classEntry.Get(key);
            if (extension == null)
            {
                Object writeLock = GetWriteLock();
                lock (writeLock)
                {
                    extension = this.classEntry.Get(key);
                    if (extension == null)
                    {
                        ClassTupleEntry<V> classEntry = CopyStructure();

                        classEntry.Put(key, alreadyHandled);
                        classEntry.typeToDefEntryMap.Put(key, alreadyHandled);
                        CheckToWeakRegisterExistingExtensions(key, classEntry);
                        this.classEntry = classEntry;

                        extension = classEntry.Get(key);
                        if (extension == null)
                        {
                            return default(V);
                        }
                    }
                }
            }
            if (Object.ReferenceEquals(extension, alreadyHandled))
            {
                // Already tried
                return default(V);
            }
            return (V)extension;
        }

        protected ClassTupleEntry<V> CopyStructure()
        {
            ClassTupleEntry<V> newClassEntry = new ClassTupleEntry<V>();
            HashMap<ConversionKey, Object> newTypeToDefEntryMap = newClassEntry.typeToDefEntryMap;
            HashMap<Strong2Key<V>, List<Def2Entry<V>>> newDefinitionReverseMap = newClassEntry.definitionReverseMap;
            IdentityHashMap<Def2Entry<V>, Def2Entry<V>> originalToCopyMap = new IdentityHashMap<Def2Entry<V>, Def2Entry<V>>();
            foreach (Entry<ConversionKey, Object> entry in classEntry.typeToDefEntryMap)
            {
                ConversionKey key = entry.Key;
                Object value = entry.Value;

                if (Object.ReferenceEquals(value, alreadyHandled))
                {
                    newTypeToDefEntryMap.Put(key, alreadyHandled);
                }
                else
                {
                    InterfaceFastList<Def2Entry<V>> list = (InterfaceFastList<Def2Entry<V>>)value;

                    InterfaceFastList<Def2Entry<V>> newList = new InterfaceFastList<Def2Entry<V>>();

                    IListElem<Def2Entry<V>> pointer = list.First;
                    while (pointer != null)
                    {
                        Def2Entry<V> defEntry = pointer.ElemValue;
                        Def2Entry<V> newDefEntry = new Def2Entry<V>(defEntry.extension, defEntry.sourceType, defEntry.targetType, defEntry.sourceDistance, defEntry.targetDistance);
                        originalToCopyMap.Put(defEntry, newDefEntry);

                        newList.PushLast(newDefEntry);
                        pointer = pointer.Next;
                    }
                    newTypeToDefEntryMap.Put(key, newList);
                }
                TypeToDefEntryMapChanged(newClassEntry, key);
            }
            foreach (Entry<Strong2Key<V>, List<Def2Entry<V>>> entry in classEntry.definitionReverseMap)
            {
                List<Def2Entry<V>> defEntries = entry.Value;
                List<Def2Entry<V>> newDefEntries = new List<Def2Entry<V>>(defEntries.Count);

                for (int a = 0, size = defEntries.Count; a < size; a++)
                {
                    Def2Entry<V> newDefEntry = originalToCopyMap.Get(defEntries[a]);
                    if (newDefEntry == null)
                    {
                        throw new Exception("Must never happen");
                    }
                    newDefEntries.Add(newDefEntry);
                }
                newDefinitionReverseMap.Put(entry.Key, newDefEntries);
            }
            return newClassEntry;
        }

        protected bool CheckToWeakRegisterExistingExtensions(ConversionKey conversionKey, ClassTupleEntry<V> classEntry)
        {
            bool changesHappened = false;
            foreach (Entry<Strong2Key<V>, List<Def2Entry<V>>> entry in classEntry.definitionReverseMap)
            {
                Strong2Key<V> strongKey = entry.Key;
                ConversionKey registeredStrongKey = strongKey.key;
                int sourceDistance = ClassExtendableContainer<V>.GetDistanceForType(conversionKey.SourceType, registeredStrongKey.SourceType);
                if (sourceDistance == ClassExtendableContainer<V>.NO_VALID_DISTANCE)
                {
                    continue;
                }
                int targetDistance = ClassExtendableContainer<V>.GetDistanceForType(registeredStrongKey.TargetType, conversionKey.TargetType);
                if (targetDistance == ClassExtendableContainer<V>.NO_VALID_DISTANCE)
                {
                    continue;
                }
                List<Def2Entry<V>> defEntries = entry.Value;
                for (int a = defEntries.Count; a-- > 0; )
                {
                    Def2Entry<V> defEntry = defEntries[a];
                    changesHappened |= AppendRegistration(registeredStrongKey, conversionKey, defEntry.extension, sourceDistance, targetDistance, classEntry);
                }
            }
            return changesHappened;
        }

        protected bool CheckToWeakRegisterExistingTypes(ConversionKey key, V extension, ClassTupleEntry<V> classEntry)
        {
            bool changesHappened = false;
            foreach (Entry<ConversionKey, Object> entry in classEntry.typeToDefEntryMap)
            {
                ConversionKey existingRequestedKey = entry.Key;
                int sourceDistance = ClassExtendableContainer<V>.GetDistanceForType(existingRequestedKey.SourceType, key.SourceType);
                if (sourceDistance == ClassExtendableContainer<V>.NO_VALID_DISTANCE)
                {
                    continue;
                }
                int targetDistance = ClassExtendableContainer<V>.GetDistanceForType(key.TargetType, existingRequestedKey.TargetType);
                if (targetDistance == ClassExtendableContainer<V>.NO_VALID_DISTANCE)
                {
                    continue;
                }
                changesHappened |= AppendRegistration(key, existingRequestedKey, extension, sourceDistance, targetDistance, classEntry);
            }
            return changesHappened;
        }

        public void Register(V extension, Type sourceType, Type targetType)
        {
            ParamChecker.AssertParamNotNull(sourceType, "sourceType");
            ParamChecker.AssertParamNotNull(targetType, "targetType");
            Register(extension, new ConversionKey(sourceType, targetType));
        }

        public override void Register(V extension, ConversionKey key)
        {
            ParamChecker.AssertParamNotNull(extension, "extension");
            ParamChecker.AssertParamNotNull(key, "key");
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                base.Register(extension, key);

                ClassTupleEntry<V> classEntry = CopyStructure();
                AppendRegistration(key, key, extension, 0, 0, classEntry);
                CheckToWeakRegisterExistingTypes(key, extension, classEntry);
                CheckToWeakRegisterExistingExtensions(key, classEntry);
                this.classEntry = classEntry;
            }
        }

        public void Unregister(V extension, Type sourceType, Type targetType)
        {
            ParamChecker.AssertParamNotNull(sourceType, "sourceType");
            ParamChecker.AssertParamNotNull(targetType, "targetType");
            Unregister(extension, new ConversionKey(sourceType, targetType));
        }

        public override void Unregister(V extension, ConversionKey key)
        {
            ParamChecker.AssertParamNotNull(extension, "extension");
            ParamChecker.AssertParamNotNull(key, "key");

            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                base.Unregister(extension, key);

                ClassTupleEntry<V> classEntry = CopyStructure();
                HashMap<Strong2Key<V>, List<Def2Entry<V>>> definitionReverseMap = classEntry.definitionReverseMap;
                List<Def2Entry<V>> weakEntriesOfStrongType = definitionReverseMap.Remove(new Strong2Key<V>(extension, key));
                if (weakEntriesOfStrongType == null)
                {
                    return;
                }
                HashMap<ConversionKey, Object> typeToDefEntryMap = classEntry.typeToDefEntryMap;
                for (int a = weakEntriesOfStrongType.Count; a-- > 0; )
                {
                    Def2Entry<V> defEntry = weakEntriesOfStrongType[a];
                    ConversionKey registeredKey = new ConversionKey(defEntry.sourceType, defEntry.targetType);
                    Object value = typeToDefEntryMap.Get(registeredKey);
                    InterfaceFastList<Def2Entry<V>> list = (InterfaceFastList<Def2Entry<V>>)value;
                    list.Remove(defEntry);
                    if (list.Count == 0)
                    {
                        typeToDefEntryMap.Remove(registeredKey);
                    }
                    TypeToDefEntryMapChanged(classEntry, registeredKey);
                }
                this.classEntry = classEntry;
            }
        }

        protected void TypeToDefEntryMapChanged(ClassTupleEntry<V> classEntry, ConversionKey key)
	    {
		    Object obj = classEntry.typeToDefEntryMap.Get(key);
		    if (obj == null)
		    {
			    classEntry.Remove(key);
			    return;
		    }
		    if (obj == alreadyHandled)
		    {
			    classEntry.Put(key, alreadyHandled);
			    return;
		    }
		    if (obj is Def2Entry<V>)
		    {
			    classEntry.Put(key, ((Def2Entry<V>) obj).extension);
			    return;
		    }
		    Def2Entry<V> firstDefEntry = ((InterfaceFastList<Def2Entry<V>>) obj).First.ElemValue;
		    classEntry.Put(key, firstDefEntry.extension);
	    }

        protected bool AppendRegistration(ConversionKey strongTypeKey, ConversionKey key, V extension, int sourceDistance, int targetDistance,
                ClassTupleEntry<V> classEntry)
        {
            HashMap<ConversionKey, Object> typeToDefEntryMap = classEntry.typeToDefEntryMap;
            Object fastList = typeToDefEntryMap.Get(key);
            if (fastList != null && fastList != alreadyHandled)
            {
                IListElem<Def2Entry<V>> pointer = ((InterfaceFastList<Def2Entry<V>>)fastList).First;
                while (pointer != null)
                {
                    Def2Entry<V> existingDefEntry = pointer.ElemValue;
                    if (Object.ReferenceEquals(existingDefEntry.extension, extension) && existingDefEntry.sourceDistance == sourceDistance
                            && existingDefEntry.targetDistance == targetDistance)
                    {
                        // DefEntry already exists with same distance
                        return false;
                    }
                    pointer = pointer.Next;
                }
            }
            if (fastList == null || Object.ReferenceEquals(fastList, alreadyHandled))
            {
                fastList = new InterfaceFastList<Def2Entry<V>>();
                typeToDefEntryMap.Put(key, fastList);
            }
            Def2Entry<V> defEntry = new Def2Entry<V>(extension, key.SourceType, key.TargetType, sourceDistance, targetDistance);

            HashMap<Strong2Key<V>, List<Def2Entry<V>>> definitionReverseMap = classEntry.definitionReverseMap;
            Strong2Key<V> strongKey = new Strong2Key<V>(extension, strongTypeKey);
            List<Def2Entry<V>> typeEntries = definitionReverseMap.Get(strongKey);
            if (typeEntries == null)
            {
                typeEntries = new List<Def2Entry<V>>();
                definitionReverseMap.Put(strongKey, typeEntries);
            }
            typeEntries.Add(defEntry);

            InterfaceFastList<Def2Entry<V>>.InsertOrdered((InterfaceFastList<Def2Entry<V>>)fastList, defEntry);
            TypeToDefEntryMapChanged(classEntry, key);
            return true;
        }
    }
}