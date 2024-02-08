package hello;

import java.util.List;

public class GreetingResponse {
	String name;
	List<Account> accounts;

	public GreetingResponse(String name, List<Account> accounts) {
		this.name = name;
		this.accounts = accounts;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<Account> accounts) {
		this.accounts = accounts;
	}

	static class Account {

		String key;

		public Account(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

	}
}
