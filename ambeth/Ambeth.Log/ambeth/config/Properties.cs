using System;
using System.Collections.Generic;
using System.IO;
using System.Text.RegularExpressions;
using System.Threading;
using De.Osthus.Ambeth.Io;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Config
{
    public class Properties : IProperties
    {
        protected static Regex commentRegex = new Regex(" *[#;'].*");

        protected static Regex propertyRegex = new Regex(" *([^= ]+) *(?:=? *(?:(.*)|'(.*)'|\"(.*)\") *)?");

        public static Regex dynamicRegex = new Regex("(.*)\\$\\{([^\\$\\{\\}]+)\\}(.*)");

        public static Properties System { get; private set; }

        public static Properties Application { get; private set; }
        
        static Properties()
        {
            System = new Properties();

            Application = new Properties(System);
        }

        public static void ResetApplication()
        {
            Properties.Application = new Properties(System);
        }

        public static void LoadBootstrapPropertyFile()
	    {
		    Console.WriteLine("Looking for environment property '" + UtilConfigurationConstants.BootstrapPropertyFile + "'...");
		    String bootstrapPropertyFile = Application.GetString(UtilConfigurationConstants.BootstrapPropertyFile);
		    if (bootstrapPropertyFile != null)
		    {
			    Console.WriteLine("Environment property '" + UtilConfigurationConstants.BootstrapPropertyFile + "' found with value '" + bootstrapPropertyFile
					    + "'");
			    Application.Load(bootstrapPropertyFile);
			    Console.WriteLine("External property file '" + bootstrapPropertyFile + "' successfully loaded");
		    }
		    else
		    {
                Console.WriteLine("No Environment property '" + UtilConfigurationConstants.BootstrapPropertyFile
					    + "' found. Skipping search for external bootstrap properties");
		    }
	    }


        protected IDictionary<String, Object> dictionary;

        protected ThreadLocal<ISet<String>> cyclicKeyCheckTL = new ThreadLocal<ISet<String>>(
            delegate()
            {
                return new HashSet<String>();
            }
        );

        public virtual IProperties Parent { get; private set; }

        public Properties() : this((IProperties)null)
        {
            // Intended blank
        }

        public Properties(IProperties parent)
        {
            this.Parent = parent;
            this.dictionary = new Dictionary<String, Object>();
        }

        public Properties(String filepath) : this(filepath, null)
        {
            // Intended blank
        }

        public Properties(String filepath, IProperties parent) : this(parent)
        {
            Load(filepath);
        }

        public Properties(Properties dictionary, IProperties parent) : this(parent)
        {
            Load(dictionary);
        }

        public virtual IEnumerator<KeyValuePair<String, Object>> Iterator()
        {
            return dictionary.GetEnumerator();
        }

        public String ResolvePropertyParts(String value)
        {
            if (value == null)
            {
                return null;
            }
            String currStringValue = value;
            while (true)
            {
                if (!currStringValue.Contains("${"))
                {
                    return currStringValue;
                }
                Match matcher = dynamicRegex.Match(currStringValue);
                if (!matcher.Success)
                {
                    return currStringValue;
                }
                String leftFromVariable = matcher.Groups[1].Value;
                String variableName = matcher.Groups[2].Value;
                String rightFromVariable = matcher.Groups[3].Value;
                ISet<String> cyclicKeyCheck = this.cyclicKeyCheckTL.Value;
                if (cyclicKeyCheck.Contains(variableName))
                {
                    throw new Exception("Cycle detected on dynamic property resolution with name: '" + variableName + "'. This is not supported");
                }
                cyclicKeyCheck.Add(variableName);
                try
                {
                    String resolvedVariable = GetString(variableName);
                    if (resolvedVariable == null)
                    {
                        if (leftFromVariable.Length == 0 && rightFromVariable.Length == 0)
                        {
                            return null;
                        }
                    }
                    currStringValue = leftFromVariable + resolvedVariable + rightFromVariable;
                }
                finally
                {
                    cyclicKeyCheck.Remove(variableName);
                }
            }
        }

        public virtual Object Get(String key)
        {
            return Get<Object>(key);
        }

        public virtual String GetString(String key)
        {
            return Get<String>(key);
        }

        public virtual Object this[String key]
        {
            get {

                Object propertyValue = DictionaryExtension.ValueOrDefault(dictionary, key);
		        if (propertyValue == null && Parent != null)
		        {
			        return Parent.GetString(key);
		        }
		        if (!(propertyValue is String))
		        {
			        return propertyValue;
		        }
		        return ResolvePropertyParts((String) propertyValue);
            }
            set
            {
                PutProperty(key, value);
            }
        }

        public virtual String GetString(String key, String defaultValue)
        {
            String value = GetString(key);
            if (value is String)
            {
                return value;
            }
            return defaultValue;
        }

        public virtual void Set(String key, String value)
        {
            PutProperty(key, value);
        }

        public virtual T Get<T>(String key)
        {
            Object value = this[key];
            if (value is T)
            {
                return (T)value;
            }
            if (value != null && typeof(String).Equals(typeof(T)))
            {
                return (T)(Object)value.ToString();
            }
            return (T)value; //force CCE
        }

        public virtual void Set<T>(String key, T value)
        {
            this[key] = value;
        }

        public virtual IEnumerator<KeyValuePair<String, Object>> GetEnumerator()
        {
            return dictionary.GetEnumerator();
        }

        public virtual ISet<String> CollectAllPropertyKeys()
        {
            ISet<String> allPropertiesSet = new HashSet<String>();
            CollectAllPropertyKeys(allPropertiesSet);
            return allPropertiesSet;
        }

        public virtual void CollectAllPropertyKeys(ISet<String> allPropertiesSet)
        {
            if (Parent != null)
            {
                Parent.CollectAllPropertyKeys(allPropertiesSet);
            }
            DictionaryExtension.Loop(dictionary, delegate(String key, Object value)
            {
                allPropertiesSet.Add(key);
            });
        }

        public virtual void Load(IProperties sourceProperties)
        {
            foreach (String key in sourceProperties.CollectAllPropertyKeys())
            {
                String value = sourceProperties.GetString(key);
                    
                this[key] = value;
            }
        }

        public virtual void Load(Stream stream)
        {
            using (stream)
            using (StreamReader reader = new StreamReader(stream))
            {
                String content = reader.ReadToEnd();
                HandleContent(content);
            }
        }

        public virtual void Load(String filepathSrc)
        {
            Stream[] fileStreams = FileUtil.OpenFileStreams(filepathSrc);
            foreach (Stream fileStream in fileStreams)
            {
                Load(fileStream);
            }
        }

        protected virtual void HandleContent(String content)
        {
            content = content.Replace("\r", "");
            String[] records = content.Split('\n');
            foreach (String record in records)
            {
                if (commentRegex.IsMatch(record))
                {
                    continue;
                }
                Match match = propertyRegex.Match(record);
                if (!match.Success)
                {
                    continue;
                }
                String key = match.Groups[1].Value;
                if (match.Groups.Count > 2)
                {
                    String value = match.Groups[2].Value;
                    if (value == null || value.Length == 0)
                    {
                        value = match.Groups[3].Value;
                    }
                    if (value == null || value.Length == 0)
                    {
                        value = match.Groups[4].Value;
                    }
                    PutProperty(key, value);
                }
            }
        }

        protected virtual void PutProperty(String key, Object value)
        {
            dictionary[key] = value;
        }
    }
}
