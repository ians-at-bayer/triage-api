name := "triage"

version := "0.1"

scalaVersion := "2.12.10"

val springVersion = "5.2.7.RELEASE"
val logbackVersion = "1.2.3"

mainClass in (Compile, run) := Some("app.TriageRotations")

externalResolvers := Seq(
  // Set AWS company nexus as only source of dependencies.
  "Nexus Repository" at "https://nexus.platforms.engineering/repository/tps"
)

libraryDependencies ++= Seq(
  // SPRING
  "org.springframework" % "spring-beans" % springVersion,
  "org.springframework" % "spring-core" % springVersion,
  "org.springframework" % "spring-context" % springVersion,
  "org.springframework" % "spring-aop" % springVersion,
  "org.springframework" % "spring-webmvc" % springVersion,
  "org.springframework" % "spring-jdbc" % springVersion,

  // DB
  "org.apache.commons" % "commons-dbcp2" % "2.7.0",
  "org.postgresql" % "postgresql" % "42.2.14",
  "com.bayer.scala-spring-jdbc" %% "scala-spring-jdbc" % "1.2.8",
  "com.bayer.scala-spring-jdbc" %% "scala-spring-jdbc" % "1.2.8",

  // HTTP
  "com.bayer.scala-apache-httpclient" %% "httpclient" % "1.1.2",
  "com.monsanto.mbl.oauth" %% "oauth-client-httpcomponents" % "1.2.1",

  //LOGGING
  "net.logstash.logback" % "logstash-logback-encoder" % "5.2",
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "ch.qos.logback" % "logback-access" % logbackVersion,

)

credentials += Credentials(Path.userHome / ".sbt" / ".credentials")


