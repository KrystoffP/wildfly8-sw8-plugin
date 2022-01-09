package org.apache.skywalking.apm.plugin.wildfly.server;

import java.lang.reflect.Method;

import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;

public class WilflyServletDispatching implements InstanceMethodsAroundInterceptor {

	@Override
	public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
			MethodInterceptResult result) throws Throwable {
		HttpServerExchange hse = (HttpServerExchange) allArguments[0];
		String requestURL = hse.getRequestURL();
		String formatURIPath = hse.getRequestPath(); // contexte/myPage.html
		String httpMethod = hse.getRequestMethod().toString();
		final HeaderMap headers = hse.getRequestHeaders();

		ContextCarrier contextCarrier = new ContextCarrier();
		CarrierItem items = contextCarrier.items();
		while (items.hasNext()) {
			items = items.next();
			items.setHeadValue(headers.getFirst(items.getHeadKey()));
		}

		AbstractSpan span = ContextManager.createEntrySpan(formatURIPath, contextCarrier);
		Tags.URL.set(span, requestURL);
		Tags.HTTP.METHOD.set(span, httpMethod);
		span.setComponent(WildflySharedInfos.WILDFLY_SERVER);
		SpanLayer.asHttp(span);
		
	}

	@Override
	public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
			Object ret) throws Throwable {

		if (ContextManager.isActive()) {
			ContextManager.stopSpan();
		}
		return ret;
	}

	@Override
	public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
			Class<?>[] argumentsTypes, Throwable t) {
		ContextManager.activeSpan().log(t);
	}
}
