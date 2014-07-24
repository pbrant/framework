import Dependencies._

organization in ThisBuild          := "net.liftweb"

version in ThisBuild               := "3.0-ccap06"

homepage in ThisBuild              := Some(url("http://www.liftweb.net"))

licenses in ThisBuild              += ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

startYear in ThisBuild             := Some(2006)

organizationName in ThisBuild      := "WorldWide Conferencing, LLC"

scalaVersion in ThisBuild          := "2.10.4"

crossScalaVersions in ThisBuild    := Seq("2.10.4", "2.11.1")

libraryDependencies in ThisBuild <++= scalaVersion {sv => Seq(specs2(sv), scalacheck, scalatest(sv)) }

// Settings for Sonatype compliance
pomIncludeRepository in ThisBuild  := { _ => false }

//publishTo in ThisBuild            <<= isSnapshot(if (_) Some(Opts.resolver.sonatypeSnapshots) else Some(Opts.resolver.sonatypeStaging))
publishTo in ThisBuild <<= (version) { version: String =>
  val repo =
    if (version.trim.endsWith("SNAPSHOT"))
      "CCAP Snapshots" at "http://repo.wicourts.gov/artifactory/libs-snapshot-local"
    else
      "CCAP Releases" at "http://repo.wicourts.gov/artifactory/libs-release-local"
  Some(repo)
}

scmInfo in ThisBuild               := Some(ScmInfo(url("https://github.com/lift/framework"), "scm:git:https://github.com/lift/framework.git"))

pomExtra in ThisBuild              :=  Developers.toXml

//credentials in ThisBuild <+= state map { s => Credentials(BuildPaths.getGlobalSettingsDirectory(s, BuildPaths.getGlobalBase(s)) / ".credentials") }
credentials in ThisBuild += Credentials(Path.userHome / ".ivy2" / ".credentials")

initialize <<= (name, version, scalaVersion) apply printLogo

resolvers  in ThisBuild           ++= Seq(
  "snapshots"     at "https://oss.sonatype.org/content/repositories/snapshots",
  "releases"      at "https://oss.sonatype.org/content/repositories/releases"
)
