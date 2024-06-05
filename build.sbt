import org.typelevel.sbt.gha

ThisBuild / tlBaseVersion := "0.24" // your current series x.y

ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("rossabaker", "Ross A. Baker")
)
ThisBuild / startYear := Some(2014)

// publish website from this branch
ThisBuild / tlSitePublishBranch := Some("main")

val Scala213 = "2.13.14"
ThisBuild / crossScalaVersions := Seq(Scala213, "2.12.19", "3.3.3")
ThisBuild / scalaVersion := Scala213 // the default Scala
ThisBuild / tlJdkRelease := Some(11)
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

val jettyVersion = "11.0.21"
val http4sVersion = "0.23.27"
val http4sServletVersion = "0.24.0-RC1"
val munitCatsEffectVersion = "1.0.7"
val slf4jVersion = "1.7.25"

lazy val jettyServer = project
  .in(file("jetty-server"))
  .settings(
    name := "http4s-jetty-server",
    description := "Jetty implementation for http4s servers",
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
