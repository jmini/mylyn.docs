/*******************************************************************************
 * Copyright (c) 2007, 2011 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.wikitext.core.parser.builder;

import java.io.StringWriter;

import junit.framework.TestCase;

import org.eclipse.mylyn.wikitext.confluence.core.ConfluenceLanguage;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.core.parser.LinkAttributes;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.TableAttributes;
import org.eclipse.mylyn.wikitext.tests.TestUtil;
import org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;

/**
 * @author David Green
 */
public class DocBookDocumentBuilderTest extends TestCase {

	private MarkupParser parser;

	private StringWriter out;

	private DocBookDocumentBuilder builder;

	@Override
	public void setUp() {
		parser = new MarkupParser();
		parser.setMarkupLanguage(new TextileLanguage());
		out = new StringWriter();
		builder = new DocBookDocumentBuilder(out);
		parser.setBuilder(builder);

	}

	public void testInlineImage() {
		parser.parse("some text !(inline)images/foo.png! some text");
		String docbook = out.toString();
		TestUtil.println("DocBook: \n" + docbook);
		assertTrue(docbook.contains("<inlinemediaobject role=\"inline\"><imageobject><imagedata fileref=\"images/foo.png\"/></imageobject></inlinemediaobject>"));
	}

	public void testInlineQuote() {
		parser.setMarkupLanguage(new ConfluenceLanguage());
		parser.parse("some text {quote}quoted text{quote} some text");
		String docbook = out.toString();
		TestUtil.println("DocBook: \n" + docbook);
		assertTrue(docbook.contains("<para>some text <quote>quoted text</quote> some text</para>"));
	}

	public void testImage() {
		parser.parse("some text !images/foo.png! some text");
		String docbook = out.toString();
		TestUtil.println("DocBook: \n" + docbook);
		assertTrue(docbook.contains("<mediaobject><imageobject><imagedata fileref=\"images/foo.png\"/></imageobject></mediaobject>"));
	}

	public void testImageWithScaling() {
		parser.parse("some text !{width:80%}images/foo.png! some text");
		String docbook = out.toString();
		TestUtil.println("DocBook: \n" + docbook);
		assertTrue(docbook.contains("<mediaobject><imageobject><imagedata fileref=\"images/foo.png\" scale=\"80\"/></imageobject></mediaobject>"));
	}

	public void testDefinitionList() {
		builder.beginDocument();
		builder.beginBlock(BlockType.DEFINITION_LIST, new Attributes());

		builder.beginBlock(BlockType.DEFINITION_TERM, new Attributes());
		builder.characters("foo");
		builder.endBlock(); // DT

		builder.beginBlock(BlockType.DEFINITION_ITEM, new Attributes());
		builder.characters("Foo definition");
		builder.endBlock(); // DI

		builder.beginBlock(BlockType.DEFINITION_TERM, new Attributes());
		builder.characters("bar");
		builder.endBlock(); // DT

		builder.beginBlock(BlockType.DEFINITION_ITEM, new Attributes());
		builder.characters("Bar definition");
		builder.endBlock(); // DI

		builder.endBlock(); // DL
		builder.endDocument();

		String docbook = out.toString();
		TestUtil.println("DocBook: \n" + docbook);

		assertTrue(docbook.contains("<variablelist><varlistentry><term>foo</term><listitem><para>Foo definition</para></listitem></varlistentry><varlistentry><term>bar</term><listitem><para>Bar definition</para></listitem></varlistentry></variablelist>"));
	}

	public void testGlossaryUsesDefinitionList() {
		builder.setAutomaticGlossary(false);
		parser.parse("ABW(A Better Way) is not NIMBY(Not In My Back Yard)\n\n{glossary}");

		String docbook = out.toString();
		TestUtil.println("DocBook: \n" + docbook);

		assertTrue(docbook.contains("<variablelist><varlistentry><term>ABW</term><listitem><para>A Better Way</para></listitem></varlistentry><varlistentry><term>NIMBY</term><listitem><para>Not In My Back Yard</para></listitem></varlistentry></variablelist>"));
	}

	public void testAutomaticGlossaryOnByDefault() {
		assertTrue(builder.isAutomaticGlossary());
	}

	public void testNestedListsCreatesValidDocbook() {
		builder.beginDocument();
		builder.beginBlock(BlockType.BULLETED_LIST, new Attributes());

		builder.beginBlock(BlockType.LIST_ITEM, new Attributes());
		builder.characters("foo");

		builder.beginBlock(BlockType.BULLETED_LIST, new Attributes());

		builder.beginBlock(BlockType.LIST_ITEM, new Attributes());
		builder.characters("bar");
		builder.endBlock(); // LI

		builder.endBlock(); // UL

		builder.characters("foo2");
		builder.endBlock(); // LI

		builder.beginBlock(BlockType.LIST_ITEM, new Attributes());
		builder.characters("baz");
		builder.endBlock(); // LI

		builder.endBlock(); // UL
		builder.endDocument();

		String docbook = out.toString();
		TestUtil.println("DocBook: \n" + docbook);

		// should look something like this:

		//		<itemizedlist>
		//			<listitem>
		//				<para>foo</para>
		//				<itemizedlist>
		//					<listitem>
		//						<para>bar</para>
		//					</listitem>
		//				</itemizedlist>
		//				<para>foo2</para>
		//			</listitem>
		//			<listitem>
		//				<para>baz</para>
		//			</listitem>
		//		</itemizedlist>

		assertTrue(docbook.contains("<itemizedlist><listitem><para>foo</para><itemizedlist><listitem><para>bar</para></listitem></itemizedlist><para>foo2</para></listitem><listitem><para>baz</para></listitem></itemizedlist>"));
	}

	public void testDiv() {
		builder.beginDocument();
		builder.beginBlock(BlockType.DIV, new Attributes());

		builder.beginBlock(BlockType.PARAGRAPH, new Attributes());
		builder.characters("foo");
		builder.endBlock(); // PARAGRAPH

		builder.beginBlock(BlockType.PARAGRAPH, new Attributes());
		builder.characters("bar");
		builder.endBlock(); // PARAGRAPH

		builder.endBlock(); // DIV
		builder.endDocument();

		String docbook = out.toString();
		TestUtil.println("DocBook: \n" + docbook);

		assertTrue(docbook.contains("<book><title></title><chapter><title></title><para>foo</para><para>bar</para></chapter></book>"));
	}

	public void testSpanLink() {
		builder.beginDocument();
		builder.beginBlock(BlockType.DIV, new Attributes());

		builder.beginBlock(BlockType.PARAGRAPH, new Attributes());
		LinkAttributes attributes = new LinkAttributes();
		attributes.setHref("#test1234");
		builder.beginSpan(SpanType.LINK, attributes);
		builder.beginSpan(SpanType.EMPHASIS, new Attributes());
		builder.characters("link text");
		builder.endSpan();
		builder.endSpan();
		builder.endBlock(); // PARAGRAPH

		builder.endBlock(); // DIV
		builder.endDocument();

		String docbook = out.toString();
		TestUtil.println("DocBook: \n" + docbook);

		assertTrue(docbook.contains("<book><title></title><chapter><title></title><para><link linkend=\"test1234\"><emphasis>link text</emphasis></link></para></chapter></book>"));
	}

	public void testTableClass() {
		builder.beginDocument();
		TableAttributes tableAttributes = new TableAttributes();
		tableAttributes.appendCssClass("foo");
		builder.beginBlock(BlockType.TABLE, tableAttributes);

		builder.beginBlock(BlockType.TABLE_ROW, new Attributes());

		builder.beginBlock(BlockType.TABLE_CELL_NORMAL, new Attributes());
		builder.characters("text");
		builder.endBlock(); // cell

		builder.endBlock(); // row

		builder.endBlock(); // table
		builder.endDocument();

		String docbook = out.toString();
		TestUtil.println("DocBook: \n" + docbook);

		assertTrue(docbook.contains("<informaltable role=\"foo\"><tr><td>text</td></tr></informaltable>"));

	}
}
