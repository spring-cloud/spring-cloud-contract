package io.codearte.accurest.dsl.internal

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import io.codearte.accurest.util.MapConverter

import java.util.regex.Pattern

@CompileStatic
class JsonStructureConverter {

	public static final String TEMPORARY_PLACEHOLDER = '###PLACEHOLDER###'
	public static final Pattern TEMPORARY_PATTERN_HOLDER = Pattern.compile(TEMPORARY_PLACEHOLDER)
	
	static Object convertJsonStructureToObjectUnderstandingStructure(Object parsedJson,
															Closure<Boolean> retrievePlaceholders,
															Closure<String> performAdditionalLogicOnSerializedJson,
															Closure convertSerializedJsonToSth) {
		LinkedList<Object> queue = new LinkedList<>()
		def transformedJson = MapConverter.transformValues(parsedJson, {
			if(retrievePlaceholders(it)) {
				queue.push(it)
				return TEMPORARY_PLACEHOLDER
			}
			return it
		})
		String jsonAsString =  JsonOutput.toJson(transformedJson)
		String transformedJsonAsString = performAdditionalLogicOnSerializedJson(jsonAsString)
		return convertSerializedJsonToSth(queue, transformedJsonAsString)
	}
	
}
