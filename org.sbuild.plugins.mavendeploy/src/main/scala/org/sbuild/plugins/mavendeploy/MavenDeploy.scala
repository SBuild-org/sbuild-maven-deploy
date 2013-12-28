package org.sbuild.plugins.mavendeploy

import de.tototec.sbuild.Project
import de.tototec.sbuild.Path
import java.io.File
import de.tototec.sbuild.TargetRef

/**
 * Plugin instance class used for configuration of the SBuild Plugin Plugin.
 *
 * @param mavenSerttings Use an existing Maven `settings.xml` file.
 *   If `[[scala.None$]]`, a Maven settings file will be generated locally.
 *
 * @param localRepoDir The path of the local repository used by Maven.
 *   This property will only be used if `[[MavenDeploy#mavenSettings]]` is set to `None`, else the the local repository configured in the `setings.xml` file will be used.
 *
 * @param files A map of files, by Maven classifier.
 *   This map most typically contains a `jar` entry and somtimes also a `sources` and a `javadoc` entry.
 *
 */
case class MavenDeploy(
    groupId: String = null,
    artifactId: String = null,
    version: String = null,
    artifactName: Option[String] = None,
    description: Option[String] = None,
    mavenSettings: Option[File] = None,
    mavenHome: Option[File] = None,
    gpg: Boolean = false,
    localRepoDir: Option[File] = None,
    url: Option[String] = None,
    licenses: Seq[License] = Seq(),
    scm: Option[Scm] = None,
    developers: Seq[Developer] = Seq(),
    files: Map[String, TargetRef] = Map(),
    repository: Repository = null) {

  //  def artifact: String = s"${Option(groupId).mkString}:${Option(artifactId).mkString}:${Option(version).mkString}"
  //  def artifact_=(artifact: String): Unit = {
  //    artifact.split(":", 3) match {
  //      case Array(g, a, v) =>
  //        groupId = g
  //        artifactId = a
  //        version = v
  //      case _ => throw new IllegalArgumentException("Unsupported pattern used in property 'artifact'. Please use pattern: 'groupId:artifactId:version'.")
  //    }
  //  }

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
    needsAuth = true,
    // Sonatype's OSS Nexus has lots of requirements
    validator = Some { d =>
      import d._
      Seq(
        Option(groupId).toRight("'groupId' must be specified."),
        Option(artifactId).toRight("'artifactId' must be specified"),
        artifactName.toRight("'artifactName'  must be specified"),
        description.toRight("'description' must be specified"),
        if (gpg) Right(gpg) else Left("'gpg' must be set to true"),
        url.toRight("'url' must be specified"),
        if (licenses.isEmpty) Left("at least one license must be specified") else Right(licenses),
        scm.toRight("'scm' must be specified"),
        if (developers.isEmpty) Left("At least one developer must be specified") else Right(developers),
        files.keySet.find(k => k == "jar" || k == "").toRight("A 'jar' file must be specified"),
        files.keySet.find(_ == "sources").toRight("A 'sources' file must be specified"),
        files.keySet.find(_ == "javadoc").toRight("A 'javaodc' file must be specified")
      ).collect { case Left(msg) => msg }
    }
  )
}

case class Repository(id: String,
                      url: String,
                      needsAuth: Boolean = false,
                      username: Option[String] = None,
                      password: Option[String] = None,
                      validator: Option[MavenDeploy => Seq[String]] = None)