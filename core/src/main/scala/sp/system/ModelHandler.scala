package sp.system

import akka.actor.Actor.Receive
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import akka.pattern.pipe

import scala.concurrent.duration._
import sp.system.messages._
import akka.persistence._
import sp.domain._
import sp.domain.LogicNoImplicit._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.pubsub._



//object TemporaryLauncher extends App {
//  implicit val system = ActorSystem("SP")
//  system.actorOf(ModelHandler.props, "modelHandler")
//}

class ModelHandler extends PersistentActor with ActorLogging  {
  override def persistenceId = "modelhandler"
  implicit val timeout = Timeout(10 seconds)
  import context.dispatcher

  private var modelMap: Map[ID, ActorRef] = Map()
  private var viewMap: Map[String, ActorRef] = Map()

  val cluster = Cluster(context.system)
  import DistributedPubSubMediator.{ Put, Subscribe, Publish }
  val mediator = DistributedPubSub(context.system).mediator
  mediator ! Put(self)
  mediator ! Subscribe("modelHandler", self)

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)




  def receiveCommand = {
    //case mess @ _ if {println(s"handler got: $mess from $sender"); false} => Unit

    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case _: MemberEvent => // ignore


    case cm @ CreateModel(id, name, attr) =>
      val reply = sender()
      if (!modelMap.contains(id)){
        persist(cm){n =>
          addModel(n)
          modelMap(n.id) ! n
          val info = ModelInfo(id, name, 1, attr, List())
          reply ! SPOK()
        }
        reply ! SPOK()
      } else reply ! SPError("A model with that ID do already exist.")

    case del: DeleteModel =>
      if (modelMap.contains(del.model)){
        val reply = sender()
        persist(del){ d =>
          println(del)
          deleteModel(del)
          val delMess = ModelDeleted(del.model, SPAttributes())
          reply ! SPOK()
          mediator ! Publish("eventHandler", delMess)
        }
      }
      else sender ! SPError(s"Model ${del.model} does not exist.")

    case imp @ ImportModel(id, mi, ids, h) => {
      if (!modelMap.contains(id)) {
        self ! CreateModel(id, mi.name, mi.attributes)
        self ! imp
      } else
        modelMap(id) forward imp
    }
    case m: ModelCommand =>
      if (modelMap.contains(m.model)) modelMap(m.model) forward m
      else sender ! SPError(s"Model ${m.model} does not exist.")

    case (m: ModelCommand, v: Long) =>
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

  def deleteModel(del: DeleteModel) = {
    if (modelMap.contains(del.model)){
      modelMap(del.model) ! PoisonPill
      modelMap = modelMap - del.model
    }
    else sender ! SPError(s"Model ${del.model} does not exist.")
  }

  def viewNameMaker(id: ID, v: Long) = id.toString() + " - Version: " + v

  var reMod = Map[ID, CreateModel]()
  def receiveRecover = {
    case cm: CreateModel  =>
      reMod = reMod + (cm.id->cm)
    case dm: DeleteModel =>
      reMod = reMod - dm.model
    case RecoveryCompleted =>
      reMod.values.foreach(addModel)
      reMod = Map()
  }

}

object ModelHandler {
  def props = Props(classOf[ModelHandler])
}

