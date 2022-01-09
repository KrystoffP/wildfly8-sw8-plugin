# wildfly8-sw8-plugin
This is a Wilfly 8 plugin for Apache SkyWalking APM (SW8)


# Compilation
To get your plugin compiled:
  mvn install -Dcheckstyle.skip
  
Checkstyle is activated because of the parent pom - as this project is not in the SkyWalking src structure -> checkstyle conf is not found --> so we disable it

  
# Usage
Copy "wildfly8-sw8-plugin-8.8.0.jar" in the plugin directory of your SW8 Agent


# WARNING
This plugin has not been deployed yet in production -> please use carefully!.