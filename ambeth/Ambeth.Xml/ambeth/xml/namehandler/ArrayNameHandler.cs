using System;
using System.Text.RegularExpressions;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Util.Converter;
using De.Osthus.Ambeth.Xml.Pending;
using De.Osthus.Ambeth.Xml.Typehandler;

namespace De.Osthus.Ambeth.Xml.Namehandler
{
    public class ArrayNameHandler : AbstractHandler, INameBasedHandler, IInitializingBean
    {
        [LogInstance]
        public new ILogger Log { private get; set; }

        public virtual ICommandBuilder CommandBuilder { protected get; set; }

        public const String primitiveValueSeparator = ";";

        protected readonly Regex splitPattern = new Regex(primitiveValueSeparator);

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();

            ParamChecker.AssertNotNull(CommandBuilder, "CommandBuilder");
        }

        public virtual bool WritesCustom(Object obj, Type type, IWriter writer)
        {
            if (!type.IsArray)
            {
                return false;
            }
            Array array = (Array)obj;
            String arrayElement = XmlDictionary.ArrayElement;
            writer.WriteStartElement(arrayElement);
            int id = writer.AcquireIdForObject(obj);
            writer.WriteAttribute(XmlDictionary.IdAttribute, id.ToString());
            int length = array.Length;
            writer.WriteAttribute(XmlDictionary.SizeAttribute, length.ToString());
            Type componentType = type.GetElementType();
            ClassElementHandler.WriteAsAttribute(componentType, writer);

            if (length == 0)
            {
                writer.WriteEndElement();
            }
            else
            {
                writer.WriteStartElementEnd();
                if (componentType.IsPrimitive)
                {
                    writer.Write("<values v=\"");
                    if (typeof(char).Equals(componentType) || typeof(byte).Equals(componentType) || typeof(sbyte).Equals(componentType)
                         || typeof(bool).Equals(componentType))
                    {
                        String value = ConversionHelper.ConvertValueToType<String>(array, EncodingInformation.SOURCE_PLAIN | EncodingInformation.TARGET_BASE64);
                        writer.Write(value);
                    }
                    else
                    {
                        for (int a = 0; a < length; a++)
                        {
                            Object item = array.GetValue(a);
                            if (a > 0)
                            {
                                writer.Write(primitiveValueSeparator);
                            }
                            String value = ConversionHelper.ConvertValueToType<String>(item);
                            writer.WriteEscapedXml(value);
                        }
                    }
                    writer.Write("\"/>");
                }
                else
                {
                    for (int a = 0; a < length; a++)
                    {
                        Object item = array.GetValue(a);
                        writer.WriteObject(item);
                    }
                }
                writer.WriteCloseElement(arrayElement);
            }
            return true;
        }

        public virtual Object ReadObject(Type returnType, String elementName, int id, IReader reader)
        {
            if (!XmlDictionary.ArrayElement.Equals(elementName))
            {
                throw new Exception("Element '" + elementName + "' not supported");
            }
            int length = Int32.Parse(reader.GetAttributeValue(XmlDictionary.SizeAttribute));
            Type componentType = ClassElementHandler.ReadFromAttribute(reader);

            Array targetArray;
            if (!reader.IsEmptyElement())
            {
                reader.NextTag();
            }
            if ("values".Equals(reader.GetElementName()))
            {
                String listOfValuesString = reader.GetAttributeValue("v");

                if (typeof(char).Equals(componentType) || typeof(byte).Equals(componentType) || typeof(sbyte).Equals(componentType)
                    || typeof(bool).Equals(componentType))
                {
                    targetArray = (Array)ConversionHelper.ConvertValueToType(componentType.MakeArrayType(), listOfValuesString, EncodingInformation.SOURCE_BASE64 | EncodingInformation.TARGET_PLAIN);
                    reader.PutObjectWithId(targetArray, id);
                }
                else
                {
                    targetArray = Array.CreateInstance(componentType, length);
                    reader.PutObjectWithId(targetArray, id);
                    String[] items = splitPattern.Split(listOfValuesString);
                    for (int a = 0, size = items.Length; a < size; a++)
                    {
                        String item = items[a];
                        if (item == null || item.Length == 0)
                        {
                            continue;
                        }
                        Object convertedValue = ConversionHelper.ConvertValueToType(componentType, items[a]);
                        targetArray.SetValue(convertedValue, a);
                    }
                }
                reader.MoveOverElementEnd();
            }
            else
            {
                if (returnType.IsGenericType)
                {
                    componentType = returnType.GetGenericArguments()[0];
                }
                targetArray = Array.CreateInstance(componentType, length);
                reader.PutObjectWithId(targetArray, id);
                ICommandBuilder commandBuilder = CommandBuilder;
                ICommandTypeRegistry commandTypeRegistry = reader.CommandTypeRegistry;
                for (int index = 0; index < length; index++)
                {
                    Object item = reader.ReadObject(componentType);
                    if (item is IObjectFuture)
                    {
                        IObjectFuture objectFuture = (IObjectFuture)item;
                        IObjectCommand command = commandBuilder.Build(commandTypeRegistry, objectFuture, targetArray, index);
                        reader.AddObjectCommand(command);
                    }
                    else
                    {
                        targetArray.SetValue(item, index);
                    }
                }
            }
            return targetArray;
        }
    }
}