name := "hw3"

version := "1.0"

scalaVersion := "2.11.8"

//libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0"
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.8",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.slf4j" % "slf4j-api" % "1.7.29",
  "com.typesafe" % "config" % "1.4.0",
  "org.apache.spark" %% "spark-core" % "2.2.0",
  "com.amazonaws" % "aws-java-sdk" % "1.11.460"
)


assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _                             => MergeStrategy.first
}
