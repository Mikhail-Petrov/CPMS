package com.cpms.tests.web;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestLanding.class,
	TestPagination.class,
	TestProfileLifecycle.class
})
public class WebSuite {

}
