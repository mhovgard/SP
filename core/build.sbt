retrieveManaged := true
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence" % "2.4.1",
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8"
)
