import sbt.Keys.scalaVersion

name := "avro-scala-js"

scalaVersion in ThisBuild := "2.11.8"
scalaOrganization := "org.typelevel"

val circeVersion = "0.7.0"

lazy val root = project.in(file(".")).
	aggregate(fooJS, fooJVM).
	settings(
		publish := {},
		publishLocal := {}
	)

lazy val avroScalaJs = crossProject.in(file(".")).
	settings(
		name := "foo",
		version := "0.1-SNAPSHOT",
		resolvers ++= Seq(
			Resolver.sonatypeRepo("releases"),
			Resolver.sonatypeRepo("snapshots"),
			"mmreleases" at "https://artifactory.mediamath.com/artifactory/libs-release-global"
		),
		libraryDependencies ++= Seq(
			"com.chuusai" %% "shapeless" % "2.3.2",
			"org.scalatest" %% "scalatest" % "3.0.1",
			"io.circe" %% "circe-core" % circeVersion,
			"io.circe" %% "circe-generic" % circeVersion,
			"io.circe" %% "circe-parser" % circeVersion,
			scalaOrganization.value % "scala-reflect" % scalaVersion.value
		)
	).
	jvmSettings(
		libraryDependencies ++= Seq(
		 	"org.apache.avro" % "avro" % "1.8.1"
		)
	).
	jsSettings(
		// Add JS-specific settings here
	)

lazy val fooJVM = avroScalaJs.jvm
lazy val fooJS = avroScalaJs.js