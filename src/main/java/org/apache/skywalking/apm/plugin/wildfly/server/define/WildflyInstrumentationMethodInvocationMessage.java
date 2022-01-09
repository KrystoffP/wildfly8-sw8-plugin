package org.apache.skywalking.apm.plugin.wildfly.server.define;

import static net.bytebuddy.matcher.ElementMatchers.nameContainsIgnoreCase;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.NameMatch;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class WildflyInstrumentationMethodInvocationMessage extends ClassInstanceMethodsEnhancePluginDefine {
//public class WildflyInstrumentationLocalEjb extends ClassEnhancePluginDefine {
	 
	private static final String ENHANCE_CLASS = "org.jboss.as.ejb3.remote.protocol.versionone.MethodInvocationMessageHandler";
    private static final String ENHANCE_METHOD = "invokeMethod";
    private static final String INTERCEPTOR_CLASS = "org.apache.skywalking.apm.plugin.wildfly.server.WilflyMethodInvocationMessage";

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
            new InstanceMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return  nameContainsIgnoreCase(ENHANCE_METHOD);
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
