name := "hw3"

version := "1.0"

scalaVersion := "2.11.8"

//libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0"
libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.4.0",
  "org.apache.spark" %% "spark-core" % "2.2.0",
  "com.amazonaws" % "aws-java-sdk" % "1.11.460"
)


assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _                             => MergeStrategy.first
}
