package org.sbuild.plugins.mavendeploy

import de.tototec.sbuild.Project
import de.tototec.sbuild.Path
import java.io.File
import de.tototec.sbuild.TargetRef

/**
 * Plugin instance class used for configuration of the SBuild Plugin Plugin.
 */
class MavenDeploy(val name: String)(implicit project: Project) {

  //  var : String = if (name == "") "maven-deploy" else s"maven-deploy-$name"

  /**
   * Use an existing Maven `settings.xml` file.
   * If `[[scala.None$]]`, a Maven settings file will be generated locally.
   */
  var mavenSettings: Option[File] = None

  var mavenHome: Option[File] = None

  var gpg: Boolean = false

  /**
   * The path of the local repository used by Maven.
   * This property will only be used if `[[MavenDeploy#mavenSettings]]` is set to `None`, else the the local repository configured in the `setings.xml` file will be used.
   */
  var localRepoDir: Option[File] = None

  var groupId: String = _

  var artifactId: String = _

  var version: String = _

  def artifact: String = s"${Option(groupId).mkString}:${Option(artifactId).mkString}:${Option(version).mkString}"
  def artifact_=(artifact: String): Unit = {
    artifact.split(":", 3) match {
      case Array(g, a, v) =>
        groupId = g
        artifactId = a
        version = v
      case _ => throw new IllegalArgumentException("Unsupported pattern used in property 'artifact'. Please use pattern: 'groupId:artifactId:version'.")
    }
  }

  var artifactName: Option[String] = None

  var description: Option[String] = None

  var url: Option[String] = None

  var licenses: Seq[License] = Seq()

  var scm: Option[Scm] = None

  var developers: Seq[Developer] = Seq()

  /**
   * A map of files, by Maven classifier. This map most typically contains a `jar` entry and somtimes also a `sources` and a `javadoc` entry.
   */
  var files: Map[String, TargetRef] = Map()

  var repository: Repository = _

}

object License {
  val Apache20 = License(
    name = "The Apache Software License, Version 2.0",
    url = "http://www.apache.org/licenses/LICENSE-2.0.txt",
    distribution = "repo"
  )
}

case class License(name: String, url: String, distribution: String = "repo")

case class Scm(url: String, connection: String)

case class Developer(id: String, name: String, email: String)

object Repository {
  val SonatypeOss = Repository(
    id = "sonatype-oss-nexus-staging",
    url = "http://oss.sonatype.org/service/local/staging/deploy/maven2/",
    needsAuth = true
  )
}

case class Repository(id: String,
                      url: String,
                      needsAuth: Boolean = false,
                      username: Option[String] = None,
                      password: Option[String] = None)