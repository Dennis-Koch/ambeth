using System;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;

namespace De.Osthus.Ambeth.Xml
{
    public class CyclicXmlDictionary : ICyclicXmlDictionary, IInitializingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public virtual void AfterPropertiesSet()
        {
        }

        public String ArrayElement
        {
            get
            {
                return "a";
            }
        }


        public String BlobElement
        {
            get
            {
                return "b";
            }
        }


        public String ClassElement
        {
            get
            {
                return "c";
            }
        }

        public String EnumElement
        {
            get
            {
                return "e";
            }
        }

        public String ExceptionElement
        {
            get
            {
                return "ex";
            }
        }

        public String EntityRefElement
        {
            get
            {
                return "or";
            }
        }


        public String ListElement
        {
            get
            {
                return "l";
            }
        }


        public String NullElement
        {
            get
            {
                return "n";
            }
        }


        public String NumberElement
        {
            get
            {
                return "nu";
            }
        }


        public String ObjectElement
        {
            get
            {
                return "o";
            }
        }


        public String OriWrapperElement
        {
            get
            {
                return "ow";
            }
        }


        public String PostProcessElement
        {
            get
            {
                return "pp";
            }
        }


        public String PrimitiveElement
        {
            get
            {
                return "p";
            }
        }


        public String RefElement
        {
            get
            {
                return "r";
            }
        }


        public String RootElement
        {
            get
            {
                return "root";
            }
        }


        public String SetElement
        {
            get
            {
                return "set";
            }
        }

        
        public String SimpleObjectElement
        {
            get
            {
                return "v";
            }
        }


        public String StringElement
        {
            get
            {
                return "s";
            }
        }


        public String TypeElement
        {
            get
            {
                return "type";
            }
        }


        public String ArrayDimensionAttribute
        {
            get
            {
                return "dim";
            }
        }


        public String ClassNameAttribute
        {
            get
            {
                return "n";
            }
        }


        public String ClassNamespaceAttribute
        {
            get
            {
                return "ns";
            }
        }

        public String ClassMemberAttribute
        {
            get
            {
                return "m";
            }
        }

        public String EntityRefKeyAttribute
        {
            get
            {
                return "key";
            }
        }

        public String EntityRefKeyIndexAttribute
        {
            get
            {
                return "index";
            }
        }

        public String EntityRefVersionAttribute
        {
            get
            {
                return "version";
            }
        }

        public String ClassIdAttribute
        {
            get
            {
                return "ti";
            }
        }


        public String IdAttribute
        {
            get
            {
                return "i";
            }
        }


        public String SizeAttribute
        {
            get
            {
                return "s";
            }
        }


        public String ValueAttribute
        {
            get
            {
                return "v";
            }
        }
    }
}