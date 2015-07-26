package sp.system

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Future
import akka.pattern.pipe
import scala.concurrent.duration._
import sp.system.messages._
import akka.persistence._
import sp.system.SPActorSystem.eventHandler

import sp.domain._

class ModelHandler extends PersistentActor {
  override def persistenceId = "modelhandler"
  implicit val timeout = Timeout(1 seconds)
  import context.dispatcher

  private var modelMap: Map[ID, ActorRef] = Map()
  private var viewMap: Map[String, ActorRef] = Map()

  def receiveCommand = {
    //case mess @ _ if {println(s"handler got: $mess from $sender"); false} => Unit

    case cm @ CreateModel(id, name, attr) =>
      val reply = sender()
      if (!modelMap.contains(id)){
        persist(cm){n =>
          println(s"The modelHandler creates a new model called ${cm.name} id: ${cm.id}")
          addModel(n)
          modelMap(n.id) ! n
        }
        reply ! "OK"
      } else reply ! SPError("A model with that ID do already exist.")

    case dm @ DeleteModel(id) =>
      val reply = sender()
      if (modelMap.contains(id)){
        persist(dm){n =>
          println(s"The modelService deletes the model with id: ${dm.model}")
          deleteModel(n)
          eventHandler ! ModelDeleted(EventTargets.ModelHandler, EventTypes.Deletion, dm.model)
        }
        reply ! "OK"
      } else {
        reply ! SPError("There's no model with that ID")
      }

    case m: ModelMessage =>
      if (modelMap.contains(m.model)) modelMap(m.model) forward m
      else sender ! SPError(s"Model ${m.model} does not exist.")

    case (m: ModelMessage, v: Long) =>
      val viewName = viewNameMaker(m.model, v)
      if (!viewMap.contains(viewName)) {
        println(s"The modelHandler creates a new view for ${m.model} version: ${v}")
        val view = context.actorOf(sp.models.ModelView.props(m.model, v, viewName))
        viewMap += viewName -> view
      }
      viewMap(viewName).tell(m, sender())


    case GetModels =>
      val reply = sender()
      if (modelMap.nonEmpty){
        val fList = Future.traverse(modelMap.values)(x => (x ? GetModels).mapTo[ModelInfo]) map(_ toList)
        fList map ModelInfos pipeTo reply
      } else reply ! ModelInfos(List[ModelInfo]())
  }

  def addModel(cm: CreateModel) = {
    val newModelH = context.actorOf(sp.models.ModelActor.props(cm.id))
    modelMap += cm.id -> newModelH
  }

  def deleteModel(dm: DeleteModel) = {
    context.stop(modelMap(dm.model))
    modelMap -= dm.model
  }

  def viewNameMaker(id: ID, v: Long) = id.toString() + " - Version: " + v

  def receiveRecover = {
    case cm: CreateModel  =>
      addModel(cm)
    case dm: DeleteModel =>
      deleteModel(dm)
  }

}

object ModelHandler {
  def props = Props(classOf[ModelHandler])
}