# xrate plugins
xrate can support other third-party currency conversion / exchange rate services through plugins.

## Plugin installation
To use a plugin, put it in the `xrate/plugins` directory.
Instead of the default converter, xrate will use the plugin for conversions.
If your plugin also needs an API key or other authentication information, specify it in
`xrate/config/xrate.properties` as `xrate.plugin.auth=YOUR-API-KEY`.

Module `plugin-exchangerateapi` contains such a plugin:
it adds the capability to convert currencies using [ExchangeRate-API](https://www.exchangerate-api.com/).
Look for `plugin-exchangerateapi-<version>.jar` in the module's `target` directory, and place it in `xrate/plugins`.
You have to get a free API key from ExchangeRate-API, if you want to use this plugin.

## Plugin development
Plugin development is based on Java's Service Provider Interface mechanism.

To create a plugin you have to do the following:
1. Implement the `CurrencyConverter` interface from module `xrate-api`.
2. Create a service mapping file in your project in the `META-INF/services` directory.
   The name of the file has to be `nemethi.xrate.api.CurrencyConverter`.
3. The service mapping file has to consist of the fully qualified name of your class, that implements the interface.
4. Package your plugin, then place it and all its dependencies in your xrate installation's `plugins` directory.
   You don't have to include `xrate-api-<version>.jar`, as it will be already on the classpath.

Note, that you do not have to package your plugin into a single JAR file or other distributable.
Everything in the `plugins` directory will be added __non-recursively__ to the application's classpath.

### Example module
Module `plugin-exchangerateapi` serves as a fully functional plugin
and also as an example on how to develop one.

It follows all the steps mentioned before:
1. Class `ExchangeRateApiConverter` implements the `CurrencyConverter` interface.
2. In the `resources` directory, there is a service mapping file: `META-INF/services/nemethi.xrate.api.CurrencyConverter`.
3. The file's content is the fully qualified name of the implementing class: 
   `nemethi.xrate.plugin.ExchangeRateApiConverter`.
4. The module's files and dependencies are packaged into a single JAR file by the
   [Maven Shade Plugin](https://maven.apache.org/plugins/maven-shade-plugin/).
   The plugin is configured to exclude `xrate-api` from the resulting JAR file.
