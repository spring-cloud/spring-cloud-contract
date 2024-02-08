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

package contracts

// tag::class[]
import org.springframework.cloud.contract.spec.ContractDsl.Companion.contract

contract {
    request {
        method = GET
        urlPath = path("/get")
        headers {
            contentType = APPLICATION_XML
        }
    }
    response {
        status = OK
        headers {
            contentType =APPLICATION_XML
        }
        body = body("<test>\n" + "<duck type='xtype'>123</duck>\n"
                + "<alpha>abc</alpha>\n" + "<list>\n" + "<elem>abc</elem>\n"
                + "<elem>def</elem>\n" + "<elem>ghi</elem>\n" + "</list>\n"
                + "<number>123</number>\n" + "<aBoolean>true</aBoolean>\n"
                + "<date>2017-01-01</date>\n"
                + "<dateTime>2017-01-01T01:23:45</dateTime>\n"
                + "<time>01:02:34</time>\n"
                + "<valueWithoutAMatcher>foo</valueWithoutAMatcher>\n"
                + "<key><complex>foo</complex></key>\n" + "</test>")
        bodyMatchers {
            xPath("/test/duck/text()", byRegex("[0-9]{3}"))
            xPath("/test/duck/text()", byCommand("equals(\$it)"))
            xPath("/test/duck/xxx", byNull)
            xPath("/test/duck/text()", byEquality)
            xPath("/test/alpha/text()", byRegex(onlyAlphaUnicode))
            xPath("/test/alpha/text()", byEquality)
            xPath("/test/number/text()", byRegex(number))
            xPath("/test/date/text()", byDate)
            xPath("/test/dateTime/text()", byTimestamp)
            xPath("/test/time/text()", byTime)
            xPath("/test/*/complex/text()", byEquality)
            xPath("/test/duck/@type", byEquality)
        }
    }
}
// end::class[]
