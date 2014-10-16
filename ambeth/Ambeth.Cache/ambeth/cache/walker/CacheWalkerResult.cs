using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Util;
using System;
using System.Runtime.CompilerServices;
using System.Text;

namespace De.Osthus.Ambeth.Walker
{
    public class CacheWalkerResult : ICacheWalkerResult
    {
        protected static readonly char pipe = "\u2514".ToCharArray()[0];

        protected readonly ICache cache;

        protected readonly bool privileged;

        protected readonly bool transactional;

        protected readonly bool threadLocal;

        protected readonly IObjRef[] objRefs;

        protected readonly Object[] cacheValues;

        protected readonly bool[] pendingChanges;

        protected CacheWalkerResult parentEntry;

        protected readonly Object childEntries;

        public CacheWalkerResult(ICache cache, bool transactional, bool threadLocal, IObjRef[] objRefs, Object[] cacheValues, Object childEntries)
        {
            this.cache = cache;
            this.transactional = transactional;
            this.threadLocal = threadLocal;
            this.objRefs = objRefs;
            this.cacheValues = cacheValues;
            this.childEntries = childEntries;
            bool[] pendingChanges = null;
            for (int a = cacheValues.Length; a-- > 0; )
            {
                Object cacheValue = cacheValues[a];
                if (cacheValue == null || cacheValue is AbstractCacheValue)
                {
                    continue;
                }
                if (pendingChanges == null)
                {
                    pendingChanges = new bool[cacheValues.Length];
                }
                pendingChanges[a] = ((IDataObject)cacheValue).HasPendingChanges;
            }
            this.pendingChanges = pendingChanges;
            privileged = cache.Privileged;
        }

        public CacheWalkerResult ParentEntry { get; set; }

        public ICache Cache
        {
            get
            {
                return cache;
            }
        }

        public bool Privileged
        {
            get
            {
                return privileged;
            }
        }

        public bool Transactional
        {
            get
            {
                return transactional;
            }
        }

        public bool ThreadLocal
        {
            get
            {
                return threadLocal;
            }
        }

        public Object[] CacheValues
        {
            get
            {
                return cacheValues;
            }
        }

        public Object ChildEntries
        {
            get
            {
                return childEntries;
            }
        }

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            ToString(sb, 0);
        }

        public void ToString(StringBuilder sb, int tabCount)
        {
            ToStringIntern(sb, new PrintableDelegateWrapper(new PrintableDelegate(delegate(StringBuilder sb2)
                {
                    sb.Append(Environment.NewLine);
                    StringBuilderUtil.AppendTabs(sb, tabCount);
                })), null);
        }

        protected void ToStringIntern(StringBuilder sb, IPrintable preSpace, bool? entityDescriptionAtRoot)
        {
            Object[] cacheValues = this.cacheValues;

            if (entityDescriptionAtRoot == null)
            {
                IObjRef[] objRefs = this.objRefs;
                for (int a = 0, size = objRefs.Length; a < size; a++)
                {
                    IObjRef objRef = objRefs[a];
                    sb.Append(pipe).Append(' ').Append(a + 1);
                    sb.Append(". Type=").Append(objRef.RealType.Name);
                    sb.Append(" Id(");
                    if (objRef.IdNameIndex == ObjRef.PRIMARY_KEY_INDEX)
                    {
                        sb.Append("PK");
                    }
                    else
                    {
                        sb.Append("AK-").Append(objRef.IdNameIndex);
                    }
                    sb.Append(")=").Append(objRef.Id);
                    entityDescriptionAtRoot = true;
                    preSpace.ToString(sb);
                }
            }
            sb.Append(pipe).Append(" Cache");
            bool firstSuffix = true;
            if (!privileged)
            {
                firstSuffix = AppendSuffix("SEC", firstSuffix, sb);
            }
            if (transactional)
            {
                firstSuffix = AppendSuffix("TX", firstSuffix, sb);
            }
            if (threadLocal)
            {
                firstSuffix = AppendSuffix("L", firstSuffix, sb);
            }
            if (parentEntry == null)
            {
                firstSuffix = AppendSuffix("G", firstSuffix, sb);
            }
            sb.Append("--#0x").Append(ToHexString(cache));

            IPrintable oldPreSpace = preSpace;
            preSpace = new PrintableDelegateWrapper(new PrintableDelegate(delegate(StringBuilder sb2)
                {
                    oldPreSpace.ToString(sb2);
                    sb2.Append('\t');
                }));
            IPrintable preSpaceForCacheValue;
            if (childEntries == null)
            {
                preSpaceForCacheValue = new PrintableDelegateWrapper(new PrintableDelegate(delegate(StringBuilder sb2)
                    {
                        preSpace.ToString(sb2);
                        sb2.Append("   ");
                    }));
            }
            else
            {
                preSpaceForCacheValue = new PrintableDelegateWrapper(new PrintableDelegate(delegate(StringBuilder sb2)
                    {
                        preSpace.ToString(sb2);
                        sb2.Append("|  ");
                    }));
            }
            for (int a = 0, size = cacheValues.Length; a < size; a++)
            {
                preSpaceForCacheValue.ToString(sb);
                sb.Append(a + 1).Append('.');
                Object cacheValue = cacheValues[a];
                if (cacheValue == null)
                {
                    sb.Append(" n/a");
                    continue;
                }
                IEntityMetaData metaData = ((IEntityMetaDataHolder)cacheValue).Get__EntityMetaData();
                Object id, version = null;
                bool hasVersion = metaData.VersionMember != null;
                bool hasPendingChanges = false;
                if (cacheValue is AbstractCacheValue)
                {
                    AbstractCacheValue cacheValueCasted = (AbstractCacheValue)cacheValue;
                    id = cacheValueCasted.Id;
                    if (hasVersion)
                    {
                        version = cacheValueCasted.Version;
                    }
                }
                else
                {
                    id = metaData.IdMember.GetValue(cacheValue);
                    if (hasVersion)
                    {
                        version = metaData.VersionMember.GetValue(cacheValue);
                    }
                    hasPendingChanges = this.pendingChanges != null && this.pendingChanges[a];
                }
                if (!entityDescriptionAtRoot.HasValue || !entityDescriptionAtRoot.Value)
                {
                    sb.Append(" Type=").Append(metaData.EntityType.Name);
                    sb.Append(" Id=").Append(id);
                }
                if (hasVersion)
                {
                    sb.Append(" Version=").Append(version);
                }
                if (hasPendingChanges)
                {
                    sb.Append(" (m)");
                }
            }
            if (this.childEntries is CacheWalkerResult[])
            {
                CacheWalkerResult[] childEntries2 = (CacheWalkerResult[])this.childEntries;
                for (int a = 0, size = childEntries2.Length; a < size; a++)
                {
                    bool hasSuccessor = a < size - 1;
                    CacheWalkerResult entry = childEntries2[a];

                    IPrintable preSpaceForChildEntry = new PrintableDelegateWrapper(new PrintableDelegate(delegate(StringBuilder sb2)
                        {
                            preSpace.ToString(sb2);
                            if (hasSuccessor)
                            {
                                sb2.Append("|");
                            }
                        }));
                    preSpace.ToString(sb);
                    entry.ToStringIntern(sb, preSpaceForChildEntry, entityDescriptionAtRoot);
                }
            }
            else if (this.childEntries != null)
            {
                CacheWalkerResult entry = (CacheWalkerResult)this.childEntries;
                preSpace.ToString(sb);
                entry.ToStringIntern(sb, preSpace, entityDescriptionAtRoot);
            }
        }

        protected bool AppendSuffix(String suffix, bool isFirstSuffix, StringBuilder sb)
        {
            sb.Append('-');
            sb.Append(suffix);
            return false;
        }

        protected String ToHexString(Object obj)
        {
            if (obj == null)
            {
                return null;
            }
            return RuntimeHelpers.GetHashCode(obj).ToString("x");
        }
    }
}