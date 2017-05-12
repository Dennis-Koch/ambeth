using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Merge.Transfer
{
    [DataContract(Name = "CUDResult", Namespace = "http://schemas.osthus.de/Ambeth")]
    public partial class CUDResult : ICUDResult
    {
#if !SILVERLIGHT
        [NonSerialized]
#endif
        [IgnoreDataMember]
        protected IDictionary<Type, IList<IChangeContainer>> typeToModDict;

#if !SILVERLIGHT
        [NonSerialized]
#endif
        [IgnoreDataMember]
        protected IList<Object> originalRefs;

        [DataMember]
        public IList<IChangeContainer> AllChanges { get; set; }

        [IgnoreDataMember]
        protected IList<AbstractChangeContainer> tallChanges = new List<AbstractChangeContainer>();

        public CUDResult()
        {
            AllChanges = new List<IChangeContainer>();
        }

        public CUDResult(IList<IChangeContainer> allChanges, IList<Object> originalRefs)
        {
            this.AllChanges = allChanges;
            this.originalRefs = originalRefs;

            tallChanges = new List<AbstractChangeContainer>(allChanges.Count);
            for (int a = 0, size = allChanges.Count; a < size; a++)
            {
                tallChanges.Add((AbstractChangeContainer)allChanges[a]);
            }
        }

        public IList<Object> GetOriginalRefs()
        {
            return originalRefs;
        }

        public IList<IChangeContainer> GetChanges(Type type)
        {
            if (typeToModDict != null)
            {
                return DictionaryExtension.ValueOrDefault(typeToModDict, type);
            }
            typeToModDict = new Dictionary<Type, IList<IChangeContainer>>();

            for (int a = AllChanges.Count; a-- > 0; )
            {
                IChangeContainer changeContainer = AllChanges[a];
                Type realType = changeContainer.Reference.RealType;
                IList<IChangeContainer> modList = DictionaryExtension.ValueOrDefault(typeToModDict, realType);
                if (modList == null)
                {
                    modList = new List<IChangeContainer>();
                    typeToModDict.Add(realType, modList);
                }
                modList.Add(changeContainer);
            }
            return DictionaryExtension.ValueOrDefault(typeToModDict, type);
        }
    }
}
