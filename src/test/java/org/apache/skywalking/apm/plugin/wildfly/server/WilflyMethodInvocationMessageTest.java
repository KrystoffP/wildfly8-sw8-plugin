package org.apache.skywalking.apm.plugin.wildfly.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.apm.agent.core.context.SW8CarrierItem;
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
public class WilflyMethodInvocationMessageTest {

	@SegmentStoragePoint
	private SegmentStorage segmentStorage;
	@Rule
	public AgentServiceRule serviceRule = new AgentServiceRule();

	@Mock
	private MethodInterceptResult methodInterceptResult;

	@Mock
	private EnhancedInstance enhancedInstance;

	

	

	private WilflyMethodInvocationMessage interceptor;

	// Choose setUp as name of method because can't mock Method class
	String ejbMethodeNameAsStr = "setUp";
	String earAppNameAsStr = "earAppNameAsStr";
	String earModNameAsStr = "earModNameAsStr";
	String earBeanNameAsStr = "earBeanNameAsStr";
	
	// the ejbMethod - not the intercepted method..
	@SuppressWarnings("rawtypes")
	@Mock
	private EJBLocator locator;
	
	@Mock
	private Map<String, Object> attachments;

	private Method ejbMethod;
	
	@Before
	public void setUp() throws Exception {
		interceptor = new WilflyMethodInvocationMessage();

		when(locator.getAppName()).thenReturn(earAppNameAsStr);
		when(locator.getModuleName()).thenReturn(earModNameAsStr);
		when(locator.getBeanName()).thenReturn(earBeanNameAsStr);
		//Line didn't wok as expected -> see https://stackoverflow.com/questions/62742777/org-mockito-exceptions-misusing-notamockexception-argument-should-be-a-mock-bu
		// ==> Instance
//		when(ejbMethod.getName()).thenReturn(ejbMethodeNameAsStr);
		ejbMethod =  WilflyMethodInvocationMessageTest.class.getMethod("setUp");
	}

	
	
	@Test
	public void testStatusCodeIsOk() throws Throwable {

		Object[] arguments = { this, this, ejbMethod, this, locator, attachments };
		@SuppressWarnings("rawtypes")
		Class[] argumentType = new Class[] { this.getClass(), this.getClass(), ejbMethod.getClass(), this.getClass(),
				locator.getClass(), attachments.getClass() };

		interceptor.beforeMethod(enhancedInstance, null, arguments, argumentType, methodInterceptResult);
		interceptor.afterMethod(enhancedInstance, null, arguments, argumentType, null);

		assertThat(segmentStorage.getTraceSegments().size(), is(1));
		TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
		List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);

		Wilfly8TestHelper.assertHttpSpan(spans.get(0), ejbMethodeNameAsStr, Wilfly8TestHelper.FULL_URL_EJB_REMOTE, SpanLayer.RPC_FRAMEWORK);

	}

	@Test
	public void testStatusCodeIsOkWithContextData() throws Throwable {
		// Sw8 header to get chained
		when(attachments.get(SW8CarrierItem.HEADER_NAME)).thenReturn("1-My40LjU=-MS4yLjM=-3-c2VydmljZQ==-aW5zdGFuY2U=-L2FwcA==-MTI3LjAuMC4xOjgwODA=");

		Object[] arguments = { this, this, ejbMethod, this, locator, attachments };
		@SuppressWarnings("rawtypes")
		Class[] argumentType = new Class[] { this.getClass(), this.getClass(), ejbMethod.getClass(), this.getClass(),
				locator.getClass(), attachments.getClass() };

		interceptor.beforeMethod(enhancedInstance, null, arguments, argumentType, methodInterceptResult);
		interceptor.afterMethod(enhancedInstance, null, arguments, argumentType, null);

		assertThat(segmentStorage.getTraceSegments().size(), is(1));
		TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
		List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);

		Wilfly8TestHelper.assertHttpSpan(spans.get(0), ejbMethodeNameAsStr, Wilfly8TestHelper.FULL_URL_EJB_REMOTE, SpanLayer.RPC_FRAMEWORK);
		Wilfly8TestHelper.assertTraceSegmentRef(traceSegment.getRef());

	}

	
	
	@Test
	public void testWithOccurException() throws Throwable {
		Object[] arguments = { this, this, ejbMethod, this, locator, attachments };
		@SuppressWarnings("rawtypes")
		Class[] argumentType = new Class[] { this.getClass(), this.getClass(), ejbMethod.getClass(), this.getClass(),
				locator.getClass(), attachments.getClass() };

		interceptor.beforeMethod(enhancedInstance, null, arguments, argumentType, methodInterceptResult);
		interceptor.handleMethodException(enhancedInstance, null, arguments, argumentType, new RuntimeException());
		interceptor.afterMethod(enhancedInstance, null, arguments, argumentType, null);

		assertThat(segmentStorage.getTraceSegments().size(), is(1));
		TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
		List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);

		Wilfly8TestHelper.assertHttpSpan(spans.get(0), ejbMethodeNameAsStr, Wilfly8TestHelper.FULL_URL_EJB_REMOTE, SpanLayer.RPC_FRAMEWORK);
		Wilfly8TestHelper.assertErrorSpan(spans.get(0));
	}


}