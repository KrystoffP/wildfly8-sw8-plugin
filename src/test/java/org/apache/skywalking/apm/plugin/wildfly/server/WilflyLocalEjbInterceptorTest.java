package org.apache.skywalking.apm.plugin.wildfly.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.skywalking.apm.agent.core.context.trace.AbstractTracingSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.context.trace.TraceSegment;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.test.helper.SegmentHelper;
import org.apache.skywalking.apm.agent.test.tools.AgentServiceRule;
import org.apache.skywalking.apm.agent.test.tools.SegmentStorage;
import org.apache.skywalking.apm.agent.test.tools.SegmentStoragePoint;
import org.apache.skywalking.apm.agent.test.tools.TracingSegmentRunner;
import org.jboss.ejb.client.EJBClientInvocationContext;
import org.jboss.ejb.client.EJBLocator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(TracingSegmentRunner.class)
public class WilflyLocalEjbInterceptorTest {

	@SegmentStoragePoint
	private SegmentStorage segmentStorage;
	@Rule
	public AgentServiceRule serviceRule = new AgentServiceRule();

	@Mock
	private MethodInterceptResult methodInterceptResult;

	@Mock
	private EnhancedInstance enhancedInstance;

	private WilflyLocalEjbInterceptor interceptor;


	public final static String fullURL = "localEjb://earAppNameAsStr/earModNameAsStr/earBeanNameAsStr#setUp";

	@Mock
	private EJBClientInvocationContext ejbIC;

	@SuppressWarnings("rawtypes")
	@Mock
	EJBLocator locator;

	Method ejbMethod;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		interceptor = new WilflyLocalEjbInterceptor();

		when(locator.getAppName()).thenReturn(Wilfly8TestHelper.EAR_APP_NAME_AS_STR);
		when(locator.getModuleName()).thenReturn(Wilfly8TestHelper.EAR_MOD_NAME_AS_STR);
		when(locator.getBeanName()).thenReturn(Wilfly8TestHelper.EAR_BEAN_NAME_AS_STR);

		ejbMethod = WilflyMethodInvocationMessageTest.class.getMethod("setUp");

		when(ejbIC.getLocator()).thenReturn(locator);
		when(ejbIC.getInvokedMethod()).thenReturn(ejbMethod);

	}

	@Test
	public void testStatusCodeIsOk() throws Throwable {

		Object[] arguments = { ejbIC };
		@SuppressWarnings("rawtypes")
		Class[] argumentType = new Class[] { ejbIC.getClass() };

		interceptor.beforeMethod(enhancedInstance, null, arguments, argumentType, methodInterceptResult);
		interceptor.afterMethod(enhancedInstance, null, arguments, argumentType, null);

		assertThat(segmentStorage.getTraceSegments().size(), is(1));
		TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
		List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);

		Wilfly8TestHelper.assertHttpLocalSpan(spans.get(0), Wilfly8TestHelper.EJB_METHOD_NAME_AS_STR, fullURL, SpanLayer.RPC_FRAMEWORK);

	}

//	@Test
//	public void testStatusCodeIsOkWithContextData() throws Throwable {
		// LocalEJB Call are a LocalEntry in the SW8 meaning - no chained propagation to
		// test

//	}

	@Test
	public void testWithOccurException() throws Throwable {
		Object[] arguments = { ejbIC };
		@SuppressWarnings("rawtypes")
		Class[] argumentType = new Class[] { ejbIC.getClass() };

		interceptor.beforeMethod(enhancedInstance, null, arguments, argumentType, methodInterceptResult);
		interceptor.handleMethodException(enhancedInstance, null, arguments, argumentType, new RuntimeException());
		interceptor.afterMethod(enhancedInstance, null, arguments, argumentType, null);

		assertThat(segmentStorage.getTraceSegments().size(), is(1));
		TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
		List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);

		Wilfly8TestHelper.assertHttpLocalSpan(spans.get(0), Wilfly8TestHelper.EJB_METHOD_NAME_AS_STR, fullURL, SpanLayer.RPC_FRAMEWORK);
		Wilfly8TestHelper.assertErrorSpan(spans.get(0));
	}

}