using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.Config
{
    public interface IProperties
    {
        IProperties Parent { get; }

        Object Get(String key);

        Object Get(String key, IProperties initiallyCalledProps);

        String GetString(String key);

        String GetString(String key, String defaultValue);

        String ResolvePropertyParts(String value);

        IEnumerator<KeyValuePair<String, Object>> Iterator();

        ISet<String> CollectAllPropertyKeys();

        void CollectAllPropertyKeys(ISet<String> allPropertiesSet);
    }
}
