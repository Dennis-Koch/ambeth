using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml.Namehandler;

namespace De.Osthus.Ambeth.Xml.Typehandler
{
    public abstract class AbstractHandler : IInitializingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IConversionHelper ConversionHelper { protected get; set; }

        [Autowired]
        public ICyclicXmlDictionary XmlDictionary { protected get; set; }

        [Autowired]
        public ClassNameHandler ClassElementHandler { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            // intended blank
        }
    }
}