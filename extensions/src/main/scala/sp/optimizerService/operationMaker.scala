package sp.optimizerService

import sp.domain._
import sp.psl.AbilityStructure
import akka.actor._
import sp.control.AddressValues
import sp.domain.logic.{PropositionParser, ActionParser, IDAbleLogic}
import sp.runnerService.RunnerService
import scala.concurrent._
import sp.system.messages._
import sp.system._
import sp.domain._
import sp.domain.Logic._
import sp.domain.Operation
import com.typesafe.config._


object operationMaker extends SPService {

  // Valuerestrictions for the robots. 
  val fixturePlaces = 8
  val towerRows = 4
  val towerCols = 4
  val r4ReachablePalette = List(1,2,3,4)
  val r5ReachablePalette = List(3,4)     // r4 kommer åt alla. r5 kommer åt 3 och 4
  val rs = List("R4", "R5")
  val opNamePickedUpCubes = "pickedUpCubes"


  val r4listOfPickUp = for {
      r <- rs(1)
      x <- r4ReachablePalette
      a <- 1 to fixturePlaces
    } yield { 
      Operation(s"$r"+s"pickCube$x$a",List(), SPAttributes("ability" -> AbilityStructure(s"$r"+".pickBlock.run", Some(s"$r"+".pickBlock.pos",x*10+a))))
    }
  
  val r5listOfPickUp = for {
      r <- rs(2)
      x <- r5ReachablePalette
      a <- 1 to fixturePlaces
    } yield { 
      Operation(s"$r"+s"pickCube$x$a",List(), SPAttributes("ability" -> AbilityStructure(s"$r"+".pickBlock.run", Some(s"$r"+".pickBlock.pos",x*10+a))))
    }




// Finns inte definierade på detta sätt för R5
  val listOfPutDownAll = for {
      r <- rs
      a <- 1 to towerRows
      b <- 1 to towerCols
      } yield {
        (s"$r",
        Operation(s"$r"+s"putDownCube$a$b", List(), SPAttributes("ability" -> AbilityStructure(s"$r"+".placeBlock.run", Some(s"$r"+".placeBlock.pos",a*10+b))))
        )

  }
  val r4listOfPutDown = listOfPutDownAll.filter(_._1 == rs(1)).unzip._2
  val r5listOfPutDown = listOfPutDownAll.filter(_._1 == rs(2)).unzip._2

  val r4toDodge = Operation("R4toDodge", List(), SPAttributes("ability" -> AbilityStructure("R4.toDodge.run", None))) // när man ska lyfta in paletter måste de stå i dodgeläge
  val r5toDodge = Operation("R4toDodge", List(), SPAttributes("ability" -> AbilityStructure("R5.toDodge.run", None)))
  val r4toHome = Operation("R5toHome", List(), SPAttributes("ability" -> AbilityStructure("R4.toHome.run", None)))
  val r5toHome = Operation("R5toHome", List(), SPAttributes("ability" -> AbilityStructure("R5.toHome.run", None)))

// X = 5 flytta in till byggplatsen
  val r2listOfplaceAtPos = for {
    x <- 1 to 5
    } yield {
      Operation(s"r2PlaceAtPos$x", List(), SPAttributes("ability" -> AbilityStructure("R2.placeAtPos.run", Some("R2.placeAtPos.run", x))))
    }
  val r2listpickAtPos = for {
    x <- 1 to 5
    } yield {
      Operation(s"r2PickAtPos$x", List(), SPAttributes("ability" -> AbilityStructure("R2.pickAtPos.run", Some("R2.pickAtPos.run", x))))
    }

  // Operations for flexlink
  // "R2.placeAtPos.run"

  val r2elevatorStn2ToHomeTable = Operation("r2elevatorStn2ToHomeTable", List(), SPAttributes("ability" -> AbilityStructure("R2.elevatorStn2ToHomeTable.run", None)))
  val r2homeTableToElevatorStn3 = Operation("r2homeTableToElevatorStn3", List(), SPAttributes("ability" -> AbilityStructure("R2.homeTableToElevatorStn3.run", None)))
  val r2homeTableToHomeBP = Operation("r2homeTableToHomeBP", List(), SPAttributes("ability" -> AbilityStructure("R2.homeTableToHomeBP.run", None)))
  val r2homeBPToHomeTable = Operation("r2homeBPToHomeTable", List(), SPAttributes("ability" -> AbilityStructure("R2.homeBPToHomeTable.run", None)))

      //Operation for test
  val h2Up = Operation("h2.up.run", List(), SPAttributes("ability"-> AbilityStructure("h2.up.run", None)))
  val h2Down= Operation("h2.down.run", List(), SPAttributes("ability"-> AbilityStructure("h2.down.run", None)))
  val h3Up = Operation("h3.up.run", List(), SPAttributes("ability"-> AbilityStructure("h3.up.run", None)))
  val h3Down = Operation("h3.down.run", List(), SPAttributes("ability"-> AbilityStructure("h3.down.run", None)))


  val specification = SPAttributes(
      "service" -> SPAttributes(
      "group" -> "hide" // to organize in gui. maybe use "hide" to hide service in gui
    ),
    "buildOrder" -> KeyDefinition("List[List[Int]]", List(), None)
  )
  val transformTuple =(
    TransformValue("buildOrder", _.getAs[List[List[Int]]]("buildOrder"))
    )
  val transformation = transformToList(transformTuple.productIterator.toList)
  def props = ServiceLauncher.props(Props(classOf[operationMaker]))

  }
  

  class operationMaker extends Actor with ServiceSupport {

    def receive = { /*
      case r@Request(service, attr, ids, reqID) => {
        val replyTo = sender()
        implicit val rnr = RequestNReply(r, replyTo)
        

    val attr = SPAttributes("command"->SPAttributes("commandType"->"execute", "execute"->id,
      "parameters" -> State(paraMap)))*/
    val pickOperations = operationMaker.r4listOfPutDown
    val placeOperations = operationMaker.r4listOfPutDown

    val aSOP = Parallel(Sequence(pickOperations(0), placeOperations(0), pickOperations(1),placeOperations(1)))

//    val thaSOP = SOPSpec("thaSOP", List(aSOP), SPAttributes())

   // askAService(Request("RunnerService", aSOP, List(pickOperations, placeOperations), ID.makeID))

//  val thaSOP = SOPSpec("thaSOP", List(aSOP), List(op1...), SPAttributes())
       //mottagare ! meddelande
     }

}


//       val root = HierarchyRoot("Resources", List(h2._1, h3._1, toOper._1, toRobo._1, R5._1, R4._1, R2._1, h1._1, h4._1, sensorIH2._1, HierarchyNode(sopSpec.id), HierarchyNode(thaSOP.id)))
