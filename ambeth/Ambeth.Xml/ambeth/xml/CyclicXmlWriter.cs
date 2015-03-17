using De.Osthus.Ambeth.Appendable;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml.PostProcess;
using System;
using System.IO;
using System.Text;

namespace De.Osthus.Ambeth.Xml
{
    public class CyclicXmlWriter : IInitializingBean, ICyclicXmlWriter
    {
        public virtual ICyclicXmlDictionary XmlDictionary { protected get; set; }

        public virtual CyclicXmlController XmlController { protected get; set; }

        public virtual IXmlPostProcessorRegistry XmlPostProcessorRegistry { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(XmlController, "XmlController");
            ParamChecker.AssertNotNull(XmlDictionary, "XmlDictionary");
            ParamChecker.AssertNotNull(XmlPostProcessorRegistry, "XmlPostProcessorRegistry");
        }

        public virtual String Write(Object obj)
        {
            StringBuilder sb = new StringBuilder();
            String rootElement = XmlDictionary.RootElement;

            DefaultXmlWriter writer = new DefaultXmlWriter(new AppendableStringBuilder(sb), XmlController);

            writer.WriteOpenElement(rootElement);
            writer.WriteObject(obj);
            PostProcess(writer);
            writer.WriteCloseElement(rootElement);
            return sb.ToString();
        }

        public virtual void WriteToStream(Stream outputStream, Object obj)
        {
            StreamWriter outputStreamWriter = null;
            try
            {
                outputStreamWriter = new StreamWriter(outputStream, Encoding.UTF8);
                String rootElement = XmlDictionary.RootElement;

                DefaultXmlWriter writer = new DefaultXmlWriter(new StreamWriterAppendable(outputStreamWriter), XmlController);

                writer.WriteOpenElement(rootElement);
                writer.WriteObject(obj);
                PostProcess(writer);
                writer.WriteCloseElement(rootElement);
            }
            finally
            {
                if (outputStreamWriter != null)
                {
                    outputStreamWriter.Flush();
                    outputStreamWriter.Close();
                    outputStreamWriter = null;
                }
            }
        }

        protected void PostProcess(IPostProcessWriter writer)
        {
            ILinkedMap<String, IXmlPostProcessor> xmlPostProcessors = XmlPostProcessorRegistry.GetXmlPostProcessors();
            ILinkedMap<String, Object> ppResults = new LinkedHashMap<String, Object>((int)(xmlPostProcessors.Count / 0.75 + 1));
            foreach (Entry<String, IXmlPostProcessor> entry in xmlPostProcessors)
            {
                String tagName = entry.Key;
                IXmlPostProcessor xmlPostProcessor = entry.Value;
                Object result = xmlPostProcessor.ProcessWrite(writer);
                if (result != null)
                {
                    ppResults.Put(tagName, result);
                }
            }

            if (ppResults.Count == 0)
            {
                return;
            }

            String postProcessElement = XmlDictionary.PostProcessElement;
            writer.WriteStartElement(postProcessElement);
            foreach (Entry<String, Object> entry in ppResults)
            {
                String tagName = entry.Key;
                Object result = entry.Value;

                writer.WriteOpenElement(tagName);
                writer.WriteObject(result);
                writer.WriteCloseElement(tagName);
            }
            writer.WriteCloseElement(postProcessElement);
        }
    }
}