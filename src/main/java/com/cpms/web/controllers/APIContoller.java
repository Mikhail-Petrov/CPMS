package com.cpms.web.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class APIContoller {

	@RequestMapping(path = { "/test" }, method = RequestMethod.GET)
	public String API() {
		return "test";
	}

}
