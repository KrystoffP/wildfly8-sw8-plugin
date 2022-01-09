package org.apache.skywalking.apm.plugin.wildfly.server;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	WilflyInterceptorEJBInvocationHandlerTest.class,
	WilflyLocalEjbInterceptorTest.class,
	WilflyMethodInvocationMessageTest.class,
	WilflyServletDispatchingTest.class,
	WilflyTimedObjectInvokerImplTest.class
})
public class Wildfly8TestSuite {

}
