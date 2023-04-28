import java.util.Date

name := "support-triage-rotations-manager"
version := "0.1"
scalaVersion := "2.13.9"

val springVersion = "5.2.7.RELEASE"
val logbackVersion = "1.2.3"
val servletApiVersion = "4.0.1"
val jacksonVersion = "2.11.2"
val springCloudPropsVersion = "1.4.1"

enablePlugins(WarPlugin)
enablePlugins(TomcatPlugin)

artifactName := { (_: ScalaVersion, _: ModuleID, a: Artifact) =>
  a.name + "." + a.extension
}

packageOptions in(Compile, packageBin) += Package.ManifestAttributes("Build-Date" -> new Date().toString)
packageOptions in(Compile, packageBin) += Package.ManifestAttributes("Vcs-Release-Hash" -> new sbtrelease.Git(new File(".")).currentHash)

(sys.env.get("artifactory_username"), sys.env.get("artifactory_password")) match {
  case (Some(username), Some(password)) => credentials += Credentials("Artifactory Realm", "artifactory.bayer.com", username, password)
  case _ => credentials += Credentials(Path.userHome / ".sbt" / ".credentials-artifactory")
}

externalResolvers := Seq(
  "Bayer Artifactory Repository" at "https://artifactory.bayer.com/artifactory/shared-mvn-prod-shared",
  DefaultMavenRepository
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
  "com.bayer.scala-spring-jdbc" %% "scala-spring-jdbc" % "2.4.2",
  "com.bayer.scala-spring-jdbc" %% "scala-spring-jdbc" % "2.4.2",

  // HTTP
  "com.bayer.scala-apache-httpclient" %% "httpclient" % "1.1.5",
  "com.bayer.scala-oauth" %% "oauth-client-httpcomponents" % "2.3.5",

  // SERVLET
  "javax.servlet" % "javax.servlet-api" % servletApiVersion % "provided",

  // SWAGGER
  "io.springfox" % "springfox-swagger2" % "2.9.2",
  "io.springfox" % "springfox-swagger-ui" % "2.9.2",

  //LOGGING
  "net.logstash.logback" % "logstash-logback-encoder" % "5.2",
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "ch.qos.logback" % "logback-access" % logbackVersion,

  // JSON serialization
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % jacksonVersion,
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-csv" % jacksonVersion,
  "com.fasterxml.jackson.module" % "jackson-module-paranamer" % jacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
)

credentials += Credentials(Path.userHome / ".sbt" / ".credentials")


