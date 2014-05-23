name := "donkbot"

version := "1.0"

resolvers ++= Seq(
  DefaultMavenRepository,
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  // this is super ugly, haven't figured out a better way to do it
  "project-local" at "file:///" + (baseDirectory.value / "repo").getAbsolutePath.toString
)

val json4sNative = "org.json4s" %% "json4s-native" % "3.2.9"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.github.scopt" %% "scopt" % "3.2.0",
  "com.github.nscala-time" %% "nscala-time" % "1.0.0",
  "com.stackmob" %% "newman" % "1.3.5",
  "org.pircbotx" % "pircbotx" % "2.0.1",
  "org.jsoup" % "jsoup" % "1.7.3",
  "com.google.apis" % "google-api-services-customsearch" % "v1-rev33-1.17.0-rc",
  "com.google.http-client" % "google-http-client-jackson" % "1.15.0-rc",
  "org.twitter4j" % "twitter4j-core" % "3.0.5",
  "org.ocpsoft.prettytime" % "prettytime" % "3.2.4.Final",
  "com.xeiam.xchange" % "xchange-bitcoinaverage" % "2.0.1-SNAPSHOT",
  "com.xeiam.xchange" % "xchange-bitstamp" % "2.0.1-SNAPSHOT",
  "com.xeiam.xchange" % "xchange-coinbase" % "2.0.1-SNAPSHOT",
  "it" % "tomatoclient" % "0.0.1-SNAPSHOT",
  "org.joda" % "joda-money" % "0.9.1",
  json4sNative
)