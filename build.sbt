import com.typesafe.sbt.SbtNativePackager._

import NativePackagerKeys._

import com.typesafe.sbt.packager.archetypes.ServerLoader.{SystemV, Upstart}

organization  := "junkmail.tk"

name          := "junkmail-service"

version       := "0.5"

scalaVersion  := "2.10.4"

maintainer in Linux := "Alexey Ponkin <alexey.ponkin@gmail.com>"

packageSummary in Linux := "junkmail.tk server"

packageDescription := "Websocket server for junkmail.tk"

packageDescription in Linux := packageDescription.value

daemonUser in Linux := "junkmail" // user which will execute the application

daemonGroup in Linux := daemonUser.value    // group which will execute the application

packageArchetype.java_server

serverLoading in Debian := SystemV

bashScriptConfigLocation := Some("${app_home}/../conf/jvmopts")

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Spray repository" at "http://repo.spray.io/",
  "OSS" at "https://oss.sonatype.org/content/repositories/snapshots",
  "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

libraryDependencies ++= {
  val akkaV = "2.3.5"
  val sprayV = "1.3.1"
  Seq(
    "com.typesafe.slick" %% "slick" % "2.1.0",
    "joda-time" % "joda-time" % "2.4",
    "org.joda" % "joda-convert" % "1.6",
    "com.github.tototoshi" %% "slick-joda-mapper" % "1.2.0",
    "com.github.nscala-time" %% "nscala-time" % "1.4.0",
    "commons-codec" % "commons-codec" % "1.9",
    "com.sun.mail" % "javax.mail" % "1.5.2",
    "com.h2database" % "h2" % "1.4.181",
    "org.apache.commons" % "commons-lang3" % "3.3.2",
    "com.typesafe.slick" %% "slick" % "2.1.0",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.slf4j" % "slf4j-api" % "1.7.7",
    "org.eclipse.jetty.websocket" % "websocket-api" % "9.2.2.v20140723",
    "org.eclipse.jetty.websocket" % "websocket-server" % "9.2.2.v20140723",
    "io.spray"            %%  "spray-json"     % "1.2.6",
    "io.spray"            %   "spray-can"      % sprayV,
    "io.spray"            %   "spray-routing"  % sprayV,
    "com.typesafe.akka"   %%  "akka-actor"     % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"   % akkaV   % "test",
    "io.spray"            %   "spray-testkit"  % sprayV  % "test",
    "junit"               %   "junit"          % "4.11"  % "test",
    "org.specs2"          %%  "specs2"         % "2.3.11" % "test"
  )
}

    