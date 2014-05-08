using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Exceptions;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Link;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Threading;

namespace De.Osthus.Ambeth.Ioc.Config
{
    public abstract class AbstractPropertyConfiguration : IPropertyConfiguration
    {
        public static readonly CHashSet<String> ignoreClassNames = new CHashSet<String>(0.5f);

        static AbstractPropertyConfiguration()
        {
            //ignoreClassNames.Add(typeof(Thread)..class.getName());
            ignoreClassNames.Add(typeof(AbstractPropertyConfiguration).FullName);
            ignoreClassNames.Add(typeof(BeanContextFactory).FullName);
            ignoreClassNames.Add(typeof(LinkController).FullName);
            ignoreClassNames.Add(typeof(PropertyEmbeddedRefConfiguration).FullName);
            ignoreClassNames.Add(typeof(PropertyRefConfiguration).FullName);
            ignoreClassNames.Add(typeof(PropertyValueConfiguration).FullName);

            ignoreClassNames.AddAll(AbstractBeanConfiguration.ignoreClassNames);
        }

        public static String GetCurrentStackTraceCompact()
        {
            return GetCurrentStackTraceCompactIntern(null);
        }

        public static String GetCurrentStackTraceCompact(ISet<String> ignoreClassNames, IProperties props)
        {
            if (props == null || !Boolean.Parse(props.GetString(IocConfigurationConstants.TrackDeclarationTrace, "false")))
            {
                return null;
            }

            return GetCurrentStackTraceCompactIntern(ignoreClassNames);
        }

        protected static String GetCurrentStackTraceCompactIntern(ISet<String> ignoreClassNames)
        {
            try
            {
                throw new TempStacktraceException();
            }
            catch (TempStacktraceException e)
            {
                return e.StackTrace;
            }
            //StackTraceElement[] stes = Thread.currentThread().getStackTrace();
            //int start = 0, end = stes.length;
            //if (ignoreClassNames != null && ignoreClassNames.size() > 0)
            //{
            //    for (int a = 0, size = stes.length; a < size; a++)
            //    {
            //        StackTraceElement ste = stes[a];
            //        if (!ignoreClassNames.contains(ste.getClassName()))
            //        {
            //            start = a;
            //            break;
            //        }
            //    }
            //}
            //StringBuilder sb = new StringBuilder();
            //for (int a = start, size = stes.length; a < size; a++)
            //{
            //    StackTraceElement ste = stes[a];
            //    if (ste.getClassName().startsWith("org.eclipse.jdt"))
            //    {
            //        end = a;
            //        break;
            //    }
            //    if (a != start)
            //    {
            //        sb.append('\n');
            //    }
            //    sb.append(ste.getClassName()).append('.').append(ste.getMethodName());
            //    if (ste.isNativeMethod())
            //    {
            //        sb.append("(Native Method)");
            //    }
            //    else if (ste.getFileName() != null)
            //    {
            //        sb.append('(').append(ste.getFileName());
            //        if (ste.getLineNumber() >= 0)
            //        {
            //            sb.append(':').append(ste.getLineNumber()).append(')');
            //        }
            //        else
            //        {
            //            sb.append(')');
            //        }
            //    }
            //    else
            //    {
            //        sb.append("(Unknown Source)");
            //    }
            //    sb.append(ste);
            //}
            //return Arrays.copyOfRange(stes, start, end);
        }

        protected String declarationStackTrace;

        protected IBeanConfiguration beanConfiguration;

        public AbstractPropertyConfiguration(IBeanConfiguration beanConfiguration, IProperties props)
        {
            this.beanConfiguration = beanConfiguration;
            ParamChecker.AssertParamNotNull(beanConfiguration, "beanConfiguration");
            declarationStackTrace = GetCurrentStackTraceCompact(ignoreClassNames, props);
        }

        public abstract bool IsOptional();

        public abstract String GetBeanName();

        public abstract String GetPropertyName();

        public abstract Object GetValue();

        public String GetDeclarationStackTrace()
        {
            return declarationStackTrace;
        }

        public IBeanConfiguration BeanConfiguration
        {
            get
            {
                return beanConfiguration;
            }
        }
    }
}