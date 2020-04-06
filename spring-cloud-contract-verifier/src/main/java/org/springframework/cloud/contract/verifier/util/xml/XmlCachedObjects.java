/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.util.xml;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.wst.xml.xpath2.processor.util.StaticContextBuilder;
import org.w3c.dom.Document;

/**
 * Contains cached objects that are memory consuming.
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
class XmlCachedObjects {

	final Document document;

	final StaticContextBuilder xpathBuilder;

	final String xmlAsString;

	XmlCachedObjects(Document document) {
		this.document = document;
		this.xpathBuilder = new StaticContextBuilder();
		this.xmlAsString = xmlAsString();
	}

	XmlCachedObjects(Document document, String xmlAsString) {
		this.document = document;
		this.xpathBuilder = new StaticContextBuilder();
		this.xmlAsString = xmlAsString;
	}

	private String xmlAsString() {
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(this.document), new StreamResult(writer));
			return writer.getBuffer().toString().replaceAll("\n|\r", "");
		}
		catch (TransformerException e) {
			throw new RuntimeException(
					"Exception occured while trying to convert XML Document to String",
					e);
		}
	}

}
