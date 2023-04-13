import org.typelevel.sbt.gha

ThisBuild / tlBaseVersion := "0.24" // your current series x.y

ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("rossabaker", "Ross A. Baker")
)

// publish website from this branch
ThisBuild / tlSitePublishBranch := Some("main")

val Scala213 = "2.13.10"
ThisBuild / crossScalaVersions := Seq(Scala213, "2.12.17", "3.2.2")
ThisBuild / scalaVersion := Scala213 // the default Scala
ThisBuild / githubWorkflowJavaVersions ~= {
  // Jetty 10 bumps the requirement to Java 11
  _.filter { case JavaSpec(_, major) => major.toInt >= 11 }
}

ThisBuild / resolvers +=
  "s01 snapshots".at("https://s01.oss.sonatype.org/content/repositories/snapshots/")

lazy val root = project
  .in(file("."))
  .enablePlugins(NoPublishPlugin)
  .aggregate(jettyServer, jettyClient)

val jettyVersion = "10.0.15"
val http4sVersion = "0.23.18"
val http4sServletVersion = "0.24.0-M2"
val munitCatsEffectVersion = "1.0.7"
val slf4jVersion = "1.7.25"

lazy val jettyServer = project
  .in(file("jetty-server"))
  .settings(
    name := "http4s-jetty-server",
    description := "Jetty implementation for http4s servers",
    startYear := Some(2014),
    libraryDependencies ++= Seq(
      "org.eclipse.jetty" % "jetty-client" % jettyVersion % Test,
      "org.eclipse.jetty" % "jetty-servlet" % jettyVersion,
      "org.eclipse.jetty" % "jetty-util" % jettyVersion,
      "org.eclipse.jetty.http2" % "http2-server" % jettyVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion % Test,
      "org.http4s" %% "http4s-servlet" % http4sServletVersion,
      "org.typelevel" %% "munit-cats-effect-3" % munitCatsEffectVersion % Test,
    ),
    jettyApiMappings,
    javaApiMappings,
  )

lazy val examples = project
  .in(file("examples"))
  .enablePlugins(NoPublishPlugin)
  .settings(
    name := "http4s-jetty-examples",
    description := "Example of http4s server on JEtty",
    startYear := Some(2014),
    fork := true,
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-simple" % slf4jVersion % Runtime
    ),
  )
  .dependsOn(jettyServer)

lazy val jettyClient = project
  .in(file("jetty-client"))
  .settings(
    name := "http4s-jetty-client",
    description := "jetty implementation for http4s clients",
    startYear := Some(2018),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-client" % http4sVersion,
      "org.eclipse.jetty" % "jetty-client" % jettyVersion,
      "org.eclipse.jetty" % "jetty-http" % jettyVersion,
      "org.eclipse.jetty" % "jetty-util" % jettyVersion,
      "org.http4s" %% "http4s-client-testkit" % http4sVersion % Test,
    ),
  )

lazy val docs = project.in(file("site")).enablePlugins(TypelevelSitePlugin)

val jettyApiMappings: Setting[_] =
  doc / apiMappings ++= (Compile / fullClasspath).value
    .flatMap { entry =>
      entry.get(moduleID.key).map(entry -> _)
    }
    .collect {
      case (entry, module)
          if module.organization == "org.eclipse.jetty" || module.organization == "org.eclipse.jetty.http2" =>
        val major = module.revision.split('.').head
        entry.data -> url(s"https://www.eclipse.org/jetty/javadoc/jetty-${major}/")
    }
    .toMap

// This works out of the box in Scala 2.13, but 2.12 needs some help.
val javaApiMappings: Setting[_] = {
  val javaVersion = sys.props("java.specification.version") match {
    case VersionNumber(Seq(1, v, _*), _, _) => v
    case VersionNumber(Seq(v, _*), _, _) => v
    case _ => 8 // not worth crashing over
  }
  val baseUrl = javaVersion match {
    case v if v < 11 => url(s"https://docs.oracle.com/javase/${javaVersion}/docs/api/")
    case _ => url(s"https://docs.oracle.com/en/java/javase/${javaVersion}/docs/api/java.base/")
  }
  doc / apiMappings ++= {
    val runtimeMXBean = java.lang.management.ManagementFactory.getRuntimeMXBean
    val bootClassPath =
      if (runtimeMXBean.isBootClassPathSupported)
        runtimeMXBean.getBootClassPath
          .split(java.io.File.pathSeparatorChar)
          .map(file(_) -> baseUrl)
          .toMap
      else
        Map.empty
    bootClassPath ++ Map(file("/modules/java.base") -> baseUrl)
  }
}
