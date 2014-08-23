name := "junkmail.tk"

version := "1.0"

scalaVersion  := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

libraryDependencies ++= {
  val akkaV = "2.3.0"
  val sprayV = "1.3.1"
  Seq(
    //"org.java-websocket"  %   "Java-WebSocket" % "1.3.0",
    "commons-codec" % "commons-codec" % "1.9",
    "com.h2database" % "h2" % "1.3.175",
    "com.typesafe.slick" %% "slick" % "2.0.2",
    "org.apache.commons" % "commons-lang3" % "3.3.2",
    "javax.mail" % "javax.mail-api" % "1.5.2",
    "com.sun.mail" % "javax.mail" % "1.5.2",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
    "org.eclipse.jetty.websocket" % "websocket-server" % "9.2.2.v20140723",
    "io.spray"            %%  "spray-json"    % "1.2.6",
    "io.spray"            %   "spray-can"     % sprayV,
    "io.spray"            %   "spray-routing" % sprayV,
    "io.spray"            %   "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "com.typesafe.akka"   %%  "akka-slf4j"    % akkaV,
    "org.specs2"          %%  "specs2-core"   % "2.3.7" % "test"
  )
}
