import org.typelevel.sbt.gha

ThisBuild / tlBaseVersion := "0.26" // your current series x.y

ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("rossabaker", "Ross A. Baker")
)
ThisBuild / startYear := Some(2014)

// publish website from this branch
ThisBuild / tlSitePublishBranch := Some("main")

val Scala213 = "2.13.18"
ThisBuild / crossScalaVersions := Seq(Scala213, "2.12.21", "3.3.7")
ThisBuild / scalaVersion := Scala213 // the default Scala
ThisBuild / tlJdkRelease := Some(17)
ThisBuild / githubWorkflowJavaVersions ~= {
  // The minimum required Java version for Jetty 12 is 17.
  _.filter { case JavaSpec(_, major) => major.toInt >= 17 }
}

ThisBuild / resolvers +=
  "s01 snapshots".at("https://s01.oss.sonatype.org/content/repositories/snapshots/")

lazy val root = project
  .in(file("."))
  .enablePlugins(NoPublishPlugin)
  .aggregate(jettyServer, jettyServerEe8, jettyClient)

val catsEffectVersion = "3.6.3"
val jettyVersion = "12.1.7"
val http4sVersion = "0.23.33"
val http4sServletVersion = "0.24.0-RC2"
val munitCatsEffectVersion = "2.1.0"
val slf4jVersion = "1.7.25"
val scalaJava8Compat = "1.0.2"

lazy val jettyServer = project
  .in(file("jetty-server"))
  .settings(
    name := "http4s-jetty-server",
    description := "Jetty implementation for http4s servers",
    libraryDependencies ++= Seq(
      "org.eclipse.jetty" % "jetty-client" % jettyVersion % Test,
      "org.eclipse.jetty" % "jetty-util" % jettyVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion % Test,
      "org.typelevel" %% "munit-cats-effect" % munitCatsEffectVersion % Test,
    ),
    jettyApiMappings,
  )

lazy val jettyServerEe8 = project
  .in(file("jetty-server-ee8"))
  .settings(
    name := "http4s-jetty-server-ee8",
    description := "Jetty implementation for http4s servers",
    libraryDependencies ++= Seq(
      "org.eclipse.jetty.ee8" % "jetty-ee8-servlet" % jettyVersion,
      "org.eclipse.jetty.http2" % "jetty-http2-server" % jettyVersion,
      "org.http4s" %% "http4s-server" % http4sVersion,
      "org.http4s" %% "http4s-servlet" % http4sServletVersion,
    ),
    jettyApiMappings,
  )
  .dependsOn(jettyServer % "compile;test->test")

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

lazy val docs = project
  .in(file("site"))
  .enablePlugins(Http4sOrgSitePlugin)

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
