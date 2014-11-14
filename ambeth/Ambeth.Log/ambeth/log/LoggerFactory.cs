using De.Osthus.Ambeth.Config;
using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;

namespace De.Osthus.Ambeth.Log
{
    public sealed class LoggerFactory
    {
        public static readonly String logLevelPropertyName = "level";

        public static readonly String logConsolePropertyName = "console";

        public static readonly String logSourcePropertyName = "source";

        public static readonly String ambethLogPrefix = "ambeth.log";

        private static readonly Regex logLevelRegex = new Regex(@"ambeth\.log\.(" + logLevelPropertyName + "|" + logConsolePropertyName + "|" + logSourcePropertyName + @")(?:\.(.+))?");

        private static Type loggerType;

        public static Type LoggerType
        {
            get
            {
                return loggerType;
            }
            set
            {
                if (value == null || !typeof(ILogger).IsAssignableFrom(value))
                {
                    throw new ArgumentException("LoggerType must be derived from '" + typeof(ILogger).Namespace + "." + typeof(ILogger).Name + "'");
                }
                loggerType = value;
            }
        }

        static LoggerFactory()
        {
            LoggerType = typeof(Logger);
        }

        public static String LogConsoleProperty(Type type)
        {
            return ambethLogPrefix + "." + logConsolePropertyName + "." + type.FullName;
        }

        public static String LogLevelProperty(Type type)
        {
            return ambethLogPrefix + "." + logLevelPropertyName + "." + type.FullName;
        }

        public static String LogSourceProperty(Type type)
        {
            return ambethLogPrefix + "." + logSourcePropertyName + "." + type.FullName;
        }

        public static ILogger GetLogger(String source)
        {
            return (ILogger)Activator.CreateInstance(LoggerType, source);
        }

        public static ILogger GetLogger<T>()
        {
            return GetLogger(typeof(T));
        }

        public static ILogger GetLogger(Type source)
        {
            return GetLogger(source, Properties.Application);
        }

        public static ILogger GetLogger(Type source, IProperties props)
        {
            ILogger logger = (ILogger)Activator.CreateInstance(LoggerType, source.Namespace + "." + source.Name);

            if (logger is IConfigurableLogger)
            {
                ConfigureLogger(source, (IConfigurableLogger)logger, props);
            }
            return logger;
        }

        public static void ConfigureLogger(Type source, IConfigurableLogger logger, IProperties appProps)
        {
            String loggerName = source.Namespace + "." + source.Name;
            ConfigureLogger(loggerName, logger, appProps);
        }

        public static void ConfigureLogger(String loggerName, IConfigurableLogger logger, IProperties appProps)
        {
            HashSet<String> allPropertiesSet = new HashSet<String>();
            appProps.CollectAllPropertyKeys(allPropertiesSet);
            // highest precision found
            int logLevelHPF = 0, logConsoleHPF = 0, logSourceHPF = 0;
            foreach (String key in allPropertiesSet)
            {
                Match match = logLevelRegex.Match(key);
                if (!match.Success)
                {
                    continue;
                }
                String type = match.Groups[1].Value;
                String target = match.Groups[2].Value;
                if (target != null && !loggerName.StartsWith(target))
                {
                    continue;
                }
                if (target == null)
                {
                    target = "";
                }
                if (logLevelPropertyName.Equals(type))
                {
                    if (target.Length < logLevelHPF)
                    {
                        continue;
                    }
                    logLevelHPF = target.Length;
                    String valueString = appProps.GetString(key).ToLower();
                    if ("debug".Equals(valueString))
                    {
                        logger.DebugEnabled = true;
                        logger.InfoEnabled = true;
                        logger.WarnEnabled = true;
                        logger.ErrorEnabled = true;
                    }
                    else if ("info".Equals(valueString))
                    {
                        logger.DebugEnabled = false;
                        logger.InfoEnabled = true;
                        logger.WarnEnabled = true;
                        logger.ErrorEnabled = true;
                    }
                    else if ("warn".Equals(valueString))
                    {
                        logger.DebugEnabled = false;
                        logger.InfoEnabled = false;
                        logger.WarnEnabled = true;
                        logger.ErrorEnabled = true;
                    }
                    else if ("error".Equals(valueString))
                    {
                        logger.DebugEnabled = false;
                        logger.InfoEnabled = false;
                        logger.WarnEnabled = false;
                        logger.ErrorEnabled = true;
                    }
                }
                else if (logConsolePropertyName.Equals(type))
                {
                    if (target.Length < logConsoleHPF)
                    {
                        continue;
                    }
                    logConsoleHPF = target.Length;
                    String valueString = appProps.GetString(key).ToLower();
                    logger.LogToConsole = Boolean.Parse(valueString);
                }
                else if (logSourcePropertyName.Equals(type))
                {
                    if (target.Length < logSourceHPF)
                    {
                        continue;
                    }
                    logSourceHPF = target.Length;
                    String valueString = appProps.GetString(key);
                    logger.LogSourceLevel = (LogSourceLevel)Enum.Parse(typeof(LogSourceLevel), valueString, true);
                }
                else
                {
                    throw new Exception("Property: " + key + " not supported");
                }
            }
            logger.PostProcess(appProps);
        }

        private LoggerFactory()
        {
            // intended blank
        }
    }
}