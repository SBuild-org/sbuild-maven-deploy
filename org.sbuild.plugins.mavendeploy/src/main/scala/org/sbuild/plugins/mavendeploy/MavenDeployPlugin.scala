package org.sbuild.plugins.mavendeploy

import de.tototec.sbuild._
import de.tototec.sbuild.addons.support.ForkSupport
import java.io.PrintWriter

class MavenDeployPlugin(implicit project: Project) extends Plugin[MavenDeploy] {
  override def create(name: String): MavenDeploy = new MavenDeploy(name)
  override def applyToProject(instances: Seq[(String, MavenDeploy)]): Unit =
    instances foreach {
      case (name, deploy) =>
        // Some checks

        //        if (deploy.targetName.isEmpty) throw new ProjectConfigurationException(s"The 'targetName' property must not be empty for plugin ${classOf[MavenDeploy].getName}.")
        if (deploy.groupId == null) throw new ProjectConfigurationException(s"The 'groupId' property was not set for plugin ${classOf[MavenDeploy].getName}.")
        if (deploy.artifactId == null) throw new ProjectConfigurationException(s"The 'artifactId' property was not set for plugin ${classOf[MavenDeploy].getName}.")
        if (deploy.version == null) throw new ProjectConfigurationException(s"The 'artifactId' property was not set for plugin ${classOf[MavenDeploy].getName}.")
        if (deploy.repository == null) throw new ProjectConfigurationException(s"The 'repository' property was not set for plugin ${classOf[MavenDeploy].getName}.")

        val targetNamePart = if (name == "") "maven-deploy" else s"maven-deploy-$name"
        val workDir = Path("target") / targetNamePart
        // workDir.mkdirs

        val cleanT = Target(s"phony:clean-${targetNamePart}") exec {
          workDir.deleteRecursive
        }

        Target("phony:clean") dependsOn cleanT

        val isWindows = System.getProperty("os.name").toLowerCase.indexOf("win") >= 0
        val mvnExe = if (isWindows) "mvn.bat" else "mvn"

        val mvn = deploy.mavenHome match {
          case Some(home) => (home / "bin" / mvnExe).getPath
          case None => mvnExe
        }

        // TODO: download and provide maven

        val mvnSettings: TargetRef = deploy.mavenSettings.map(TargetRef(_)).getOrElse {
          Target(workDir / "maven-settings.xml") dependsOn project.projectFile exec { ctx: TargetContext =>

            val repo = deploy.repository

            val (username, password) =
              if (repo.needsAuth) {
                if (repo.username.isEmpty || repo.password.isEmpty)
                  println(s"Server ${deploy.repository.url} needs authentication:")

                val username = repo.username match {
                  case Some(x) => x
                  case None =>
                    print("Username: ")
                    readLine()
                }
                val password = repo.password match {
                  case Some(x) => x
                  case None =>
                    print("Password: ")
                    readLine()
                }

                (username, password)
              } else ("", "")

            val settings = s"""<settings>
  <servers>
    <server>
      <id>${deploy.repository.id}</id>
      ${
              if (deploy.repository.needsAuth) s"<username>${username}</username>\n      <password>${password}</password>"
              else ""
            }
    </server>
  </servers>
</settings>"""

            val file = ctx.targetFile.get
            file.getParentFile.mkdirs

            val pw = new PrintWriter(file)
            try { pw.print(settings) }
            finally { pw.close() }
          }
        }

        val pomFile = workDir / "pom.xml"

        // TODO: dependsOn on this project, somehow 
        val pomT = Target(pomFile) dependsOn project.projectFile exec { ctx: TargetContext =>

          val pom = s"""<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>${deploy.groupId}</groupId>
  <artifactId>${deploy.artifactId}</artifactId>
  <packaging>jar</packaging>
  <version>${deploy.version}</version>
  ${deploy.artifactName.map(n => s"<name>$n</name>").mkString}
  ${deploy.description.map(d => s"<name>$d</name>").mkString}
  ${deploy.url.map(u => s"<url>$u</url>").mkString}
  ${
            deploy.licenses.map { l => s"<license><name>${l.name}</name><url>${l.url}</url><distribution>${l.distribution}</distribution></license>" }.
              mkString("<licenses>\n    ", "\n    ", "\n  </licenses>")
          }
  ${deploy.scm.map { s => s"<scm><url>${s.url}</url><connection>${s.connection}</connection></scm>" }.getOrElse("")}
  ${
            deploy.developers.map { d => s"<developer><id>${d.id}</id><name>${d.name}</name><email>${d.email}</email></developer>" }.
              mkString("<developers>\n    ", "\n    ", "\n  </developers>")
          }
</project>"""

          val file = ctx.targetFile.get
          file.getParentFile.mkdirs

          val pw = new PrintWriter(file)
          try { pw.print(pom) }
          finally { pw.close() }

        }

        val deployTs = deploy.files.map {
          case (cl, file) =>
            val classifier = if (cl == "") "jar" else cl
            Target(s"phony:${targetNamePart}-${classifier}") dependsOn pomT ~ mvnSettings ~ file exec { ctx: TargetContext =>

              if (file.files.size != 1) {
                throw new RuntimeException(s"Not exactly one file given for classifier '${classifier}'.")
              }

              var cmd = Array(
                mvn,
                "-s", mvnSettings.files.head.getPath,
                if (deploy.gpg) "gpg:sign-and-deploy-file" else "deploy-file",
                s"-DpomFile=${pomFile.getPath}",
                s"-Dfile=${file.files.head}",
                s"-Durl=${deploy.repository.url}",
                s"-DrepositoryId=${deploy.repository.id}"
              )

              if (classifier != "jar") cmd ++= Array(s"-Dclassifier=${classifier}")

              ForkSupport.runAndWait(
                directory = workDir,
                command = cmd,
                interactive = false,
                failOnError = true
              )
            }
        }

        Target(s"phony:${targetNamePart}") dependsOn deployTs.toSeq.map(TargetRef(_))

    }

}
