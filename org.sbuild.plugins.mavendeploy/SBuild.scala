import de.tototec.sbuild._
import de.tototec.sbuild.ant._
import de.tototec.sbuild.ant.tasks._

import de.tototec.sbuild._

@version("0.7.0")
@classpath("../../sbuild-plugin/org.sbuild.plugins.sbuildplugin/target/org.sbuild.plugins.sbuildplugin-0.0.9000.jar")
class SBuild(implicit _project: Project) {

  Plugin[org.sbuild.plugins.sbuildplugin.SBuildPlugin] configure { c =>
    // the version of SBuild this plugin is compatible to
    c.sbuildVersion = "0.7.0"

    // the plugin API
    c.pluginClass = "org.sbuild.plugins.mavendeploy.MavenDeploy"
    // the version of this plugin
    c.pluginVersion = "0.0.9000"
    // the plugin factory class, which extends trait de.tototec.sbuild.Plugin
    c.pluginFactoryClass = "org.sbuild.plugins.mavendeploy.MavenDeployPlugin"
  }

}
