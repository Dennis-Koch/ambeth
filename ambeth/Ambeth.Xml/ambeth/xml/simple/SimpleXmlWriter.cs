using De.Osthus.Ambeth.Appendable;
using De.Osthus.Ambeth.Ioc.Annotation;
using System;
using System.IO;
using System.Text;

namespace De.Osthus.Ambeth.Xml.Simple
{
    public class SimpleXmlWriter : ICyclicXmlWriter
    {
        [Autowired]
        public ICyclicXmlController XmlController { protected get; set; }

        public String Write(Object obj)
        {
            StringBuilder sb = new StringBuilder();
            DefaultXmlWriter writer = new DefaultXmlWriter(new AppendableStringBuilder(sb), XmlController);

            WritePrefix(writer);
            writer.WriteObject(obj);
            PostProcess(writer);
            WritePostfix(writer);
            return sb.ToString();
        }

        public void WriteToStream(Stream os, Object obj)
        {
            DefaultXmlWriter writer = new DefaultXmlWriter(new AppendableStreamEncoder(os, Encoding.UTF8), XmlController);

            WritePrefix(writer);
            writer.WriteObject(obj);
            PostProcess(writer);
            WritePostfix(writer);
            os.Flush();
        }

        protected virtual void WritePrefix(IWriter writer)
        {
            // Intended blank
        }

        protected virtual void WritePostfix(IWriter writer)
        {
            // Intended blank
        }

        protected virtual void PostProcess(DefaultXmlWriter writer)
        {
            // Intended blank
        }
    }
}