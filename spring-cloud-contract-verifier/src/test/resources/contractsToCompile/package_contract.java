package example;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;

class package_contract implements Supplier<Collection<Contract>> {

	@Override
	public Collection<Contract> get() {
		return Collections.singletonList(Contract.make(c -> {
			c.request(r -> {
				r.method(r.PUT());
				r.headers(h -> {
					h.contentType(h.applicationJson());
				});
				r.body(" { \"status\" : \"OK\" } ");
				r.url("/1");
			});
			c.response(r -> {
				r.status(r.OK());
				r.body(" { \"status\" : \"OK\" } ");
				r.headers(h -> {
					h.contentType(h.textPlain());
				});
			});
		}));
	}

}
