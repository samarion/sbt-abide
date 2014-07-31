package scala.tools.abide

import sbt._
import Keys._

/**
 * Plugin for cross-publishing abide rule packages
 *
 * Inspired by sbt/main/src/main/scala/sbt/Cross.scala
 * The idea is to reuse the same concept behind ++ for temporary scala-version switching but
 * applying it to the abide version as well. This way we should be able to cross-version against
 * scala AND abide version.
 */
object AbidePublishPlugin extends AutoPlugin {

  private def crossVersions(state : State) : Seq[String] = {
    val p = Project.extract(state)
    import p._

    crossAbideVersions in currentRef get structure.data getOrElse Nil
  }

  private final val SwitchCommand : String = "++abide"
  private def switchParser(state : State) : Parser[(String, String)] = {
    val knownVersions = crossVersions(state)
    val version = token(StringBasic.examples(knownVersions : _*))
    val comand = token(SwitchCommand <~ OptSpace) flatMap { _ => token(matched(state.combinedParser & (opOrIDSpaced(SwitchCommand) ~ any.+))) }
    ...
  }

  lazy val switchAbide = Command.arb("++abide") { (state, version) =>
    val p = Project.extract(state)
    import p._

    val cleared = session.mergeSettings.filterNot(excludeKeys(Set(abideVersion.key)))
    val newStructure = Load.reapply(Seq(abideVersion :== version) ++ cleared, structure)
    Project.setProject(session, newStructure, state)
  }

  object autoImport {
    val abidePlugin = settingKey[Boolean]("Register project as abide plugin")
  }

  import autoImport._

  private lazy val publishSettings : Seq[sbt.Def.Setting[_]] = Seq(

    ...
  )

  override def requires = sbt.plugins.JvmPlugin

  override def trigger = allRequirements

  override def projectSettings = super.projectSettings ++ publishSettings
}
