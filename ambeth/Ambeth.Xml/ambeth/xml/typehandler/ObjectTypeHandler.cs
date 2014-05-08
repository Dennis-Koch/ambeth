using System;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml.Pending;

namespace De.Osthus.Ambeth.Xml.Typehandler
{
    public class ObjectTypeHandler : AbstractHandler, ITypeBasedHandler
    {
        [LogInstance]
        public new ILogger Log { private get; set; }

        public virtual ICommandBuilder CommandBuilder { protected get; set; }

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();

            ParamChecker.AssertNotNull(CommandBuilder, "CommandBuilder");
        }

        public virtual void WriteObject(Object obj, Type type, IWriter writer)
        {
            writer.WriteStartElementEnd();
            ITypeInfoItem[] members = writer.GetMembersOfType(type);

            String valueAttribute = XmlDictionary.ValueAttribute;
            String primitiveElement = XmlDictionary.PrimitiveElement;
            for (int a = 0, size = members.Length; a < size; a++)
            {
                ITypeInfoItem field = members[a];

                Object fieldValue = field.GetValue(obj, false);
                if (field.RealType.IsPrimitive)
                {
                    writer.WriteStartElement(primitiveElement);
                    String convertedValue = ConversionHelper.ConvertValueToType<String>(fieldValue);
                    writer.WriteAttribute(valueAttribute, convertedValue);
                    writer.WriteEndElement();
                    continue;
                }
                writer.WriteObject(fieldValue);
            }
        }

        public virtual Object ReadObject(Type returnType, Type objType, int id, IReader reader)
        {
            Object obj = Activator.CreateInstance(objType);
            if (id > 0)
            {
                reader.PutObjectWithId(obj, id);
            }
            reader.NextTag();
            ITypeInfoItem[] members = reader.GetMembersOfType(objType);

            int index = 0;
            String valueAttribute = XmlDictionary.ValueAttribute;
            String primitiveElement = XmlDictionary.PrimitiveElement;
            ICommandBuilder commandBuilder = CommandBuilder;
            ICommandTypeRegistry commandTypeRegistry = reader.CommandTypeRegistry;
            IConversionHelper conversionHelper = ConversionHelper;
            while (reader.IsStartTag())
            {
                ITypeInfoItem member = members[index++];
                Object memberValue;
                if (primitiveElement.Equals(reader.GetElementName()))
                {
                    String value = reader.GetAttributeValue(valueAttribute);
                    memberValue = conversionHelper.ConvertValueToType(member.RealType, value);
                    reader.MoveOverElementEnd();
                }
                else
                {
                    memberValue = reader.ReadObject(member.RealType);
                }
                if (memberValue is IObjectFuture)
                {
                    IObjectFuture objectFuture = (IObjectFuture)memberValue;
                    IObjectCommand command = commandBuilder.Build(commandTypeRegistry, objectFuture, obj, member);
                    reader.AddObjectCommand(command);
                }
                else
                {
                    member.SetValue(obj, memberValue);
                }
            }
            return obj;
        }
    }
}