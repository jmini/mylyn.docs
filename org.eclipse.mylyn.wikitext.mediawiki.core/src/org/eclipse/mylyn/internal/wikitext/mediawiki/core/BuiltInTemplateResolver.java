/*******************************************************************************
 * Copyright (c) 2010, 2012 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.mediawiki.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.mylyn.wikitext.mediawiki.core.Template;
import org.eclipse.mylyn.wikitext.mediawiki.core.TemplateResolver;

public class BuiltInTemplateResolver extends TemplateResolver {

	private static Map<String, Template> builtInTemplates = new HashMap<String, Template>();
	static {
		registerTemplate(new Template("mdash", "&nbsp;&mdash; ")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new Template("mdash", "&nbsp;&mdash; ")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new Template("ndash", "&nbsp;&ndash; ")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new Template("emdash", "&nbsp;&mdash; ")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new Template("endash", "&nbsp;&ndash; ")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new DateTimeTemlate("CURRENTYEAR", "yyyy")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new DateTimeTemlate("CURRENTMONTH", "MM")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new DateTimeTemlate("CURRENTMONTHNAME", "MMMMMMMM")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new DateTimeTemlate("CURRENTMONTHNAMEGEN", "MMMMMMMM")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new DateTimeTemlate("CURRENTMONTHABBREV", "MMM")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new DateTimeTemlate("CURRENTDAY", "dd")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new DateTimeTemlate("CURRENTDAY2", "dd")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new DateTimeTemlate("CURRENTDOW", "F")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new DateTimeTemlate("CURRENTDAYNAME", "EEEEEEEE")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new DateTimeTemlate("CURRENTTIME", "HH:mm")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new DateTimeTemlate("CURRENTHOUR", "HH")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new DateTimeTemlate("CURRENTWEEK", "ww")); //$NON-NLS-1$//$NON-NLS-2$
		registerTemplate(new DateTimeTemlate("CURRENTTIMESTAMP", "yyyyMMddHHmmss")); //$NON-NLS-1$//$NON-NLS-2$
	}

	@Override
	public Template resolveTemplate(String templateName) {
		return builtInTemplates.get(templateName);
	}

	private static void registerTemplate(Template template) {
		builtInTemplates.put(template.getName(), template);
	}

}
