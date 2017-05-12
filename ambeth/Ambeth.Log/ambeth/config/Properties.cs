using System;
using System.Collections.Generic;
using System.IO;
using System.Text.RegularExpressions;
using System.Threading;
using De.Osthus.Ambeth.Io;
using De.Osthus.Ambeth.Util;
using System.Text;

namespace De.Osthus.Ambeth.Config
{
    public class Properties : IProperties
    {
        protected static Regex commentRegex = new Regex(" *[#;'].*");

        protected static Regex propertyRegex = new Regex(" *([^= ]+) *(?:=? *(?:(.*)|'(.*)'|\"(.*)\") *)?");

		public static Regex dynamicRegex = new Regex("(.*)\\$\\{([^\\$\\{\\}]+)\\}(.*)", RegexOptions.Singleline);

        public static Properties System { get; private set; }

        public static Properties Application { get; private set; }

        static Properties()
        {
            System = new Properties();
#if !SILVERLIGHT
            String userHome = (Environment.OSVersion.Platform == PlatformID.Unix || Environment.OSVersion.Platform == PlatformID.MacOSX)
                        ? Environment.GetEnvironmentVariable("HOME")
                        : Environment.ExpandEnvironmentVariables("%HOMEDRIVE%%HOMEPATH%");
            System.PutProperty("user.home", userHome);
#endif

            Application = new Properties(System);
        }

        public static void ResetApplication()
        {
            Properties.Application = new Properties(System);
        }

        public static void LoadBootstrapPropertyFile()
	    {
		    Console.WriteLine("Ambeth is looking for environment property '" + UtilConfigurationConstants.BootstrapPropertyFile + "'...");
		    String bootstrapPropertyFile = Application.GetString(UtilConfigurationConstants.BootstrapPropertyFile);
            if (bootstrapPropertyFile == null)
            {
                bootstrapPropertyFile = Application.GetString(UtilConfigurationConstants.BootstrapPropertyFile.ToUpperInvariant());
            }
		    if (bootstrapPropertyFile != null)
		    {
			    Console.WriteLine("  Environment property '" + UtilConfigurationConstants.BootstrapPropertyFile + "' found with value '" + bootstrapPropertyFile
					    + "'");
			    Application.Load(bootstrapPropertyFile, false);
			    Console.WriteLine("  External property file '" + bootstrapPropertyFile + "' successfully loaded");
		    }
		    else
		    {
                Console.WriteLine("  No Environment property '" + UtilConfigurationConstants.BootstrapPropertyFile
					    + "' found. Skipping search for external bootstrap properties");
		    }
	    }


        protected IDictionary<String, Object> dictionary;

        protected readonly ThreadLocal<ISet<String>> cyclicKeyCheckTL = new ThreadLocal<ISet<String>>(
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

        public IEnumerator<KeyValuePair<String, Object>> Iterator()
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

        public void FillWithCommandLineArgs(String[] args)
        {
            StringBuilder sb = new StringBuilder();
            for (int a = args.Length; a-- > 0; )
            {
                String arg = args[a];
                if (sb.Length > 0)
                {
                    sb.Append('\n');
                }
                sb.Append(arg);
            }
            HandleContent(sb.ToString(), true);
        }

        public Object Get(String key)
        {
            return Get(key, this);
        }

        public Object Get(String key, IProperties initiallyCalledProps)
        {
            if (initiallyCalledProps == null)
            {
                initiallyCalledProps = this;
            }
            Object propertyValue = DictionaryExtension.ValueOrDefault(dictionary, key);
		    if (propertyValue == null && Parent != null)
		    {
                return Parent.Get(key, initiallyCalledProps);
		    }
		    if (!(propertyValue is String))
		    {
			    return propertyValue;
		    }
            return initiallyCalledProps.ResolvePropertyParts((String)propertyValue);
        }

        public String GetString(String key)
        {
            return (String) Get(key);
        }

        public Object this[String key]
        {
            get {
                return Get(key);
            }
            set
            {
                PutProperty(key, value);
            }
        }

        public String GetString(String key, String defaultValue)
        {
            String value = GetString(key);
            if (value is String)
            {
                return value;
            }
            return defaultValue;
        }

        public void Set(String key, String value)
        {
            PutProperty(key, value);
        }

        public IEnumerator<KeyValuePair<String, Object>> GetEnumerator()
        {
            return dictionary.GetEnumerator();
        }

        public ISet<String> CollectAllPropertyKeys()
        {
            ISet<String> allPropertiesSet = new HashSet<String>();
            CollectAllPropertyKeys(allPropertiesSet);
            return allPropertiesSet;
        }

        public void CollectAllPropertyKeys(ISet<String> allPropertiesSet)
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

        public void Load(IProperties sourceProperties)
        {
            foreach (String key in sourceProperties.CollectAllPropertyKeys())
            {
                String value = sourceProperties.GetString(key);
                    
                this[key] = value;
            }
        }

        public void Load(Stream stream, bool overwriteParentExisting)
        {
            using (stream)
            using (StreamReader reader = new StreamReader(stream))
            {
                String content = reader.ReadToEnd();
                HandleContent(content, overwriteParentExisting);
            }
        }

        public void Load(String filepathSrc)
        {
            Load(filepathSrc, true);
        }

        public void Load(String filepathSrc, bool overwriteParentExisting)
        {
            Stream[] fileStreams = FileUtil.OpenFileStreams(filepathSrc);
            foreach (Stream fileStream in fileStreams)
            {
                Load(fileStream, overwriteParentExisting);
            }
        }

        protected void HandleContent(String content, bool overwriteParentExisting)
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
                Object value = null;
                if (match.Groups.Count > 2)
                {
                    String stringValue = match.Groups[2].Value;
                    if (stringValue == null || stringValue.Length == 0)
                    {
                        stringValue = match.Groups[3].Value;
                    }
                    if (stringValue == null || stringValue.Length == 0)
                    {
                        stringValue = match.Groups[4].Value;
                    }
                    value = stringValue;
                }
                else
                {
                    value = match.Groups[2].Value;
                }
                if (!overwriteParentExisting && Get(key) != null)
                {
                    continue;
                }
				if (value == null)
				{
					value = "";
				}
                PutProperty(key, value);
            }
        }

        protected void PutProperty(String key, Object value)
        {
            dictionary[key] = value;
        }
    }
}
