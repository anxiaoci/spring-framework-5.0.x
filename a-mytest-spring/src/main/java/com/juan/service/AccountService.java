package com.juan.service;

import org.springframework.beans.factory.annotation.Required;

public class AccountService {
	private PersonService personService;

	@Required
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}


}
