package org.apache.skywalking.apm.plugin.wildfly.server;

import java.lang.reflect.Method;

import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.jboss.as.ejb3.timerservice.TimedObjectInvokerImpl;

public class WilflyTimedObjectInvokerImpl implements InstanceMethodsAroundInterceptor {

	

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
        MethodInterceptResult result) throws Throwable {

    	TimedObjectInvokerImpl timedObjectInvokerImpl =  (TimedObjectInvokerImpl)objInst;
    	
    	String ejbComponentAsURL = timedObjectInvokerImpl.getTimedObjectId(); 
    	//Singleton class name as method
    	String methodName = timedObjectInvokerImpl.getEjbComponent().getValue().getComponentName();
    	
    	ContextCarrier contextCarrier = new ContextCarrier();
          	
        AbstractSpan span =  ContextManager.createEntrySpan(methodName, contextCarrier);
        Tags.URL.set(span, ejbComponentAsURL);
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
