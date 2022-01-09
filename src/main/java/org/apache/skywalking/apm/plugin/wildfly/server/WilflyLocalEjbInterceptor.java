package org.apache.skywalking.apm.plugin.wildfly.server;

import java.lang.reflect.Method;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.jboss.ejb.client.EJBClientInvocationContext;
import org.jboss.ejb.client.EJBLocator;

public class WilflyLocalEjbInterceptor implements InstanceMethodsAroundInterceptor {

	@Override
	public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
			MethodInterceptResult result) throws Throwable {
		EJBClientInvocationContext ejbIC = (EJBClientInvocationContext) allArguments[0];
		@SuppressWarnings("rawtypes")
		EJBLocator locator = ejbIC.getLocator();

		String entryPoint = locator.getBeanName() + "#" + ejbIC.getInvokedMethod().getName();
		String fullEjbNameAsURL = "localEjb://" + locator.getAppName() + "/" + locator.getModuleName() + "/"
				+ entryPoint;

		AbstractSpan span = ContextManager.createLocalSpan(entryPoint);
		Tags.URL.set(span, fullEjbNameAsURL);
//        Tags.HTTP.METHOD.set(span, hse.getMgetMethod());
		span.setComponent(WildflySharedInfos.WILDFLY_SERVER);
		SpanLayer.asRPCFramework(span);
	}

	@Override
	public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
			Object ret) throws Throwable {
		ContextManager.stopSpan();
		return ret;
	}

	@Override
	public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
			Class<?>[] argumentsTypes, Throwable t) {
		ContextManager.activeSpan().log(t);
	}
}
