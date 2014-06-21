import de.tototec.sbuild._
import de.tototec.sbuild.ant._
import de.tototec.sbuild.ant.tasks._

import de.tototec.sbuild._

@version("0.7.1")
@classpath("mvn:org.sbuild:org.sbuild.plugins.sbuildplugin:0.2.2")
class SBuild(implicit _project: Project) {

  Plugin[org.sbuild.plugins.sbuildplugin.SBuildPlugin] configure { _.copy(
    sbuildVersion = "0.7.1",
    pluginClass = "org.sbuild.plugins.mavendeploy.MavenDeploy",
    pluginVersion = "0.1.0.9000"
  )}

}
