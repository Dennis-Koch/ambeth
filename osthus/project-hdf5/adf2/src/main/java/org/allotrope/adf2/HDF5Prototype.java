package org.allotrope.adf2;

import static ncsa.hdf.hdf5lib.H5.H5Aclose;
import static ncsa.hdf.hdf5lib.H5.H5Acreate;
import static ncsa.hdf.hdf5lib.H5.H5Awrite;
import static ncsa.hdf.hdf5lib.H5.H5Dclose;
import static ncsa.hdf.hdf5lib.H5.H5Dcreate;
import static ncsa.hdf.hdf5lib.H5.H5Dwrite;
import static ncsa.hdf.hdf5lib.H5.H5Fclose;
import static ncsa.hdf.hdf5lib.H5.H5Fcreate;
import static ncsa.hdf.hdf5lib.H5.H5Gclose;
import static ncsa.hdf.hdf5lib.H5.H5Gcreate;
import static ncsa.hdf.hdf5lib.H5.H5Sclose;
import static ncsa.hdf.hdf5lib.H5.H5Screate;
import static ncsa.hdf.hdf5lib.H5.H5Screate_simple;
import static ncsa.hdf.hdf5lib.H5.H5Tclose;
import static ncsa.hdf.hdf5lib.H5.H5Tcopy;
import static ncsa.hdf.hdf5lib.H5.H5Tset_cset;
import static ncsa.hdf.hdf5lib.H5.H5Tset_size;
import static ncsa.hdf.hdf5lib.H5.H5Tvlen_create;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5F_ACC_TRUNC;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5S_ALL;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5S_SCALAR;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_CSET_UTF8;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_C_S1;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_NATIVE_CHAR;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5T_NATIVE_DOUBLE;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class HDF5Prototype
{
	public static void main(String args[])
	{
		Model m = ModelFactory.createDefaultModel();
		// read into the model.
		// m.read("test.ttl");
		m.read("test-ore.ttl"); // ore description
		m.read("test-void.ttl"); // void ontology description
		m.write(System.out);
		Pointer[][] rdf = makeTriples(m);
		int N = 5;
		int NX = 200;
		int NY = 2;
		byte[] dataBytes = new byte[N * NX * NY * 8];
		DoubleBuffer dataMS = ByteBuffer.wrap(dataBytes).order(ByteOrder.nativeOrder()).asDoubleBuffer();

		for (int k = 0; k < N; ++k)
		{
			for (int i = 0; i < NX; ++i)
			{
				dataMS.put(i);
				dataMS.put(Math.sin(k * i * Math.PI / NX * 2));
			}
		}
		try
		{
			int file = H5Fcreate("test.hdf5", H5F_ACC_TRUNC, H5P_DEFAULT, H5P_DEFAULT);
			createUTF8File(file, "bagit.txt", "BagIt-Version: 0.97\nTag-File-Character-Encoding: UTF-8");
			// int manifestId = createVarCharTable(file, "manifest", new Pointer[][] { { utf8("md5"), utf8("data/mass-spectra") },
			// { utf8("md5"), utf8("meta-data/triples") }, }, 2);
			// // createAttribute(manifestId, "Algorithm", "MD5");
			// int piId = createVarCharTable(file, "pid-mapping", new Pointer[][] {
			// { utf8("http://purl.allotrope.org/member/ACME/test/data/studies/study1/t2013/ms1"), utf8("info:BagIt/data/mass spectra[1]") },
			// { utf8("http://purl.allotrope.org/member/ACME/test/data/studies/study1/t2013/ms2"), utf8("info:BagIt/data/mass spectra[2]") }, }, 2);
			// int prefixId = createVarCharTable(file, "namespace-prefixes", new Pointer[][] { { utf8("ore"), utf8("http://www.openarchives.org/ore/terms/") },
			// { utf8("dc"), utf8("http://purl.org/dc/elements/1.1/") }, { utf8("dcterms"), utf8("http://purl.org/dc/terms/") },
			// { utf8("foaf"), utf8("http://xmlns.com/foaf/0.1/") }, { utf8("local"), utf8("info:BagIt/data/") },
			// { utf8("public"), utf8("http://purl.allotrope.org/member/ACME/test/data/") }, }, 2);
			int data = H5Gcreate(file, "data", H5P_DEFAULT, H5P_DEFAULT, H5P_DEFAULT);
			int msspace = H5Screate_simple(3, new long[] { N, NX, NY }, null);
			int msds = H5Dcreate(data, "mass spectra", H5T_NATIVE_DOUBLE, msspace, H5P_DEFAULT, H5P_DEFAULT, H5P_DEFAULT);
			H5Dwrite(msds, H5T_NATIVE_DOUBLE, H5S_ALL, H5S_ALL, H5P_DEFAULT, dataBytes);
			H5Dclose(msds);
			H5Sclose(msspace);
			int metadata = H5Gcreate(file, "meta-data", H5P_DEFAULT, H5P_DEFAULT, H5P_DEFAULT);
			createVarCharTable(metadata, "triples", rdf, 3);
			H5Gclose(metadata);
			H5Gclose(data);
			H5Fclose(file);
			allocatedStrings.clear();// free memory

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
		}
	}

	public static class Hvl_t extends Structure
	{
		public long len;
		public Pointer p;

		@Override
		protected List<String> getFieldOrder()
		{
			return Arrays.asList(new String[] { "len", "p" });
		}

		public Hvl_t()
		{
			len = 0L;
			p = Pointer.NULL;
		}

		public Hvl_t(Pointer p)
		{
			super(p);
		}
	};

	public static class UTF8String extends Memory
	{
		public static Charset UTF8 = Charset.forName("utf-8");

		public UTF8String(String s)
		{
			super(toUTF8Bytes(s).length + 1);
			this.setString(0, s, "utf-8");
		}

		public static byte[] toUTF8Bytes(String s)
		{
			return s.getBytes(UTF8);
		}

		public int length()
		{
			int l = 0;
			while (getByte(l) != (byte) 0)
			{
				l++;
			}
			return l;
		}

		@Override
		public String toString()
		{
			byte[] mem = getByteArray(0, length());
			return new String(mem, UTF8);
		}
	}

	public static void createAttribute(int locId, String name, String value)
	{
		try
		{
			int ds = H5Screate(H5S_SCALAR);
			int dt = H5Tcopy(H5T_C_S1);
			UTF8String utf8 = new UTF8String(value);
			H5Tset_size(dt, utf8.length() + 1);
			H5Tset_cset(dt, H5T_CSET_UTF8);

			int at = H5Acreate(locId, name, dt, ds, H5P_DEFAULT, H5P_DEFAULT);
			byte[] bytes = utf8.getByteArray(0, utf8.length() + 1);
			H5Awrite(at, dt, bytes);
			H5Aclose(at);
			H5Tclose(dt);
			H5Sclose(ds);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
		}
	}

	public static void createUTF8File(int locId, String name, String value)
	{
		try
		{
			int ds = H5Screate(H5S_SCALAR);
			int dt = H5Tcopy(H5T_C_S1);
			UTF8String utf8 = new UTF8String(value);
			H5Tset_size(dt, utf8.length() + 1);
			H5Tset_cset(dt, H5T_CSET_UTF8);
			byte[] bytes = utf8.getByteArray(0, utf8.length() + 1);
			int dset = H5Dcreate(locId, name, dt, ds, H5P_DEFAULT, H5P_DEFAULT, H5P_DEFAULT);
			H5Dwrite(dset, dt, ds, ds, H5S_ALL, bytes);
			H5Dclose(dset);
			H5Tclose(dt);
			H5Sclose(ds);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
		}
	}

	public static int createVarCharTable(int locId, String name, Pointer[][] stringTable, int nColumns)
	{
		try
		{
			Hvl_t hvl = new Hvl_t();
			int hvl_size = hvl.size();
			Memory hvlData = new Memory(hvl_size * stringTable.length * nColumns); // malloc()
			int offset = 0;
			for (int i = 0; i < stringTable.length; ++i)
			{
				for (int j = 0; j < nColumns; ++j)
				{
					Pointer p0 = hvlData.share(offset, hvl_size);
					offset += hvl_size;
					Hvl_t hvl_i0 = new Hvl_t(p0);
					int len = 0;
					while (stringTable[i][j].getByte(len) != (byte) 0)
					{
						len++;
					}
					hvl_i0.len = len + 1;
					hvl_i0.p = stringTable[i][j];
					hvl_i0.write(); // commit to native memory
				}
			}
			byte[] tableBytes = hvlData.getByteArray(0, hvl_size * stringTable.length * nColumns);
			int tspace = H5Screate_simple(2, new long[] { stringTable.length, nColumns }, null);
			int strUTF8 = H5Tvlen_create(H5T_NATIVE_CHAR);
			int tds = H5Dcreate(locId, name, strUTF8, tspace, H5P_DEFAULT, H5P_DEFAULT, H5P_DEFAULT);
			H5Dwrite(tds, strUTF8, H5S_ALL, H5S_ALL, H5P_DEFAULT, tableBytes);
			H5Dclose(tds);
			H5Sclose(tspace);
			return tds;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return 0;
		}
		finally
		{
		}
	}

	private static Map<String, UTF8String> allocatedStrings = new HashMap<String, UTF8String>(); // global heap (malloc)
																									// allocated
																									// strings,

	private static Pointer utf8(String s)
	{
		UTF8String utf8 = allocatedStrings.get(s);
		if (utf8 == null)
		{
			utf8 = new UTF8String(s);
			allocatedStrings.put(s, utf8);
		}
		return utf8.share(0); // return reference counted pointer
	}

	public static Pointer[][] makeTriples(Model m)
	{
		StmtIterator it = m.listStatements();
		ArrayList<Pointer[]> triples = new ArrayList<Pointer[]>();
		int anon = 1;
		while (it.hasNext())
		{
			Statement s = it.next();
			Pointer[] spo = new Pointer[3];
			String subject;
			String predicate;
			String object;
			if (s.getSubject().isAnon())
			{
				subject = "_" + anon;
				++anon;
			}
			else
			{
				subject = s.getSubject().getURI();
			}
			predicate = s.getPredicate().getURI();
			if (s.getObject().isLiteral())
			{
				object = s.getObject().asLiteral().getLexicalForm();
			}
			else if (s.getObject().isAnon())
			{
				object = "_" + anon;
				++anon;
			}
			else
			{
				object = s.getObject().asResource().getURI();
			}
			spo[0] = utf8(subject);
			spo[1] = utf8(predicate);
			spo[2] = utf8(object);
			triples.add(spo);
		}
		return triples.toArray(new Pointer[triples.size()][3]);
	}

}
