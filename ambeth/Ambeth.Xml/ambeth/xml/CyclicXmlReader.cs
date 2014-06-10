using System;
using System.IO;
using System.Xml;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml.Pending;
using De.Osthus.Ambeth.Xml.PostProcess;
using System.Text;

namespace De.Osthus.Ambeth.Xml
{
    public class CyclicXmlReader : IInitializingBean, ICyclicXmlReader
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public virtual ICommandBuilder CommandBuilder { protected get; set; }

        public virtual IConversionHelper ConversionHelper { protected get; set; }

        public virtual IObjectFutureHandlerRegistry ObjectFutureHandlerRegistry { protected get; set; }

        public virtual ITypeInfoProvider TypeInfoProvider { protected get; set; }

        public virtual CyclicXmlController XmlController { protected get; set; }

        public virtual ICyclicXmlDictionary XmlDictionary { protected get; set; }

        public virtual IXmlPostProcessorRegistry XmlPostProcessorRegistry { protected get; set; }

        public virtual IXmlTypeRegistry XmlTypeRegistry { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(CommandBuilder, "CommandBuilder");
            ParamChecker.AssertNotNull(ConversionHelper, "ConversionHelper");
            ParamChecker.AssertNotNull(ObjectFutureHandlerRegistry, "ObjectFutureHandlerRegistry");
            ParamChecker.AssertNotNull(TypeInfoProvider, "TypeInfoProvider");
            ParamChecker.AssertNotNull(XmlController, "XmlController");
            ParamChecker.AssertNotNull(XmlDictionary, "XmlDictionary");
            ParamChecker.AssertNotNull(XmlPostProcessorRegistry, "XmlPostProcessorRegistry");
            ParamChecker.AssertNotNull(XmlTypeRegistry, "XmlTypeRegistry");
        }

        public virtual Object Read(String cyclicXmlContent)
        {
            TextReader reader = new StringReader(cyclicXmlContent);
            return ReadFromReader(reader);
        }

        public virtual Object ReadFromStream(Stream inputStream)
        {
            return ReadFromStream(inputStream, Encoding.UTF8);
        }

        public virtual Object ReadFromStream(Stream inputStream, Encoding encoding)
        {
            //Stream memoryStream = new MemoryStream();
            //int b;
            //while ((b = inputStream.ReadByte()) != -1)
            //{
            //    memoryStream.WriteByte((byte)b);
            //}
            //memoryStream.Position = 0;
            //String xml = new StreamReader(memoryStream).ReadToEnd();
            String xml = "n/a";
            //memoryStream.Position = 0;
            XmlReaderSettings settings = new XmlReaderSettings();
            settings.IgnoreWhitespace = true;
            settings.DtdProcessing = DtdProcessing.Prohibit;
            //settings.ValidationType = ValidationType.None;
            settings.XmlResolver = null;

            XmlReader reader = XmlReader.Create(new StreamReader(inputStream, encoding), settings);

            try
            {
                DefaultXmlReader pullParserReader = new DefaultXmlReader(reader, XmlController, ObjectFutureHandlerRegistry);
                if (!XmlDictionary.RootElement.Equals(pullParserReader.GetElementName()))
                {
                    pullParserReader.NextTag();
                }
                if (!XmlDictionary.RootElement.Equals(pullParserReader.GetElementName()))
                {
                    throw new Exception("Invalid root element: " + pullParserReader);
                }
                pullParserReader.NextTag();
                Object obj = pullParserReader.ReadObject();
                obj = PostProcess(obj, pullParserReader);

                return obj;
            }
            catch (XmlTypeNotFoundException)
            {
                throw;
            }
            catch (Exception e)
            {
                throw new Exception("Error while parsing xml: " + xml, e);
            }
        }

        public virtual Object ReadFromReader(TextReader textReader)
        {
            XmlReaderSettings settings = new XmlReaderSettings();
            settings.IgnoreWhitespace = true;
            settings.DtdProcessing = DtdProcessing.Prohibit;
            //settings.ValidationType = ValidationType.None;
            settings.XmlResolver = null;

            XmlReader reader = XmlReader.Create(textReader, settings);

            try
            {
                DefaultXmlReader pullParserReader = new DefaultXmlReader(reader, XmlController, ObjectFutureHandlerRegistry);
                reader.Read();

                if (!XmlDictionary.RootElement.Equals(pullParserReader.GetElementName()))
                {
                    throw new Exception("Invalid root element: " + pullParserReader);
                }
                pullParserReader.NextTag();
                Object obj = pullParserReader.ReadObject();
                obj = PostProcess(obj, pullParserReader);

                return obj;
            }
            catch (XmlTypeNotFoundException)
            {
                throw;
            }
            catch (Exception e)
            {
                //if (reader != null)
                //{
                //    throw new Exception(
                //            "Error while parsing cyclic xml content (line " + reader.LgetLineNumber() + ", column " + reader.getColumnNumber(), e);
                //}
                Log.Error(e);
                throw;
            }
        }

        protected Object PostProcess(Object obj, IPostProcessReader pullParserReader)
        {
            HandlePostProcessingTag(pullParserReader);

            if (obj is IObjectFuture)
            {
                IObjectFuture objectFuture = (IObjectFuture)obj;
                ICommandTypeRegistry commandTypeRegistry = pullParserReader.CommandTypeRegistry;
                IObjectCommand objectCommand = CommandBuilder.Build(commandTypeRegistry, objectFuture, null);
                pullParserReader.AddObjectCommand(objectCommand);
                pullParserReader.ExecuteObjectCommands();
                obj = objectFuture.Value;
            }
            else
            {
                pullParserReader.ExecuteObjectCommands();
            }

            return obj;
        }

        protected void HandlePostProcessingTag(IPostProcessReader pullParserReader)
        {
            if (!pullParserReader.IsStartTag())
            {
                return;
            }

            String elementName = pullParserReader.GetElementName();
            if (!XmlDictionary.PostProcessElement.Equals(elementName))
            {
                throw new Exception("Only <" + XmlDictionary.PostProcessElement + "> allowed as second child of root. <" + elementName + "> found.");
            }

            pullParserReader.NextTag();

            IXmlPostProcessorRegistry xmlPostProcessorRegistry = XmlPostProcessorRegistry;
            while (pullParserReader.IsStartTag())
            {
                elementName = pullParserReader.GetElementName();
                IXmlPostProcessor xmlPostProcessor = xmlPostProcessorRegistry.GetXmlPostProcessor(elementName);
                if (xmlPostProcessor == null)
                {
                    throw new Exception("Post processing tag <" + elementName + "> not supported.");
                }

                xmlPostProcessor.ProcessRead(pullParserReader);

                pullParserReader.MoveOverElementEnd();
            }
        }
    }
}