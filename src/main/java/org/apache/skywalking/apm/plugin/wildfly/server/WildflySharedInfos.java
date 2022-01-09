package org.apache.skywalking.apm.plugin.wildfly.server;

import java.lang.reflect.Method;

import org.apache.skywalking.apm.network.trace.component.OfficialComponent;
import org.jboss.ejb.client.EJBLocator;

public class WildflySharedInfos {
	public static final OfficialComponent WILDFLY_SERVER = new OfficialComponent(1001, "Wildfly");
//	public static final OfficialComponent WILDFLY_SERVER = ComponentsDefine.UNDERTOW;
	
	public static String buildFullEjbNameAsURL(String ejbName, Method m) {
		String fullEjbNameAsURL = ejbName + "#" + m.getName();
		return fullEjbNameAsURL;
	}

	public static  String buildEjbNameFromLocator(@SuppressWarnings("rawtypes") EJBLocator locator) {
		String fullEjbNameAsURL = "remoteEjb://" + locator.getAppName() + "/" + locator.getModuleName() + "/" +locator.getBeanName();
		return fullEjbNameAsURL;
	}
}
