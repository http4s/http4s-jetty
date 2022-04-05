// https://typelevel.org/sbt-typelevel/faq.html#what-is-a-base-version-anyway
ThisBuild / tlBaseVersion := "0.0" // your current series x.y

ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("rossabaker", "Ross A. Baker")
)

// publish website from this branch
ThisBuild / tlSitePublishBranch := Some("main")

val Scala213 = "2.13.8"
ThisBuild / crossScalaVersions := Seq(Scala213, "2.12.15", "3.1.1")
ThisBuild / scalaVersion := Scala213 // the default Scala

lazy val root = project
  .in(file("."))
  .enablePlugins(NoPublishPlugin)
  .aggregate(jettyServer)

val jettyVersion = "9.4.46.v20220331"
val http4sVersion = "0.23.11"
val http4sServletVersion = "0.23.11"
val munitCatsEffectVersion = "1.0.7"
val slf4jVersion = "1.7.25"

lazy val jettyServer = project
  .in(file("jetty-server"))
  .settings(
    name := "http4s-jetty-server",
    description := "Jetty implementation for http4s servers",
    startYear := Some(2014),
    libraryDependencies ++= Seq(
      "org.eclipse.jetty" % "jetty-http" % jettyVersion,
      "org.eclipse.jetty" % "jetty-servlet" % jettyVersion,
      "org.eclipse.jetty" % "jetty-util" % jettyVersion,
      "org.eclipse.jetty.http2" % "http2-server" % jettyVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion % Test,
      "org.http4s" %% "http4s-servlet" % http4sServletVersion,
      "org.typelevel" %% "munit-cats-effect-3" % munitCatsEffectVersion % Test,
    ),
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

lazy val docs = project.in(file("site")).enablePlugins(TypelevelSitePlugin)
