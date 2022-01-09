package org.apache.skywalking.apm.plugin.wildfly.server;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.StaticMethodsAroundInterceptor;
import org.jboss.ejb.client.EJBClientInvocationContext;
import org.jboss.ejb.client.EJBLocator;

public class WilflyInterceptorEJBInvocationHandler implements StaticMethodsAroundInterceptor {

	@Override
	public void beforeMethod(@SuppressWarnings("rawtypes") Class clazz, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
			MethodInterceptResult result) {

		// Get content of parameter
		EJBClientInvocationContext invocationContext = (EJBClientInvocationContext) allArguments[0];
		EJBLocator<?> locator = invocationContext.getLocator();
		Method ejbMethodInvoked = invocationContext.getInvokedMethod();
		String endpointName = ejbMethodInvoked.getName(); // getEJBClientConfiguration().getEndpointName();
		Map<String, Object> contextData = invocationContext.getContextData();
		String ejbName = WildflySharedInfos.buildEjbNameFromLocator(locator);
		String fullEjbNameAsURL = WildflySharedInfos.buildFullEjbNameAsURL(ejbName, ejbMethodInvoked);

		ContextCarrier contextCarrier = new ContextCarrier();
		AbstractSpan span = ContextManager.createExitSpan(endpointName, contextCarrier, ejbName);
		span.setComponent(WildflySharedInfos.WILDFLY_SERVER);
		Tags.URL.set(span, fullEjbNameAsURL);
		SpanLayer.asRPCFramework(span);

		CarrierItem next = contextCarrier.items();
		while (next.hasNext()) {
			next = next.next();
			contextData.put(next.getHeadKey(), next.getHeadValue());
		}
	}

	@Override
	public Object afterMethod(@SuppressWarnings("rawtypes") Class clazz, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
			Object ret) {
		ContextManager.stopSpan();
		return ret;
	}

	@Override
	public void handleMethodException(@SuppressWarnings("rawtypes") Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes,
			Throwable t) {
		ContextManager.activeSpan().log(t);

	}
}
