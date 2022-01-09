package org.apache.skywalking.apm.plugin.wildfly.server;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.jboss.ejb.client.EJBLocator;

public class WilflyMethodInvocationMessage implements InstanceMethodsAroundInterceptor {

	

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
        MethodInterceptResult result) throws Throwable {

    	Method m = (Method)allArguments[2];
    	@SuppressWarnings("rawtypes")
		EJBLocator locator = (EJBLocator)allArguments[4];
    	//Contient les donéees du IncontextData
    	@SuppressWarnings("unchecked")
		Map<String, Object> attachments = (Map<String, Object>)allArguments[5];

    	ContextCarrier contextCarrier = new ContextCarrier();

        CarrierItem  next = contextCarrier.items();
        while (next.hasNext()) {
            next = next.next();
            next.setHeadValue((String)attachments.get(next.getHeadKey()));
        }

        String ejbName = WildflySharedInfos.buildEjbNameFromLocator(locator);
   		String fullEjbNameAsURL = WildflySharedInfos.buildFullEjbNameAsURL(ejbName, m); 
//        	
        AbstractSpan span =  ContextManager.createEntrySpan(m.getName(), contextCarrier);
        Tags.URL.set(span, fullEjbNameAsURL);
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
