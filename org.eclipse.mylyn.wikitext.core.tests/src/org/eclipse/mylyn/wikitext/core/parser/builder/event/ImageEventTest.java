/*******************************************************************************
 * Copyright (c) 2013 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.wikitext.core.parser.builder.event;

import static org.eclipse.mylyn.internal.wikitext.core.test.EqualityAsserts.assertEquality;
import static org.eclipse.mylyn.internal.wikitext.core.test.EqualityAsserts.assertInequality;
import static org.junit.Assert.assertEquals;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.junit.Test;

public class ImageEventTest {

	@Test
	public void testToString() {
		assertEquals("image(http://example.com/img.png)",
				new ImageEvent(new Attributes(), "http://example.com/img.png").toString());
	}

	@Test
	public void equals() {
		assertEquality(new ImageEvent(new Attributes(), "http://example.com/img.png"), new ImageEvent(new Attributes(),
				"http://example.com/img.png"));
		assertInequality(new ImageEvent(new Attributes(), "http://example.com/img.png"), new ImageEvent(
				new Attributes(), "http://example.com/img2.png"));
	}
}
