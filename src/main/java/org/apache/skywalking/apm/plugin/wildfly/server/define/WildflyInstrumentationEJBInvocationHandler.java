package org.apache.skywalking.apm.plugin.wildfly.server.define;

import static net.bytebuddy.matcher.ElementMatchers.nameContainsIgnoreCase;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassStaticMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.NameMatch;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class WildflyInstrumentationEJBInvocationHandler extends ClassStaticMethodsEnhancePluginDefine {
//public class WildflyInstrumentationLocalEjb extends ClassEnhancePluginDefine {
	 
	private static final String ENHANCE_CLASS = "org.jboss.ejb.client.EJBInvocationHandler";
    private static final String ENHANCE_METHOD = "sendRequestWithPossibleRetries";
    private static final String INTERCEPTOR_CLASS = "org.apache.skywalking.apm.plugin.wildfly.server.WilflyInterceptorEJBInvocationHandler";

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    
    @Override
    public StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return new StaticMethodsInterceptPoint[] {
            new StaticMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return nameContainsIgnoreCase(ENHANCE_METHOD);
                }

                @Override
                public String getMethodsInterceptor() {
                    return INTERCEPTOR_CLASS;
                }

                @Override
                public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }


    @Override
    protected ClassMatch enhanceClass() {
        return NameMatch.byName(ENHANCE_CLASS);
    }

//	@Override
//	public StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
//		return new StaticMethodsInterceptPoint[0];
//	}
//	
//	@Override public boolean isBootstrapInstrumentation() {
//        return true;
//    }
}
