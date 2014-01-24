import de.tototec.sbuild._
import de.tototec.sbuild.ant._
import de.tototec.sbuild.ant.tasks._

@version("0.7.1")
@classpath(
  "mvn:org.apache.ant:ant:1.8.4",
  "target/org.sbuild.plugins.mavendeploy-0.1.0.jar"
)
class SBuild(implicit _project: Project) {

  val namespace = "org.sbuild.plugins.mavendeploy"
  val version = "0.1.0"
  val url = "https://github.com/SBuild-org/sbuild-maven-deploy"
  val sourcesJar = s"target/${namespace}-${version}-sources.jar"
  val sourcesDir = "src/main/scala"

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

  Target(sourcesJar) dependsOn s"scan:${sourcesDir}" ~ "LICENSE.txt" exec { ctx: TargetContext =>
    AntZip(destFile = ctx.targetFile.get, fileSets = Seq(
      AntFileSet(dir = Path(sourcesDir)),
      AntFileSet(file = Path("LICENSE.txt"))
    ))
  }

  Target("target/fake.jar") dependsOn "LICENSE.txt" exec { ctx: TargetContext =>
    import de.tototec.sbuild.ant._
    tasks.AntJar(destFile = ctx.targetFile.get, fileSet = AntFileSet(file = "LICENSE.txt".files.head))
  }

}
