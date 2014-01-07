import de.tototec.sbuild._

@version("0.7.1")
@classpath(
  "mvn:org.apache.ant:ant:1.8.4",
  "target/org.sbuild.plugins.mavendeploy-0.0.9000.jar"
)
class SBuild(implicit _project: Project) {

  val namespace = "org.sbuild.plugins.mavendeploy"
  val version = "0.0.9000"
  val url = "https://github.com/SBuild-org/sbuild-maven-deploy"

  import org.sbuild.plugins.mavendeploy._

  Plugin[MavenDeploy] configure { _.copy(
    groupId = "org.sbuild",
    artifactId = namespace,
    version = version,
    artifactName = Some("SBuild Maven Deploy Plugin."),
    description = Some("SBuild Plugin to deploy to Remote Maven Repositories such as Nexus."),
    url = Some(url),
    repository = Repository.SonatypeOss,
    scm = Option(Scm(url = url, connection = url)),
    developers = Seq(Developer(id = "TobiasRoeser", name = "Tobias Roeser", email = "le.petit.fou@web.de")),
    licenses = Seq(License.Apache20),
    gpg = true,
    files = Map(
      "jar" -> s"target/${namespace}-${version}.jar",
      "sources" -> s"target/${namespace}-${version}-sources.jar",
      "javadoc" -> "target/fake.jar"
    )
  )}

  Target("target/fake.jar") dependsOn "LICENSE.txt" exec { ctx: TargetContext =>
    import de.tototec.sbuild.ant._
    tasks.AntJar(destFile = ctx.targetFile.get, fileSet = AntFileSet(file = "LICENSE.txt".files.head))
  }

}
