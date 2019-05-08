# Nashville Unsummit 2019 - Demo Plugin

This repository contains the source code for a plugin that was written and
demoed at the Nashville Unsummit in 2019 (at the session entitled _AM Plugins_).

There are 4 commits in the repository that cover the changes that were made:
* `Initial Node`: In this commit we have a simple node that calls out to a
  webservice to find whether it has any output to display.
* `Add a service...`: In this commit we abstract away the configuration of
  the external webservice so that it doesn't have to be configured for every
  instance of the node.
* `Add in a cache...`: Now we add a cache so that we do not keep calling the
  external webservice for every authentication, but rather cache responses
  for an hour.
* `Now let's use the secrets API...`: If we need to store secret values for
  our plugin (in this case a secret API Key), we show in this commit how to
  integrate with the Secrets API that was added in the ForgeRock stack in
  version 6.5.

The following files are included in this repository:
* [`messages-app`](./messages-app): An Express NodeJS application that
  implements the greetings webservice.
* [`pom.xml`](pom.xml): Declare the maven project with dependencies from AM.
* [`secrets/am.plugins.nashville.greetings.api`](secrets/am.plugins.nashville.greetings.api):
  A sample API key secret.
* [`src/main/java/org/forgerock/am/plugins/nashville/NashvilleNode.java`](./src/main/java/org/forgerock/am/plugins/nashville/NashvilleNode.java):
  The authentication node.
* [`src/main/java/org/forgerock/am/plugins/nashville/NashvillePlugin.java`](./src/main/java/org/forgerock/am/plugins/nashville/NashvillePlugin.java):
  The plugin class, which installs the node and the service.
* [`src/main/java/org/forgerock/am/plugins/nashville/Greetings.java`](./src/main/java/org/forgerock/am/plugins/nashville/Greetings.java):
  A class for handling interactions with the greetings webservice.
* [`src/main/java/org/forgerock/am/plugins/nashville/NashvilleSecretIdProvider.java`](./src/main/java/org/forgerock/am/plugins/nashville/NashvilleSecretIdProvider.java):
  The declaration of the secret ID that will be registered in the Secrets
  API. 
* [`src/main/java/org/forgerock/am/plugins/nashville/GreetingsService.java`](./src/main/java/org/forgerock/am/plugins/nashville/GreetingsService.java):
  A realm-level configuration service for the greetings webservice.  
* [`src/main/resources/META-INF/services/org.forgerock.openam.secrets.SecretIdProvider`](./src/main/resources/META-INF/services/org.forgerock.openam.secrets.SecretIdProvider):
  [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html)
  registration of the `NashvilleSecretIdProvider` class.
* [`src/main/resources/META-INF/services/org.forgerock.openam.plugins.AmPlugin`](./src/main/resources/META-INF/services/org.forgerock.openam.plugins.AmPlugin):
  [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html)
  registration of the `NashvillePlugin` class.

## To build/test the plugin

In order to see the plugin running following the following steps:

* Set up `local.example.com` as an host alias for `127.0.0.1`
* [Install AM6.5+](https://backstage.forgerock.com/docs/am/6.5/install-guide/)
* Compile and package the plugin: `mvn clean package`
* Copy the plugin JAR file (from `target/nashville-plugin-1.0.0-SNAPSHOT.jar`) to the `WEB-INF/lib` of the deployed AM application
* Restart AM to install the plugin
* Configure the plugin:
   * Install the secrets:
      * Navigate to `Root Realm > Secret Stores > Add Secret Store`
      * Add the API Key secret directory:
         * Secret Store ID: `Nashville`
         * Store Type: `File System Secret Volumes`
         * Directory: [`[plugin-source-directory]/secrets`](./secrets)
      * Click `Create`
      * Set `File format` to `Plain text`
      * Click `Save Changes`
   * Configure the greetings webservice:
      * Navigate to `Root Realm > Services > Add a Service`
      * Select `Greetings` from the drop down and click `Create`
      * Click `Save Changes`
   * Adapt the `Example` tree:
      * Navigate to `Root Realm > Authentication > Trees > Example`
      * Drag a `Greeting` node onto the canvas
      * Wire the `True` outcome of the `Data Store Decision` to the 
        `Greeting` input
      * Wire the `Greeting` output to `Success`
   * Logout
* Test the node:
   * Start the webservice by running:
     [`messages-app/bin/www`](./messages-app/bin/www)
   * Login at: `[AM_URL]/XUI/#login/&service=Example`

## License

This source code is licensed under the 
[Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0).
It can be used to guide you through taking a simple node and turning it into
a more fully-featured plugin.
