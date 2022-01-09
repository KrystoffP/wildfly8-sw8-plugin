package org.apache.skywalking.apm.plugin.wildfly.server;

import static org.apache.skywalking.apm.agent.test.tools.SpanAssert.assertComponent;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.apache.skywalking.apm.agent.core.context.trace.AbstractTracingSpan;
import org.apache.skywalking.apm.agent.core.context.trace.LogDataEntity;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.context.trace.TraceSegmentRef;
import org.apache.skywalking.apm.agent.test.helper.SegmentRefHelper;
import org.apache.skywalking.apm.agent.test.helper.SpanHelper;
import org.apache.skywalking.apm.agent.test.tools.SpanAssert;

public class Wilfly8TestHelper {
	
	// Choose setUp as name of test method because can't mock Method class
	public final static String EJB_METHOD_NAME_AS_STR = "earBeanNameAsStr#setUp";
	public final static String EAR_APP_NAME_AS_STR = "earAppNameAsStr";
	public final static String EAR_MOD_NAME_AS_STR = "earModNameAsStr";
	public final static String EAR_BEAN_NAME_AS_STR = "earBeanNameAsStr";

	public final static String FULL_URL_EJB_REMOTE = "remoteEjb://earAppNameAsStr/earModNameAsStr/earBeanNameAsStr#setUp";
	
	
	public static void assertHttpSpan(AbstractTracingSpan span, String operationName, String url, SpanLayer spanLayer ) {
		assertThat(span.getOperationName(), is(operationName));
		assertComponent(span, WildflySharedInfos.WILDFLY_SERVER);
		SpanAssert.assertTag(span, 0, url);
		assertThat(span.isEntry(), is(true));
		SpanAssert.assertLayer(span, spanLayer);
	}
	
	public static void assertHttpLocalSpan(AbstractTracingSpan span, String operationName, String url, SpanLayer spanLayer ) {
		assertThat(span.getOperationName(), is(operationName));
		assertComponent(span, WildflySharedInfos.WILDFLY_SERVER);
		SpanAssert.assertTag(span, 0, url);
		assertThat(span.isEntry(), is(false));
		assertThat(span.isExit(), is(false));
		SpanAssert.assertLayer(span, spanLayer);
	}

	public static void assertHttpExitSpan(AbstractTracingSpan span, String operationName, String url, SpanLayer spanLayer ) {
		assertThat(span.getOperationName(), is(operationName));
		assertComponent(span, WildflySharedInfos.WILDFLY_SERVER);
		SpanAssert.assertTag(span, 0, url);
		assertThat(span.isEntry(), is(false));
		assertThat(span.isExit(), is(true));
		SpanAssert.assertLayer(span, spanLayer);
	}

	public static void assertTraceSegmentRef(TraceSegmentRef ref) {
		assertThat(SegmentRefHelper.getParentServiceInstance(ref), is("instance"));
		assertThat(SegmentRefHelper.getSpanId(ref), is(3));
		assertThat(SegmentRefHelper.getTraceSegmentId(ref).toString(), is("3.4.5"));
	}

	public static void assertErrorSpan(AbstractTracingSpan span) {
		List<LogDataEntity> logDataEntities = SpanHelper.getLogs(span);
		assertThat(logDataEntities.size(), is(1));
		SpanAssert.assertException(logDataEntities.get(0), RuntimeException.class);
	}
}
