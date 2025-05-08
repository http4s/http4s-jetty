import org.typelevel.sbt.gha

ThisBuild / tlBaseVersion := "0.22"

ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("rossabaker", "Ross A. Baker")
)
ThisBuild / startYear := Some(2014)

val Scala213 = "2.13.16"
ThisBuild / crossScalaVersions := Seq(Scala213, "2.12.20", "3.3.5")
ThisBuild / scalaVersion := Scala213 // the default Scala
ThisBuild / tlJdkRelease := Some(17)
ThisBuild / githubWorkflowJavaVersions ~= {
  // Jetty 12 bumps the requirement to Java 17
  _.filter { case JavaSpec(_, major) => major.toInt >= 17 }
}

lazy val root = project
  .in(file("."))
  .enablePlugins(NoPublishPlugin)
  .aggregate(jettyServer, jettyServerEe8, jettyClient, testing, client)

val http4sVersion = "0.22.15"
val jettyVersion = "12.0.5"
val nettyVersion = "4.1.76.Final"
val munitCatsEffectVersion = "1.0.7"
val scalaJava8CompatVersion = "1.0.2"
val slf4jVersion = "2.0.17"

lazy val jettyServer = project
  .in(file("jetty-server"))
  .settings(
    name := "http4s-jetty12-server",
    description := "Jetty implementation for http4s servers",
    libraryDependencies ++= Seq(
      "org.eclipse.jetty" % "jetty-client" % jettyVersion % Test,
      "org.eclipse.jetty" % "jetty-util" % jettyVersion,
      "org.eclipse.jetty.http2" % "jetty-http2-server" % jettyVersion,
      "org.http4s" %% "http4s-servlet" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion % Test,
      "org.slf4j" % "slf4j-simple" % slf4jVersion % Test,
      "org.typelevel" %% "munit-cats-effect-2" % munitCatsEffectVersion % Test,
    ),
    jettyApiMappings,
  )
  .dependsOn(testing % "test->test")

lazy val jettyServerEe8 = project
  .in(file("jetty-server-ee8"))
  .settings(
    name := "http4s-jetty12-server-ee8",
    description := "Jetty implementation for http4s servers on Java EE 8",
    libraryDependencies ++= Seq(
      "org.eclipse.jetty.ee8" % "jetty-ee8-servlet" % jettyVersion,
      "org.typelevel" %% "munit-cats-effect-2" % munitCatsEffectVersion % Test,
    ),
    jettyApiMappings,
  )
  .dependsOn(jettyServer % "compile;test->test")

lazy val jettyClient = project
  .in(file("jetty-client"))
  .settings(
    name := "http4s-jetty12-client",
    description := "jetty implementation for http4s clients",
    startYear := Some(2018),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-client" % http4sVersion,
      "org.eclipse.jetty" % "jetty-client" % jettyVersion,
      "org.eclipse.jetty" % "jetty-http" % jettyVersion,
      "org.eclipse.jetty" % "jetty-util" % jettyVersion,
      "org.scala-lang.modules" %% "scala-java8-compat" % scalaJava8CompatVersion,
      "org.slf4j" % "slf4j-simple" % slf4jVersion % Test,
    ),
  )
  .dependsOn(client % "test->test")

// http4s-client-testkit was not published in http4s-0.22
lazy val client = project
  .in(file("client"))
  .enablePlugins(NoPublishPlugin)
  .settings(
    startYear := Some(2014),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-client" % http4sVersion % Test,
      "org.http4s" %% "http4s-dsl" % http4sVersion % Test,
      "org.http4s" %% "http4s-server" % http4sVersion % Test,
      "io.netty" % "netty-buffer" % nettyVersion % Test,
      "io.netty" % "netty-codec-http" % nettyVersion % Test,
    ),
  )
  .dependsOn(testing % "test->test")

lazy val testing = project
  .in(file("testing"))
  .enablePlugins(NoPublishPlugin)
  .settings(
    startYear := Some(2016),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-laws" % http4sVersion % Test
    ),
  )

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
