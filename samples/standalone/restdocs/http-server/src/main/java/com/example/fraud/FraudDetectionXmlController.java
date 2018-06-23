package com.example.fraud;

import java.math.BigDecimal;

import com.example.fraud.model.FraudCheck;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Controller
public class FraudDetectionXmlController {

	@RequestMapping(
			value = "/xmlfraud",
			method = POST,
			consumes = MediaType.APPLICATION_XML_VALUE,
			produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public XmlResponseBody xmlResponseBody(@RequestBody XmlRequestBody xmlRequestBody) {
		if (StringUtils.isEmpty(xmlRequestBody.name)) {
			return new XmlResponseBody("EMPTY");
		}
		return new XmlResponseBody("FULL");
	}

}

class XmlRequestBody {
	public String name;
}

class XmlResponseBody {
	public String status;

	public XmlResponseBody(String status) {
		this.status = status;
	}

	public XmlResponseBody() {
	}
}