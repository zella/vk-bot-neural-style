name := """vk-bot-neural-style"""

version := "0.1beta"

scalaVersion := "2.12.2"

mainClass in assembly := Some("org.zella.runner.Runner")
mainClass in Compile := Some("org.zella.runner.Runner")

test in assembly := {}

// META-INF discarding
assemblyMergeStrategy in assembly := {
  case PathList(ps@_*) if ps.last endsWith
    "io.netty.versions.properties" => MergeStrategy.first
  case "io.netty.versions.properties" => MergeStrategy.last
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  //http://stackoverflow.com/a/30713280/1996639
  case PathList("reference.conf") => MergeStrategy.concat
  case x => MergeStrategy.last
}

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

// https://mvnrepository.com/artifact/org.scalatest/scalatest_2.12
libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.1" % "test"

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-testkit_2.12
libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.12" % "2.4.17" % "test"

// https://mvnrepository.com/artifact/org.mockito/mockito-all
libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % "test"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7"

// https://mvnrepository.com/artifact/com.typesafe.scala-logging/scala-logging_2.12
libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.5.0"

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-slf4j_2.12
libraryDependencies += "com.typesafe.akka" % "akka-slf4j_2.12" % "2.4.17"

// https://mvnrepository.com/artifact/com.typesafe/config
libraryDependencies += "com.typesafe" % "config" % "1.3.1"

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor_2.12
libraryDependencies += "com.typesafe.akka" % "akka-actor_2.12" % "2.4.17"

// https://mvnrepository.com/artifact/io.vertx/vertx-core
libraryDependencies += "io.vertx" % "vertx-core" % "3.4.1"
// https://mvnrepository.com/artifact/io.vertx/vertx-web
libraryDependencies += "io.vertx" % "vertx-web" % "3.4.1"

// https://mvnrepository.com/artifact/com.google.code.findbugs/jsr305
libraryDependencies += "com.google.code.findbugs" % "jsr305" % "3.0.1"
// https://mvnrepository.com/artifact/commons-io/commons-io
libraryDependencies += "commons-io" % "commons-io" % "2.5"
// https://mvnrepository.com/artifact/com.typesafe.play/play-json_2.12
libraryDependencies += "com.typesafe.play" % "play-json_2.12" % "2.6.0-RC2"

libraryDependencies += "net.databinder.dispatch" % "dispatch-core_2.12" % "0.12.0" % "test"
// https://mvnrepository.com/artifact/com.netaporter/scala-uri_2.12
libraryDependencies += "com.netaporter" % "scala-uri_2.12" % "0.4.16" % "test"
// https://mvnrepository.com/artifact/com.vk.api/sdk
libraryDependencies += "com.vk.api" % "sdk" % "0.5.2"
