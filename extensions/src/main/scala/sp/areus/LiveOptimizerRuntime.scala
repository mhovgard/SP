package sp.areus

import akka.actor._
import com.codemettle.reactivemq._
import com.codemettle.reactivemq.ReActiveMQMessages._
import com.codemettle.reactivemq.model._
import sp.system.messages._
import sp.domain._
import sp.domain.Logic._

/**
  * Created by kristofer on 2016-04-12.
  */
class LiveOptimizerRuntime(info: RuntimeInfo) extends Actor {

  var theBus: Option[ActorRef] = None
  val busIP = info.settings.flatMap(_.getAs[String]("busip")).getOrElse("0.0.0.0")
  val topic = info.settings.flatMap(_.getAs[String]("topic")).getOrElse("areus")
  val replyTopic = info.settings.flatMap(_.getAs[String]("reply")).getOrElse("opt")


  val ev = context.actorSelection("/user/eventHandler")


  def receive = {
    case cr @ CreateRuntime(_, m, n, attr) => {
      ReActiveMQExtension(context.system).manager ! GetConnection(s"nio://${busIP}:61616")
      println(cr)
      sender ! info
    }
    case GetRuntimes => {
      sender ! info
    }

    case ConnectionEstablished(request, c) => {
      println("connected:"+request)
      c ! ConsumeFromTopic(topic)
      theBus = Some(c)
      ev ! Progress(SPAttributes("theBus"-> "Connected"), info.name, info.id)
    }
    case ConnectionFailed(request, reason) => {
      println("failed:"+reason)
    }
    case mess @ AMQMessage(body, prop, headers) => {
      val resp = SPAttributes.fromJson(body.toString)
    }
    case ConnectionInterrupted(ca, x) => {
      println("connection closed")
      theBus = None
    }
  }
}

object LiveOptimizerRuntime {
  def props(cr: RuntimeInfo) = Props(classOf[LiveOptimizerRuntime], cr)
}

trait LiveOptimizerLogic {
  def
}

