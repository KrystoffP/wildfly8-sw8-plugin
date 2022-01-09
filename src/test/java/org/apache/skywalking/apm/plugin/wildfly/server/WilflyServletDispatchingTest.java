package org.apache.skywalking.apm.plugin.wildfly.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(TracingSegmentRunner.class)
public class WilflyServletDispatchingTest {
	
	
	@SegmentStoragePoint
	private SegmentStorage segmentStorage;
	@Rule
	public AgentServiceRule serviceRule = new AgentServiceRule();

	@Mock
	private MethodInterceptResult methodInterceptResult;

	@Mock
	private EnhancedInstance enhancedInstance;

	@Mock
	private HttpServerExchange hse;

	private WilflyServletDispatching interceptor;

	@Mock
	HeaderValues headerValues;

	private String ctxPath = "/test/testRequestURL";
	private String fullURL = "http://localhost:8080" + ctxPath;
	
	@Before
	public void setUp() throws Exception {
		interceptor = new WilflyServletDispatching();

		when(hse.getRequestURL()).thenReturn("http://localhost:8080" + ctxPath);
		when(hse.getRequestPath()).thenReturn(ctxPath);
		when(hse.getRequestMethod()).thenReturn(new HttpString("GET"));
		when(hse.getRequestHeaders()).thenReturn(new HeaderMap());
	}


	@Test
	public void testStatusCodeIsOk() throws Throwable {
		Object[] arguments = { hse };
		@SuppressWarnings("rawtypes")
		Class[] argumentType = new Class[] { hse.getClass() };
		
		interceptor.beforeMethod(enhancedInstance, null, arguments, argumentType, methodInterceptResult);
		interceptor.afterMethod(enhancedInstance, null, arguments, argumentType, null);

		assertThat(segmentStorage.getTraceSegments().size(), is(1));
		TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
		List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);

		Wilfly8TestHelper.assertHttpSpan(spans.get(0), ctxPath, fullURL, SpanLayer.HTTP);
		
	}

	@Test
	public void testStatusCodeIsOkWithContextData() throws Throwable {
		//Override previous mock in the setup to get a chained span
		HeaderMap headerMap = new HeaderMap();
		headerMap.add(new HttpString(SW8CarrierItem.HEADER_NAME),
				"1-My40LjU=-MS4yLjM=-3-c2VydmljZQ==-aW5zdGFuY2U=-L2FwcA==-MTI3LjAuMC4xOjgwODA=");
		when(hse.getRequestHeaders()).thenReturn(headerMap);

		Object[] arguments = { hse };
		@SuppressWarnings("rawtypes")
		Class[] argumentType = new Class[] { hse.getClass() };
		
		interceptor.beforeMethod(enhancedInstance, null, arguments, argumentType, methodInterceptResult);
		interceptor.afterMethod(enhancedInstance, null, arguments, argumentType, null);

		assertThat(segmentStorage.getTraceSegments().size(), is(1));
		TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
		List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);

		Wilfly8TestHelper.assertHttpSpan(spans.get(0), ctxPath, fullURL, SpanLayer.HTTP);
		Wilfly8TestHelper.assertTraceSegmentRef(traceSegment.getRef());

	}

	@Test
	public void testWithOccurException() throws Throwable {
		Object[] arguments = { hse };
		@SuppressWarnings("rawtypes")
		Class[] argumentType = new Class[] { hse.getClass() };
		
		interceptor.beforeMethod(enhancedInstance, null, arguments, argumentType, methodInterceptResult);
		interceptor.handleMethodException(enhancedInstance, null, arguments, argumentType, new RuntimeException());
		interceptor.afterMethod(enhancedInstance, null, arguments, argumentType, null);

		assertThat(segmentStorage.getTraceSegments().size(), is(1));
		TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
		List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);

		Wilfly8TestHelper.assertHttpSpan(spans.get(0), ctxPath, fullURL, SpanLayer.HTTP);
		Wilfly8TestHelper.assertErrorSpan(spans.get(0));
		
	}

}