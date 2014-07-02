using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml.Pending;
using De.Osthus.Ambeth.Xml.PostProcess;
using System;
using System.IO;
using System.Text;
using System.Xml;

namespace De.Osthus.Ambeth.Xml.Simple
{
    public class SimpleXmlReader : ICyclicXmlReader
    {
        [LogInstance]
        public ILogger log { private get; set; }

        [Autowired]
        public ICommandBuilder CommandBuilder { protected get; set; }

        [Autowired]
        public IConversionHelper ConversionHelper { protected get; set; }

        [Autowired]
        public IObjectFutureHandlerRegistry ObjectFutureHandlerRegistry { protected get; set; }

        [Autowired]
        public ITypeInfoProvider TypeInfoProvider { protected get; set; }

        [Autowired]
        public ICyclicXmlController XmlController { protected get; set; }

        public Object Read(String cyclicXmlContent)
        {
            TextReader reader = new StringReader(cyclicXmlContent);
            return ReadFromReader(reader);
        }

        public Object ReadFromStream(Stream se)
        {
            return ReadFromStream(se, Encoding.UTF8);
        }

        public Object ReadFromStream(Stream se, Encoding encoding)
        {
            if (se.CanSeek)
            {
                return ReadFromStreamLogAfterException(se, encoding);
            }
            return ReadFromStreamLogBeforeException(se, encoding);
        }

        protected Object ReadFromStreamLogAfterException(Stream se, Encoding encoding)
        {
            long position = se.Position;
            StreamReader streamReader = null;
            try
            {
                streamReader = new StreamReader(se, encoding);
                return ReadFromReader(streamReader);
            }
            catch (XmlTypeNotFoundException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                long newPosition = se.Position;
                se.Seek(position, SeekOrigin.Begin);
                byte[] buffer = new byte[newPosition - position];
                se.Read(buffer, 0, buffer.Length);
                String xmlContent = new String(encoding.GetChars(buffer));
                throw RuntimeExceptionUtil.Mask(e, xmlContent);
            }
        }

        protected Object ReadFromStreamLogBeforeException(Stream se, Encoding encoding)
        {
            StringBuilder sb = new StringBuilder();
            try
            {
                StreamReader streamReader = new ExtendedStreamReader(se, sb); ;
                return ReadFromReader(streamReader);
            }
            catch (XmlTypeNotFoundException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw RuntimeExceptionUtil.Mask(e, sb.ToString());
            }
        }

        public Object ReadFromReader(TextReader reader)
        {
            XmlReaderSettings settings = new XmlReaderSettings();
            settings.IgnoreWhitespace = true;
            settings.DtdProcessing = DtdProcessing.Prohibit;
            //settings.ValidationType = ValidationType.None;
            settings.XmlResolver = null;

            XmlReader xmlReader = XmlReader.Create(reader, settings);
            try
            {
                DefaultXmlReader pullParserReader = new DefaultXmlReader(xmlReader, XmlController, ObjectFutureHandlerRegistry);
                pullParserReader.NextTag();
                ReadPrefix(pullParserReader);
                Object obj = pullParserReader.ReadObject();
                obj = PostProcess(obj, pullParserReader);
                ReadPostfix(pullParserReader);
                return obj;
            }
            catch (XmlTypeNotFoundException e)
            {
                throw e;
            }
            catch (Exception)
            {
                //if (xmlReader != null)
                //{
                //    throw RuntimeExceptionUtil.Mask(e,
                //            "Error while parsing cyclic xml content (line " + xmlReader..get.getLineNumber() + ", column " + xmlReader.getColumnNumber() + ")");
                //}
                throw;
            }
        }

        protected virtual void ReadPrefix(IReader reader)
        {
            // Intended blank
        }

        protected virtual void ReadPostfix(IReader reader)
        {
            //
        }

        protected virtual Object PostProcess(Object obj, IPostProcessReader postProcessReader)
        {
            if (obj is IObjectFuture)
            {
                IObjectFuture objectFuture = (IObjectFuture)obj;
                ICommandTypeRegistry commandTypeRegistry = postProcessReader.CommandTypeRegistry;
                IObjectCommand objectCommand = CommandBuilder.Build(commandTypeRegistry, objectFuture, null);
                postProcessReader.AddObjectCommand(objectCommand);
                postProcessReader.ExecuteObjectCommands();
                obj = objectFuture.Value;
            }
            else
            {
                postProcessReader.ExecuteObjectCommands();
            }
            return obj;
        }
    }
}