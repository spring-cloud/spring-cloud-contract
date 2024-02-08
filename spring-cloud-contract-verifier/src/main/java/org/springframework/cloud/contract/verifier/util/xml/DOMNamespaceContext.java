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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DOMNamespaceContext implements NamespaceContext {

	private final Map<String, String> namespaceMap = new HashMap<>();

	public DOMNamespaceContext(Node contextNode) {
		addNamespaces(contextNode);
	}

	public String getNamespaceURI(String arg0) {
		return namespaceMap.get(arg0);
	}

	public String getPrefix(String arg0) {
		for (Map.Entry<String, String> entry : namespaceMap.entrySet()) {
			if (entry.getValue().equals(arg0)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public Iterator<String> getPrefixes(String arg0) {
		return namespaceMap.keySet().iterator();
	}

	private void addNamespaces(Node element) {
		if (element.getChildNodes() != null) {
			// loops through child Element Nodes and check for namespace attributes
			for (int i = 0; i < element.getChildNodes().getLength(); i++) {
				if (element.getChildNodes().item(i) instanceof Element) {
					addNamespaces(element.getChildNodes().item(i));
				}
			}
		}
		if (element instanceof Element) {
			Element el = (Element) element;
			NamedNodeMap map = el.getAttributes();
			for (int x = 0; x < map.getLength(); x++) {
				Attr attr = (Attr) map.item(x);
				if ("xmlns".equals(attr.getPrefix())) {
					namespaceMap.putIfAbsent(attr.getLocalName(), attr.getValue());
				}
			}
		}
	}

}
