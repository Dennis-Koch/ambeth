using System;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode
{
    public interface IBytecodeClassLoader : IBytecodePrinter
    {
	    byte[] ReadTypeAsBinary(Type type);

	    String GetBytecodeTypeName(Type type);

        Type BuildTypeFromScratch(String newTypeName, StringBuilder writer, IBuildVisitorDelegate buildVisitorDelegate);

        Type BuildTypeFromParent(String newTypeName, Type sourceContent, StringBuilder writer, IBuildVisitorDelegate buildVisitorDelegate);

	    Type LoadClass(String typeName, byte[] content);

        void Save();

	    void Verify(byte[] content);
    }
}