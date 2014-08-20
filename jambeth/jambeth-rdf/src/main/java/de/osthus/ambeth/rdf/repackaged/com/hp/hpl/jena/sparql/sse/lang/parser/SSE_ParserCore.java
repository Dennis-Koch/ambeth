/* Generated By:JavaCC: Do not edit this line. SSE_ParserCore.java */
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.sse.lang.parser;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.sparql.sse.lang.ParserSSEBase;

//This is javacc generated code
@SuppressWarnings("all")
public class SSE_ParserCore extends ParserSSEBase implements SSE_ParserCoreConstants
{

	// Now has explicit WS control in the grammar.
	// Policy - eat trailing WS

	// ---- Entry points : check for EOF.
	final public void parse() throws ParseException
	{
		parseStart();
		label_1: while (true)
		{
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
			{
				case WS:
					;
					break;
				default:
					jj_la1[0] = jj_gen;
					break label_1;
			}
			jj_consume_token(WS);
		}
		TermOrList();
		jj_consume_token(0);
		parseFinish();
	}

	final public void term() throws ParseException
	{
		parseStart();
		Term();
		jj_consume_token(0);
		parseFinish();
	}

	// ----
	final public void TermOrList() throws ParseException
	{
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
		{
			case IRIref:
			case PNAME:
			case BLANK_NODE_LABEL:
			case VAR_NAMED:
			case VAR_OTHER:
			case INTEGER:
			case DECIMAL:
			case DOUBLE:
			case STRING_LITERAL1:
			case STRING_LITERAL2:
			case STRING_LITERAL_LONG1:
			case STRING_LITERAL_LONG2:
			case SYMBOL:
				Term();
				label_2: while (true)
				{
					switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
					{
						case WS:
							;
							break;
						default:
							jj_la1[1] = jj_gen;
							break label_2;
					}
					jj_consume_token(WS);
				}
				break;
			case LPAREN:
			case LBRACKET:
				List();
				break;
			default:
				jj_la1[2] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
		}
	}

	final public void List() throws ParseException
	{
		Token t;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
		{
			case LPAREN:
				t = jj_consume_token(LPAREN);
				label_3: while (true)
				{
					switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
					{
						case WS:
							;
							break;
						default:
							jj_la1[3] = jj_gen;
							break label_3;
					}
					jj_consume_token(WS);
				}
				listStart(t.beginLine, t.beginColumn);
				BareList();
				t = jj_consume_token(RPAREN);
				label_4: while (true)
				{
					switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
					{
						case WS:
							;
							break;
						default:
							jj_la1[4] = jj_gen;
							break label_4;
					}
					jj_consume_token(WS);
				}
				listFinish(t.beginLine, t.beginColumn);
				break;
			case LBRACKET:
				t = jj_consume_token(LBRACKET);
				label_5: while (true)
				{
					switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
					{
						case WS:
							;
							break;
						default:
							jj_la1[5] = jj_gen;
							break label_5;
					}
					jj_consume_token(WS);
				}
				listStart(t.beginLine, t.beginColumn);
				BareList();
				t = jj_consume_token(RBRACKET);
				label_6: while (true)
				{
					switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
					{
						case WS:
							;
							break;
						default:
							jj_la1[6] = jj_gen;
							break label_6;
					}
					jj_consume_token(WS);
				}
				listFinish(t.beginLine, t.beginColumn);
				break;
			default:
				jj_la1[7] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
		}
	}

	final public void BareList() throws ParseException
	{
		label_7: while (true)
		{
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
			{
				case IRIref:
				case PNAME:
				case BLANK_NODE_LABEL:
				case VAR_NAMED:
				case VAR_OTHER:
				case INTEGER:
				case DECIMAL:
				case DOUBLE:
				case STRING_LITERAL1:
				case STRING_LITERAL2:
				case STRING_LITERAL_LONG1:
				case STRING_LITERAL_LONG2:
				case LPAREN:
				case LBRACKET:
				case SYMBOL:
					;
					break;
				default:
					jj_la1[8] = jj_gen;
					break label_7;
			}
			TermOrList();
		}
	}

	final public void Term() throws ParseException
	{
		Token t;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
		{
			case SYMBOL:
				Symbol();
				break;
			case IRIref:
				IRIref();
				break;
			case PNAME:
				PrefixedName();
				break;
			case VAR_NAMED:
			case VAR_OTHER:
				Var();
				break;
			case INTEGER:
			case DECIMAL:
			case DOUBLE:
			case STRING_LITERAL1:
			case STRING_LITERAL2:
			case STRING_LITERAL_LONG1:
			case STRING_LITERAL_LONG2:
				Literal();
				break;
			case BLANK_NODE_LABEL:
				BlankNode();
				break;
			default:
				jj_la1[9] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
		}
	}

	final public void Symbol() throws ParseException
	{
		Token t;
		t = jj_consume_token(SYMBOL);
		emitSymbol(t.beginLine, t.beginColumn, t.image);
	}

	final public void IRIref() throws ParseException
	{
		Token t;
		String s;
		t = jj_consume_token(IRIref);
		s = t.image;
		s = stripQuotes(s);
		s = unescapeStr(s, t.beginLine, t.beginColumn);
		emitIRI(t.beginLine, t.beginColumn, s);
	}

	final public void PrefixedName() throws ParseException
	{
		Token t;
		t = jj_consume_token(PNAME);
		emitPName(t.beginLine, t.beginColumn, t.image);
	}

	final public void Var() throws ParseException
	{
		Token t;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
		{
			case VAR_NAMED:
				t = jj_consume_token(VAR_NAMED);
				break;
			case VAR_OTHER:
				t = jj_consume_token(VAR_OTHER);
				break;
			default:
				jj_la1[10] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
		}
		emitVar(t.beginLine, t.beginColumn, stripChars(t.image, 1));
	}

	final public void Literal() throws ParseException
	{
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
		{
			case STRING_LITERAL1:
			case STRING_LITERAL2:
			case STRING_LITERAL_LONG1:
			case STRING_LITERAL_LONG2:
				RDFLiteral();
				break;
			case INTEGER:
			case DECIMAL:
			case DOUBLE:
				NumericLiteral();
				break;
			default:
				jj_la1[11] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
		}
	}

	final public void BlankNode() throws ParseException
	{
		Token t;
		t = jj_consume_token(BLANK_NODE_LABEL);
		emitBNode(t.beginLine, t.beginColumn, stripChars(t.image, 2));
	}

	final public void RDFLiteral() throws ParseException
	{
		Token t = null;
		int currLine;
		int currColumn;
		String lex;
		String lang = null;
		String dt_iri = null;
		String dt_pn = null;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
		{
			case STRING_LITERAL1:
				t = jj_consume_token(STRING_LITERAL1);
				lex = stripQuotes(t.image);
				break;
			case STRING_LITERAL2:
				t = jj_consume_token(STRING_LITERAL2);
				lex = stripQuotes(t.image);
				break;
			case STRING_LITERAL_LONG1:
				t = jj_consume_token(STRING_LITERAL_LONG1);
				lex = stripQuotes3(t.image);
				break;
			case STRING_LITERAL_LONG2:
				t = jj_consume_token(STRING_LITERAL_LONG2);
				lex = stripQuotes3(t.image);
				break;
			default:
				jj_la1[12] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
		}
		currLine = t.beginLine;
		currColumn = t.beginColumn;
		lex = unescapeStr(lex, currLine, currColumn);
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
		{
			case DATATYPE:
			case LANGTAG:
				switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
				{
					case LANGTAG:
						t = jj_consume_token(LANGTAG);
						lang = stripChars(t.image, 1);
						break;
					case DATATYPE:
						jj_consume_token(DATATYPE);
						switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
						{
							case IRIref:
								t = jj_consume_token(IRIref);
								dt_iri = stripQuotes(t.image);
								break;
							case PNAME:
								t = jj_consume_token(PNAME);
								dt_pn = t.image;
								break;
							default:
								jj_la1[13] = jj_gen;
								jj_consume_token(-1);
								throw new ParseException();
						}
						break;
					default:
						jj_la1[14] = jj_gen;
						jj_consume_token(-1);
						throw new ParseException();
				}
				break;
			default:
				jj_la1[15] = jj_gen;
				;
		}
		emitLiteral(currLine, currColumn, lex, lang, dt_iri, dt_pn);
	}

	final public void NumericLiteral() throws ParseException
	{
		Token t;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk)
		{
			case INTEGER:
				t = jj_consume_token(INTEGER);
				emitLiteralInteger(t.beginLine, t.beginColumn, t.image);
				break;
			case DECIMAL:
				t = jj_consume_token(DECIMAL);
				emitLiteralDecimal(t.beginLine, t.beginColumn, t.image);
				break;
			case DOUBLE:
				t = jj_consume_token(DOUBLE);
				emitLiteralDouble(t.beginLine, t.beginColumn, t.image);
				break;
			default:
				jj_la1[16] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
		}
	}

	/** Generated Token Manager. */
	public SSE_ParserCoreTokenManager token_source;
	JavaCharStream jj_input_stream;
	/** Current token. */
	public Token token;
	/** Next token. */
	public Token jj_nt;
	private int jj_ntk;
	private int jj_gen;
	final private int[] jj_la1 = new int[17];
	static private int[] jj_la1_0;
	static private int[] jj_la1_1;
	static
	{
		jj_la1_init_0();
		jj_la1_init_1();
	}

	private static void jj_la1_init_0()
	{
		jj_la1_0 = new int[] { 0x2, 0x2, 0x23e1df0, 0x2, 0x2, 0x2, 0x2, 0x2200000, 0x23e1df0, 0x1e1df0, 0x180, 0x1e1c00, 0x1e0000, 0x30, 0x18000000,
				0x18000000, 0x1c00, };
	}

	private static void jj_la1_init_1()
	{
		jj_la1_1 = new int[] { 0x0, 0x0, 0x200, 0x0, 0x0, 0x0, 0x0, 0x0, 0x200, 0x200, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, };
	}

	/** Constructor with InputStream. */
	public SSE_ParserCore(java.io.InputStream stream)
	{
		this(stream, null);
	}

	/** Constructor with InputStream and supplied encoding */
	public SSE_ParserCore(java.io.InputStream stream, String encoding)
	{
		try
		{
			jj_input_stream = new JavaCharStream(stream, encoding, 1, 1);
		}
		catch (java.io.UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
		token_source = new SSE_ParserCoreTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 17; i++)
			jj_la1[i] = -1;
	}

	/** Reinitialise. */
	public void ReInit(java.io.InputStream stream)
	{
		ReInit(stream, null);
	}

	/** Reinitialise. */
	public void ReInit(java.io.InputStream stream, String encoding)
	{
		try
		{
			jj_input_stream.ReInit(stream, encoding, 1, 1);
		}
		catch (java.io.UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 17; i++)
			jj_la1[i] = -1;
	}

	/** Constructor. */
	public SSE_ParserCore(java.io.Reader stream)
	{
		jj_input_stream = new JavaCharStream(stream, 1, 1);
		token_source = new SSE_ParserCoreTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 17; i++)
			jj_la1[i] = -1;
	}

	/** Reinitialise. */
	public void ReInit(java.io.Reader stream)
	{
		jj_input_stream.ReInit(stream, 1, 1);
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 17; i++)
			jj_la1[i] = -1;
	}

	/** Constructor with generated Token Manager. */
	public SSE_ParserCore(SSE_ParserCoreTokenManager tm)
	{
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 17; i++)
			jj_la1[i] = -1;
	}

	/** Reinitialise. */
	public void ReInit(SSE_ParserCoreTokenManager tm)
	{
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 17; i++)
			jj_la1[i] = -1;
	}

	private Token jj_consume_token(int kind) throws ParseException
	{
		Token oldToken;
		if ((oldToken = token).next != null)
			token = token.next;
		else
			token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		if (token.kind == kind)
		{
			jj_gen++;
			return token;
		}
		token = oldToken;
		jj_kind = kind;
		throw generateParseException();
	}

	/** Get the next Token. */
	final public Token getNextToken()
	{
		if (token.next != null)
			token = token.next;
		else
			token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		jj_gen++;
		return token;
	}

	/** Get the specific Token. */
	final public Token getToken(int index)
	{
		Token t = token;
		for (int i = 0; i < index; i++)
		{
			if (t.next != null)
				t = t.next;
			else
				t = t.next = token_source.getNextToken();
		}
		return t;
	}

	private int jj_ntk()
	{
		if ((jj_nt = token.next) == null)
			return (jj_ntk = (token.next = token_source.getNextToken()).kind);
		else
			return (jj_ntk = jj_nt.kind);
	}

	private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
	private int[] jj_expentry;
	private int jj_kind = -1;

	/** Generate ParseException. */
	public ParseException generateParseException()
	{
		jj_expentries.clear();
		boolean[] la1tokens = new boolean[43];
		if (jj_kind >= 0)
		{
			la1tokens[jj_kind] = true;
			jj_kind = -1;
		}
		for (int i = 0; i < 17; i++)
		{
			if (jj_la1[i] == jj_gen)
			{
				for (int j = 0; j < 32; j++)
				{
					if ((jj_la1_0[i] & (1 << j)) != 0)
					{
						la1tokens[j] = true;
					}
					if ((jj_la1_1[i] & (1 << j)) != 0)
					{
						la1tokens[32 + j] = true;
					}
				}
			}
		}
		for (int i = 0; i < 43; i++)
		{
			if (la1tokens[i])
			{
				jj_expentry = new int[1];
				jj_expentry[0] = i;
				jj_expentries.add(jj_expentry);
			}
		}
		int[][] exptokseq = new int[jj_expentries.size()][];
		for (int i = 0; i < jj_expentries.size(); i++)
		{
			exptokseq[i] = jj_expentries.get(i);
		}
		return new ParseException(token, exptokseq, tokenImage);
	}

	/** Enable tracing. */
	final public void enable_tracing()
	{
	}

	/** Disable tracing. */
	final public void disable_tracing()
	{
	}

}
