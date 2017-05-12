using System;
using System.Collections.Generic;
using System.Reflection;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System.Collections;

namespace De.Osthus.Ambeth.Cache
{
    public class RevertChangesSavepoint : IRevertChangesSavepoint, IInitializingBean
    {
        public interface IBackup
        {
            void Restore(Object target);
        }

        public class ObjectBackup : IBackup
        {
            protected readonly ITypeInfoItem[] allMembers;

            protected readonly Object[] values;

            public ObjectBackup(ITypeInfoItem[] allMembers, Object[] values)
            {
                this.allMembers = allMembers;
                this.values = values;
            }

            public void Restore(Object target)
            {
                for (int b = allMembers.Length; b-- > 0; )
                {
                    ITypeInfoItem member = allMembers[b];
                    Object originalValue = values[b];
                    if(! Equals(member.GetValue(target), originalValue))
                    {
                        member.SetValue(target, originalValue);
                    }
                }
            }
        }

        public class FieldBasedBackup : IBackup
        {
            protected readonly FieldInfo[] fields;

            protected readonly Object[] values;

            public FieldBasedBackup(FieldInfo[] fields, Object[] values)
            {
                this.fields = fields;
                this.values = values;
            }

            public void Restore(Object target)
            {
                for (int b = fields.Length; b-- > 0; )
                {
                    FieldInfo field = fields[b];
                    Object originalValue = values[b];
                    field.SetValue(target, originalValue);
                }
            }
        }

        public class ArrayBackup : IBackup
        {
            protected readonly Array arrayClone;

            public ArrayBackup(Array arrayClone)
            {
                this.arrayClone = arrayClone;
            }

            public void Restore(Object target)
            {
                Array targetArray = (Array)target;
                int length = arrayClone.Length;
                Array.Copy(arrayClone, targetArray, length);
            }
        }

        public class ListBackup : IBackup
        {
            protected readonly Object[] arrayClone;

            public ListBackup(Object[] arrayClone)
            {
                this.arrayClone = arrayClone;
            }

            public void Restore(Object target)
            {
                IList targetList = (IList)target;
                while (targetList.Count > arrayClone.Length)
                {
                    targetList.RemoveAt(targetList.Count - 1);
                }
                for (int index = 0, size = arrayClone.Length; index < size; index++)
                {
                    Object itemClone = arrayClone[index];
                    if (targetList.Count <= index)
                    {
                        targetList.Add(itemClone);
                        continue;
                    }
                    if (!Object.ReferenceEquals(targetList[index], itemClone))
                    {
                        // Set only if the reference is really different
                        // This is due to the fact that some list implementations fire
                        // PropertyChangeEvents even if the index value references did not change
                        targetList[index] = itemClone;
                    }
                }
            }
        }

        public WeakDictionary<Object, IBackup> Changes { protected get; set; }

        public ICacheModification CacheModification { protected get; set; }

        protected readonly DateTime savepointTime = DateTime.Now;

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(Changes, "Changes");
            ParamChecker.AssertNotNull(CacheModification, "CacheModification");
        }

        public virtual void Dispose()
        {
            Changes = null;
        }

        public virtual Object[] GetSavedBusinessObjects()
        {
            ICollection<Object> objects = Changes.Keys;
            Object[] array = new Object[objects.Count];
            objects.CopyTo(array, 0);
            return array;
        }

        public virtual void RevertChanges()
        {
            if (Changes == null)
            {
                throw new Exception("This object has already been disposed");
            }
            bool oldCacheModificationValue = CacheModification.Active;
            CacheModification.Active = true;
            try
            {
                foreach (var kvPair in Changes.KeyValuePairs)
                {
                    kvPair.Value.Restore(kvPair.Key);
                }
            }
            finally
            {
                CacheModification.Active = oldCacheModificationValue;
            }
        }
    }
}
