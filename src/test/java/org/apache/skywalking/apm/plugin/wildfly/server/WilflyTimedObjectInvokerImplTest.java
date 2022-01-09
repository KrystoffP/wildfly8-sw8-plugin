package org.apache.skywalking.apm.plugin.wildfly.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

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
import org.jboss.as.ejb3.component.EJBComponent;
import org.jboss.as.ejb3.timerservice.TimedObjectInvokerImpl;
import org.jboss.modules.Module;
import org.jboss.msc.value.InjectedValue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import io.undertow.util.HeaderValues;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(TracingSegmentRunner.class)
public class WilflyTimedObjectInvokerImplTest {
	
	
	@SegmentStoragePoint
	private SegmentStorage segmentStorage;
	@Rule
	public AgentServiceRule serviceRule = new AgentServiceRule();

	@Mock
	private MethodInterceptResult methodInterceptResult;

	@Mock
	private TimedObjectInvokerImpl4Tests enhancedInstance;
	//private EnhancedInstance enhancedInstance;

	@Mock
	InjectedValue<EJBComponent> ejbComponent;
	
	@Mock
	EJBComponent value;

	
	private WilflyTimedObjectInvokerImpl interceptor;

	@Mock
	HeaderValues headerValues;

	private String componentName = "beanName";
	private String fullURL = "earName.ejbModule.beanName";
	
	@Before
	public void setUp() throws Exception {
		interceptor = new WilflyTimedObjectInvokerImpl();

		when(enhancedInstance.getTimedObjectId()).thenReturn(fullURL);
		when(value.getComponentName()).thenReturn(componentName);
		when(ejbComponent.getValue()).thenReturn(value);
		when(enhancedInstance.getEjbComponent()).thenReturn(ejbComponent);
	}


	@Test
	public void testStatusCodeIsOk() throws Throwable {
		interceptor.beforeMethod(enhancedInstance, null, null, null, methodInterceptResult);
		interceptor.afterMethod(enhancedInstance, null, null, null, null);

		assertThat(segmentStorage.getTraceSegments().size(), is(1));
		TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
		List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);

		Wilfly8TestHelper.assertHttpSpan(spans.get(0), componentName, fullURL, SpanLayer.RPC_FRAMEWORK);
		
	}

//	@Test
//	public void testStatusCodeIsOkWithContextData() throws Throwable {
		//As it's de local "job" -> can't be called from outside -> No chained propagation to test

//	}

	@Test
	public void testWithOccurException() throws Throwable {
		interceptor.beforeMethod(enhancedInstance, null, null, null, methodInterceptResult);
		interceptor.handleMethodException(enhancedInstance, null, null, null, new RuntimeException());
		interceptor.afterMethod(enhancedInstance, null, null, null, null);

		assertThat(segmentStorage.getTraceSegments().size(), is(1));
		TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
		List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);

		Wilfly8TestHelper.assertHttpSpan(spans.get(0), componentName, fullURL, SpanLayer.RPC_FRAMEWORK);
		Wilfly8TestHelper.assertErrorSpan(spans.get(0));
		
	}

	
	public abstract class TimedObjectInvokerImpl4Tests extends TimedObjectInvokerImpl implements EnhancedInstance {

		private static final long serialVersionUID = 7063252369745885415L;

		public TimedObjectInvokerImpl4Tests(String deploymentString, Module module) {
			super(deploymentString, module);
			// TODO Auto-generated constructor stub
		}
		
	}
	
}