
import java.io.FileWriter
import java.io.File

import cbt._

import scala.io.Source


class Build(val context: cbt.Context) extends PackageJars {
  override def defaultVersion = "0.6.1"

  override def name = "play-json-extensions"

  override def groupId = "org.cvogt"

  override def artifactId = "play-json-extensions"

  override def dependencies =
    super.dependencies ++
      Resolver(mavenCentral).bind(
        ScalaDependency("com.typesafe.play", "play-json", "2.4.4"),
        MavenDependency("joda-time", "joda-time", "2.9.2"),
        "com.lihaoyi" %% "upickle" % "0.3.9"
      )

  override def compile = {
    println("Compiling...")
    super.compile
  }


  // @TODO add only the top level dependencies
  def generateIdeaProject {
    val moduleDir = "$MODULE_DIR$"
    val dependencies = for {
      depJarFile <- this.dependencies.
        flatMap(dep => dep.dependencies).
        map(x => x.exportedClasspath.files).
        flatMap(x => x.toList)
      dep =
      s"""<orderEntry type="module-library">
          |      <library>
          |        <CLASSES>
          |            <root url="jar://$moduleDir/../cbt/${depJarFile.getPath.split("/cbt")(1)}!/" />
          |          </CLASSES>
          |          <JAVADOC />
          |          <SOURCES />
          |       </library>
          |</orderEntry>""".stripMargin
    } yield dep
    println(s"creating project for ${name}")
    val imlFile = new File(this.getClass.getResource("/").getPath + s"../../../../${name}.iml")
    if (!imlFile.exists()) {
      println("creating a new file")
      imlFile.createNewFile()
    } else {
      println("file exists")
    }
    val fw = new FileWriter(imlFile.getPath, false)
    fw.write(
      IdeaScalaProjectTemplate.templateWithCBTSources(dependencies.mkString("\n")))
    fw.close()
  }
}


object IdeaScalaProjectTemplate {

  // @TODO inject from the Build.scala
  // scala version
  // path of cbt relative to module dir
  val templateWithCBTSources: (String) => String = (dependencies: String) =>
    """<?xml version="1.0" encoding="UTF-8"?>
      |<module type="JAVA_MODULE" version="4">
      |  <component name="NewModuleRootManager" inherit-compiler-output="true">
      |    <exclude-output />
      |    <content url="file://$MODULE_DIR$/../cbt/compatibility">
      |      <sourceFolder url="file://$MODULE_DIR$/../cbt/compatibility" isTestSource="false" />
      |      <excludeFolder url="file://$MODULE_DIR$/../cbt/compatibility/target/scala-2.11" />
      |    </content>
      |    <content url="file://$MODULE_DIR$/../cbt/nailgun_launcher">
      |      <sourceFolder url="file://$MODULE_DIR$/../cbt/nailgun_launcher" isTestSource="false" />
      |      <excludeFolder url="file://$MODULE_DIR$/../cbt/nailgun_launcher/target" />
      |    </content>
      |    <content url="file://$MODULE_DIR$/../cbt/stage1">
      |      <sourceFolder url="file://$MODULE_DIR$/../cbt/stage1" isTestSource="false" />
      |      <excludeFolder url="file://$MODULE_DIR$/../cbt/stage1/target/scala-2.11" />
      |    </content>
      |    <content url="file://$MODULE_DIR$/../cbt/stage2">
      |      <sourceFolder url="file://$MODULE_DIR$/../cbt/stage2" isTestSource="false" />
      |      <excludeFolder url="file://$MODULE_DIR$/../cbt/stage2/target/scala-2.11" />
      |    </content>
      |    <content url="file://$MODULE_DIR$">
      |      <sourceFolder url="file://$MODULE_DIR$/src/main/scala/main" isTestSource="false" />
      |      <sourceFolder url="file://$MODULE_DIR$/src/test/scala" isTestSource="false" />
      |    </content>
      |    <orderEntry type="inheritedJdk" />
      |    <orderEntry type="sourceFolder" forTests="false" />
      |    <orderEntry type="library" name="scala-sdk-2.11.7" level="application" /> |
      |    """.stripMargin + dependencies +
      """  </component>
        |</module>""".stripMargin

}



