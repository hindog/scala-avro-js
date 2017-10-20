import sbt.Keys.scalaVersion

name := "avro-scala-js"

scalaVersion in ThisBuild := "2.11.11"
organization in ThisBuild := "com.hindog.codec"

val circeVersion = "0.7.0"

lazy val commonSettings = Seq(
	crossScalaVersions := Seq("2.11.11", "2.12.1"),
	scalacOptions ++= Seq( "-feature", "-language:_" ),
	resolvers ++= Seq(
		Resolver.sonatypeRepo("releases"),
		Resolver.sonatypeRepo("snapshots"),
		"mmreleases" at "https://artifactory.mediamath.com/artifactory/libs-release-global"
	),
	updateOptions := updateOptions.value.withCachedResolution(true), // to speed up dependency resolution
	partialUnificationModule := "com.milessabin" % "si2712fix-plugin" % "1.2.0"
)


lazy val macros = crossProject
	.crossType(CrossType.Full)
	.in(file("macros"))
 	.settings(commonSettings: _*)
	.settings(
		name := "macros",
		libraryDependencies ++= Seq(
			"org.scala-lang" % "scala-reflect" % scalaVersion.value,
			"com.chuusai" %%% "shapeless" % "2.3.2",
			"org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full
		)
	)

lazy val macrosJS = macros.js
lazy val macrosJVM = macros.jvm

lazy val core = crossProject.in(file("core"))
  .dependsOn(macros)
 	.settings(commonSettings: _*)
  .settings(
		name := "core",
		libraryDependencies ++= Seq(
			"org.scalatest" %%% "scalatest" % "3.0.1",
			"io.circe" %%% "circe-core" % circeVersion,
			"io.circe" %%% "circe-generic" % circeVersion,
			"io.circe" %%% "circe-parser" % circeVersion,
			"org.wvlet" %%% "wvlet-log" % "1.2.3",
			scalaOrganization.value % "scala-reflect" % scalaVersion.value
		)
	)
  .jvmSettings(
		libraryDependencies ++= Seq(
		 	"org.apache.avro" % "avro" % "1.8.1",
		 	"org.slf4j" % "slf4j-api" % "1.7.+"
		)
	)
  .jsSettings(
		scalaJSUseMainModuleInitializer := true,
		mainClass in Compile := Some("com.hindog.codec.avro.TestCodec"),
		//scalacOptions := Seq("-Xlog-implicits"),
		libraryDependencies ++= Seq(
			"org.scala-js" %%% "scalajs-dom" % "0.9.3"
		),
		jsDependencies ++= Seq(
			ProvidedJS / "avsc.js"
		),
		jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
	)

lazy val fooJVM = core.jvm
lazy val fooJS = core.js

lazy val root = project.in(file(".")).
	aggregate(macrosJS, macrosJVM, fooJS, fooJVM).
	settings(
		publish := {},
		publishLocal := {}
	)