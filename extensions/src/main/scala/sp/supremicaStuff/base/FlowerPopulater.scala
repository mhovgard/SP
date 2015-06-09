package sp.supremicaStuff.base

import scala.collection.JavaConverters._
import net.sourceforge.waters.subject.module.SimpleComponentSubject
import net.sourceforge.waters.subject.module.EdgeSubject

object NamingInAutogeneratedFlowers extends NamingInAutogeneratedFlowers
sealed trait NamingInAutogeneratedFlowers {
  lazy val EFA_NAME = "efa"
  lazy val FORBIDDEN_EVENT_NAME = TextFilePrefix.UNCONTROLLABLE_PREFIX + "x"
  lazy val FORBIDDEN_VARIABLE_NAME = "vx"
}

trait FlowerPopulater extends BasePopulater with Algorithms {

  var mUniqueIndexCounter = 0
  def indexCounterFactory = { mUniqueIndexCounter += 1; mUniqueIndexCounter - 1 }

  def efaNameFactory: String = {
    val suggestion = NamingInAutogeneratedFlowers.EFA_NAME + indexCounterFactory
    if (getEFAs.map(_.getName()).contains(suggestion)) efaNameFactory else suggestion
  }

  def forbiddenEventNameFactory(): String = {
    val suggestion = NamingInAutogeneratedFlowers.FORBIDDEN_EVENT_NAME + indexCounterFactory
    if (getAlphabet.map(_.getName()).contains(suggestion)) forbiddenEventNameFactory else suggestion
  }

  def forbiddenVariableNameFactory(): String = {
    val suggestion = NamingInAutogeneratedFlowers.FORBIDDEN_VARIABLE_NAME + indexCounterFactory
    if (getVariables.map(_.getName()).contains(suggestion)) forbiddenVariableNameFactory else suggestion
  }

  /*
   * Returns a flower that does not contain this eventLabel
   */
  def getFreshFlower(eventLabel: String): Option[SimpleComponentSubject] = {
    getFlowers.foreach(efa => if (!efaContainsEventLabel(efa, eventLabel)) { return Some(efa) })
    addFlowerRoot(efaNameFactory)
  }

  def efaContainsEventLabel(efa: SimpleComponentSubject, eventLabel: String): Boolean = {
    efa.getGraph().getEdgesModifiable().asScala.foreach(_.getLabelBlock().getEventIdentifierList().asScala.foreach(
      event => if (event.toString().equals(eventLabel)) { return true }))
    false
  }

  /*
   * Not that the event must be added to the module alphabet separately
   */
  def addLeafToEfa(efa: SimpleComponentSubject, eventName: String, guardAsText: String, actionsAsText: String): Option[EdgeSubject] = {
    if (getFlowers.contains(efa)) {
      val root = efa.getGraph().getNodes().iterator().next()
      addEdge(efa.getGraph(), eventName, root, root, guardAsText, actionsAsText)
    } else { println(efa.getName() + " is not a flower in this module"); None }
  }

  /*
	 * The leaf is added to an efa where the eventName is not labeled to any of the existing transitions.
	 */
  def addLeaf(eventName: String, guardAsText: String, actionsAsText: String): Option[EdgeSubject] = {
    getFreshFlower(eventName).flatMap(addLeafToEfa(_, eventName, guardAsText, actionsAsText))
  }

  def addLeafAndEventToAlphabet(eventName: String, unControllable: Boolean, guardAsText: String, actionsAsText: String): Option[EdgeSubject] = {
    addEventIfNeededElseReturnExistingEvent(eventName, unControllable)
    addLeaf(eventName, guardAsText, actionsAsText)
  }

  def addSupervisorGuardsToFreshFlower(optSupervisorEventGuardMap : Option[Map[String,String]]= getSupervisorGuards()): Option[SimpleComponentSubject] = {
    val eventGuardMap = optSupervisorEventGuardMap.map(_.filter { case (_, guard) => !guard.equals("1") })
    if (eventGuardMap.isEmpty) { return None }
    addFlowerRoot(efaNameFactory).map(efa => {
      eventGuardMap.map(_.foreach {
        case (label, guard) => if (addLeafToEfa(efa, label, guard, null).isEmpty) { return None }
      }); efa
    })
  }

  private var mForbiddenVariableName = ""
  def addForbiddenExpression(forbiddenExpression: String, addSelfLoop: Boolean = true, addInComment: Boolean = false): Unit = {
    // Add forbidden variable on first entry
    if (addSelfLoop) {
      if (mForbiddenVariableName.isEmpty()) {
        mForbiddenVariableName = forbiddenVariableNameFactory()
        if (addVariable(mForbiddenVariableName, 0, 1, 0, Set(0)).isEmpty) { return }
      }
      addLeafAndEventToAlphabet(forbiddenEventNameFactory(), true, s"($forbiddenExpression)&$mForbiddenVariableName==0", s"$mForbiddenVariableName=1")
    }
    if (addInComment) { mModule.setComment(s"$getComment${TextFilePrefix.FORBIDDEN_PREFIX}$forbiddenExpression") }
  }
}