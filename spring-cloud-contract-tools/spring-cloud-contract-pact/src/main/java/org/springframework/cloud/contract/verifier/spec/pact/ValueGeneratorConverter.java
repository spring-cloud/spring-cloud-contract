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

package org.springframework.cloud.contract.verifier.spec.pact;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

import au.com.dius.pact.core.model.generators.Category;
import au.com.dius.pact.core.model.generators.DateGenerator;
import au.com.dius.pact.core.model.generators.DateTimeGenerator;
import au.com.dius.pact.core.model.generators.Generator;
import au.com.dius.pact.core.model.generators.Generators;
import au.com.dius.pact.core.model.generators.RandomBooleanGenerator;
import au.com.dius.pact.core.model.generators.RandomDecimalGenerator;
import au.com.dius.pact.core.model.generators.RandomHexadecimalGenerator;
import au.com.dius.pact.core.model.generators.RandomIntGenerator;
import au.com.dius.pact.core.model.generators.RandomStringGenerator;
import au.com.dius.pact.core.model.generators.RegexGenerator;
import au.com.dius.pact.core.model.generators.TimeGenerator;
import au.com.dius.pact.core.model.generators.UuidGenerator;
import groovy.lang.GString;
import org.codehaus.plexus.util.StringUtils;

import org.springframework.cloud.contract.spec.internal.Body;
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.OutputMessage;
import org.springframework.cloud.contract.spec.internal.RegexProperty;
import org.springframework.cloud.contract.verifier.util.ContentUtils;

/**
 * Convert a value for a given {@link Generator}.
 *
 * @author Tim Ysewyn
 * @Since 2.0.0
 */
final class ValueGeneratorConverter {

	private static final String INTEGER_PATTERN = "-?(\\d+)";

	private static final Pattern INTEGER = Pattern.compile(INTEGER_PATTERN);

	private static final String DECIMAL_PATTERN = "-?(\\d*\\.\\d+)";

	private static final Pattern DECIMAL = Pattern.compile(DECIMAL_PATTERN);

	private static final String HEX_PATTERN = "[a-fA-F0-9]+";

	private static final Pattern HEX = Pattern.compile(HEX_PATTERN);

	private static final String ALPHA_NUMERIC_PATTERN = "[a-zA-Z0-9]+";

	private static final Pattern ALPHA_NUMERIC = Pattern.compile(ALPHA_NUMERIC_PATTERN);

	private static final String UUID_PATTERN = "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}";

	private static final Pattern UUID = Pattern.compile(UUID_PATTERN);

	private static final String ANY_DATE_PATTERN = "(\\d\\d\\d\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])";

	private static final Pattern ANY_DATE = Pattern.compile(ANY_DATE_PATTERN);

	private static final String ANY_TIME_PATTERN = "(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])";

	private static final Pattern ANY_TIME = Pattern.compile(ANY_TIME_PATTERN);

	private static final String ANY_DATE_TIME_PATTERN = "([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])";

	private static final Pattern ANY_DATE_TIME = Pattern.compile(ANY_DATE_TIME_PATTERN);

	private static final String TRUE_OR_FALSE_PATTERN = "(true|false)";

	private static final Pattern TRUE_OR_FALSE = Pattern.compile(TRUE_OR_FALSE_PATTERN);

	private ValueGeneratorConverter() {

	}

	static DslProperty<Object> convert(Generator generator,
			BiFunction<Pattern, Object, DslProperty<Object>> dslPropertyProvider) {
		Pattern pattern = null;
		if (generator instanceof RandomIntGenerator) {
			pattern = INTEGER;
		}
		else if (generator instanceof RandomDecimalGenerator) {
			pattern = DECIMAL;
		}
		else if (generator instanceof RandomHexadecimalGenerator) {
			pattern = HEX;
		}
		else if (generator instanceof RandomStringGenerator) {
			pattern = ALPHA_NUMERIC;
		}
		else if (generator instanceof RegexGenerator) {
			pattern = Pattern.compile(((RegexGenerator) generator).getRegex());
		}
		else if (generator instanceof UuidGenerator) {
			pattern = UUID;
		}
		else if (generator instanceof DateGenerator) {
			pattern = getDateTimePattern(((DateGenerator) generator).getFormat(),
					ANY_DATE);
		}
		else if (generator instanceof TimeGenerator) {
			pattern = getDateTimePattern(((TimeGenerator) generator).getFormat(),
					ANY_TIME);
		}
		else if (generator instanceof DateTimeGenerator) {
			pattern = getDateTimePattern(((DateTimeGenerator) generator).getFormat(),
					ANY_DATE_TIME);
		}
		else if (generator instanceof RandomBooleanGenerator) {
			pattern = TRUE_OR_FALSE;
		}
		if (pattern == null) {
			throw new UnsupportedOperationException(
					"We currently don't support a generator of type "
							+ generator.getClass().getSimpleName());
		}
		else {
			Object generatedValue = generator.generate(new HashMap<>());
			return dslPropertyProvider.apply(pattern, generatedValue);
		}
	}

	private static Pattern getDateTimePattern(String format, Pattern defaultPattern) {
		return StringUtils.isNotBlank(format) ? Pattern.compile(format) : defaultPattern;
	}

	static Generators extract(Body body,
			Function<DslProperty<?>, Object> dslPropertyValueProvider) {
		Generators generators = new Generators();
		traverse(body, dslPropertyValueProvider, "", generators, Category.BODY);
		return generators;
	}

	static Generators extract(OutputMessage message,
			Function<DslProperty<?>, Object> dslPropertyValueProvider) {
		Generators generators = new Generators();
		traverse(message.getBody(), dslPropertyValueProvider, "", generators,
				Category.BODY);
		return generators;
	}

	private static void traverse(Object value,
			Function<DslProperty<?>, Object> dslPropertyValueProvider, String path,
			Generators generators, Category category) {
		Object v = value;
		if (v instanceof DslProperty) {
			v = dslPropertyValueProvider.apply((DslProperty<?>) v);
		}
		if (v instanceof GString) {
			v = ContentUtils.extractValue((GString) v, dslPropertyValueProvider);
		}
		if (v instanceof Map) {
			((Map) v).forEach((key, val) -> traverse(val, dslPropertyValueProvider,
					path + "." + key, generators, category));
		}
		else if (v instanceof Collection) {
			AtomicInteger index = new AtomicInteger();
			((Collection<?>) v).forEach(val -> traverse(val, dslPropertyValueProvider,
					path + "[" + index.getAndIncrement() + "]", generators, category));
		}
		else if (v instanceof DslProperty) {
			traverse(v, dslPropertyValueProvider, path, generators, category);
		}
		else if (v instanceof RegexProperty || v instanceof Pattern) {
			RegexProperty regexProperty = new RegexProperty(v);
			switch (regexProperty.pattern()) {
			case INTEGER_PATTERN:
				generators.addGenerator(category, path,
						new RandomIntGenerator(0, Integer.MAX_VALUE));
				break;
			case DECIMAL_PATTERN:
				generators.addGenerator(category, path, new RandomDecimalGenerator(10));
				break;
			case HEX_PATTERN:
				generators.addGenerator(category, path,
						new RandomHexadecimalGenerator(10));
				break;
			case ALPHA_NUMERIC_PATTERN:
				generators.addGenerator(category, path, new RandomStringGenerator(10));
				break;
			case UUID_PATTERN:
				generators.addGenerator(category, path, UuidGenerator.INSTANCE);
				break;
			case ANY_DATE_PATTERN:
				generators.addGenerator(category, path, new DateGenerator());
				break;
			case ANY_TIME_PATTERN:
				generators.addGenerator(category, path, new TimeGenerator());
				break;
			case ANY_DATE_TIME_PATTERN:
				generators.addGenerator(category, path, new DateTimeGenerator());
				break;
			case TRUE_OR_FALSE_PATTERN:
				generators.addGenerator(category, path, RandomBooleanGenerator.INSTANCE);
				break;
			default:
				generators.addGenerator(category, path,
						new RegexGenerator(regexProperty.pattern()));
				break;
			}
		}
	}

}
