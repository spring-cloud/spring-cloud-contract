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

package org.springframework.cloud.contract.verifier.builder.imports;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
@Deprecated
class ImportDefinitions {

	private final List<String> imports;

	private final List<String> staticImports;

	public ImportDefinitions(List<String> imports, List<String> staticImports) {
		this.imports = imports;
		this.staticImports = staticImports;
	}

	public ImportDefinitions(List<String> imports) {
		this(imports, new ArrayList<>());
	}

	public final List<String> getImports() {
		return imports;
	}

	public final List<String> getStaticImports() {
		return staticImports;
	}

}
