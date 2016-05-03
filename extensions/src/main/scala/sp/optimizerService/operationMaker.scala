package sp.optimizerService

import akka.actor._
import scala.concurrent._
import sp.system._
import sp.domain._
import sp.domain.Logic._
import sp.psl.AbilityStructure
import sp.control.AddressValues
import sp.domain.logic.{PropositionParser, ActionParser, IDAbleLogic}
import sp.runnerService.RunnerService
import scala.concurrent._
import sp.system.messages._
import sp.domain.Operation
import com.typesafe.config._
import scala.collection.mutable._


object operationMaker extends SPService {
  val specification = SPAttributes(
    "service" -> SPAttributes(
      "group"-> "Operations",
      "description" -> "Makes operations"
    )
  )

  val transformation: List[TransformValue[_]] = List()
  def props = ServiceLauncher.props(Props(classOf[operationMaker]))

  val fixturePlaces = 8
  val towerRows = 4
  val towerCols = 4
  val r4ReachablePalette = List(1,2,3,4)
  val r5ReachablePalette = List(3,4)     // r4 kommer åt alla. r5 kommer åt 3 och 4
  val rs = List("R4", "R5")
  val opNamePickedUpCubes = "pickedUpCubes"

  def pack(xs: List[(Any,Any)]): List[List[Any]] = xs match {
    case Nil      => List(Nil)
    case x :: Nil => List(x._2 :: Nil)
    case x :: _   => List(xs.filter(_._1 == xs.head._1).unzip._2) ++ pack(xs.filter(_._1 != xs.head._1))
  }




  val r4listOfPickUpAll = for
  {
    x <- r4ReachablePalette;
    a <- List.range(1,fixturePlaces) //1 to fixturePlaces
  }
    yield {
      x ->
      (Operation(s"$rs(0)"+s"pickCube$x$a",List(), SPAttributes("ability" -> AbilityStructure(s"$rs(0)"+".pickBlock.run", Some(s"$rs(0)"+".pickBlock.pos",x*10+a)))))
    }
  
  val r4listOfPickUp = pack(r4listOfPickUpAll) // List(List(elementen på palett), List(elementen på palett), ...)


  val r5listOfPickUpAll = for
  {
    x <- r5ReachablePalette;
    a <- 1 to fixturePlaces //range kanske inte behövs. Vill inte generera IndexSeq, tyvm.
  }
    yield {
      x ->
      Operation(s"$rs(1)"+s"pickCube$x$a",List(), SPAttributes("ability" -> AbilityStructure(s"$rs(1)"+".pickBlock.run", Some(s"$rs(1)"+".pickBlock.pos",x*10+a))))
    }
  
  val r5listOfPickUp = pack(r5listOfPickUpAll) // List(List(elementen på palett), List(elementen på palett), ...)



// Finns inte definierade på detta sätt för R5 än tror jag
  val listOfPutDownAll = for (
      r <- rs;
      a <- 1 to towerRows;
      b <- 1 to towerCols
  ) yield {
        s"$r" -> (a ->
        Operation(s"$r"+s"putDownCube$a$b", List(), SPAttributes("ability" -> AbilityStructure(s"$r"+".placeBlock.run", Some(s"$r"+".placeBlock.pos",a*10+b)))))
  }
  val r4listOfPutDown = pack(listOfPutDownAll.filter(_._1 == rs(0)).unzip._2)
  val r5listOfPutDown = pack(listOfPutDownAll.filter(_._1 == rs(1)).unzip._2)

  val r4toDodge = Operation("R4toDodge", List(), SPAttributes("ability" -> AbilityStructure("R4.toDodge.run", None))) // när man ska lyfta in paletter måste de stå i dodgeläge
  val r5toDodge = Operation("R4toDodge", List(), SPAttributes("ability" -> AbilityStructure("R5.toDodge.run", None)))
  val r4toHome = Operation("R5toHome", List(), SPAttributes("ability" -> AbilityStructure("R4.toHome.run", None)))
  val r5toHome = Operation("R5toHome", List(), SPAttributes("ability" -> AbilityStructure("R5.toHome.run", None)))

// X = 5 flytta in till byggplatsen
  val r2listOfplaceAtPos = for
  (
    x <- 1 to 5
  )
  yield {
    (Operation(s"r2PlaceAtPos$x", List(), SPAttributes("ability" -> AbilityStructure("R2.placeAtPos.run", Some("R2.placeAtPos.run", x)))))
  }

  val r2listpickAtPos = for
  {
    x <- 1 to 5
  }
    yield {
      (Operation(s"r2PickAtPos$x", List(), SPAttributes("ability" -> AbilityStructure("R2.pickAtPos.run", Some("R2.pickAtPos.run", x)))))
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


  //Tar in bygg order
  //Returnerar en lista med placerings operationer
  // Splittar tornet rakt i mitten där r4 tar klossarna tv och r5 klossarna th

  def getPlaceOperations(ls :List[List[Int]]):List[Any] = {
    val returnList = MutableList[Any]() // Who needs typematching anyway...
    for (a <- 0 to 3) {
      for (b <- 0 to 3) {
        if (ls(a)(b) > 0)
          returnList += (if (b < 2) MutableList[Any](r4listOfPutDown(a)(b)) else MutableList[Any](r5listOfPutDown(a)(b)))
      }
    }
    return returnList.toList
  }


  //Tar in byggorder
  //Returnerar en lista med plocknings operationer
  // Denna är helt jävla fel :o
  /*
  def getPickOperations(ls :List[List[Int]]) = {
    var returnList: List[Operation] = List()
    var i = 0;
    for(a <- 0 to 3) {
       for(b <- 0 to 3) {
          if(ls(a)(b) > 0) {
            returnList::List(r4listOfPickUp(i))
            i += 1
          }
       }
    }
    returnList
  }*/


//   val r4listOfPutDown = listOfPutDownAll.filter(_._1 == rs(1)).unzip._2

  //Fråga Robotgruppen om start och slut operationerna

  //Returnerar en lista av operationer som innehåller startsekvensen av processen

  def getStartOperations():List[Operation] =  {
    List(r4toHome,r5toHome,/*Hämta byggpalett*/r2homeTableToHomeBP,r2listOfplaceAtPos(5),
      h2Up,r2homeBPToHomeTable,/*Hämta från hissen*/r2elevatorStn2ToHomeTable, r4toDodge,h2Down, r2homeTableToHomeBP, r2listOfplaceAtPos(1),
      h2Up/*r2hämtapalletvidh2*/,r2elevatorStn2ToHomeTable,r2homeTableToHomeBP, h2Down,r2listOfplaceAtPos(2)
    )
  }

  //Returnerar en lista av operationer som innehåller slutsekvensen av processen

  def getEndOperations():List[Operation] = {
    List(r4toDodge,r2homeTableToHomeBP,r2listpickAtPos(1),h3Up,r2homeBPToHomeTable,r2homeTableToElevatorStn3,
      h3Down,r2listpickAtPos(1),h3Up,r2homeTableToElevatorStn3,h3Down,r2homeTableToHomeBP,h3Up,r2listpickAtPos(2),
      r2homeTableToElevatorStn3, h3Down
    )
  }

  //Skapar en SOP
/* Vill liksom sova nu... 
  def getSOP(ls : List[List[Int]]) = {
    val sop = SOP();
    for(ls <- getStartOperations()) {
      sop.+(SOP(Sequence(ls)))
    }
    var tempList = for((pi, pl) <-(getPickOperations(ls) zip getPlaceOperations(ls))) yield sop.+(SOP(Sequence(pi,pl)))

    for(ls <- getEndOperations()) {
      sop.+(SOP(Sequence(ls)))
    }
    sop
  } */






}


class operationMaker extends Actor with ServiceSupport {
  def receive = {
    case r @ Request(service, attr, ids, reqID) => {
      val replyTo = sender()
      implicit val rnr = RequestNReply(r, replyTo)



      //inti
      val init = Thing("init")
      val useTwoPalettes = Thing("useTwoPalettes")
      //For the buildingspace were the tower should be built
      val buildSpotBooked = Thing("buildSpotBooked")
      //Things for elivator 1
      val H1UpWithBuildPalette1 = Thing("H1UpWithBuildPalette1")
      val H1UpWithBuildPalette2 = Thing("H1UpWithBuildPalette2")
      //Things for elivator 2
      val H2UpWithBuildPalette1 = Thing("H2UpWithBuildPalette1")
      val H2UpWithBuildPalette2 = Thing("H2UpWithBuildPalette2")
      //Book robot 2
      val R2Booked = Thing("R2Booked")
      //Book robot 4
      val R4Booked = Thing("R4Booked")
      //Book robot 5
      val R5Booked = Thing("R5Booked")
      //Status of buildPalette 1
      val buildPalette1Empty = Thing("buildPalette1Empty")
      //Status of buildPalette 2
      val buildPalette2Empty = Thing("buildPalette2Empty")
      //Status of buildningPalette
      val buildingPaletteComplete = Thing("buildingPaletteComplete")
      //Tells when R2 is done
      val R2OPComplete = Thing("R2OPComplete")
      //Tells when R4 is done
      val R4OPComplete = Thing("R4OPComplete")
      //Tells when R5 is done
      val R5OPComplete = Thing("R5OPComplete")
      //Tells if R4 is holding a cube
      val R4HoldingCube = Thing("R4HoldingCube")
      //Tells if R4 is holding a cube
      val R5HoldingCube = Thing("R5HoldingCube")
      //Things for status of palettes
      val BuildingPalette1In = Thing ("BuildingPalette1In")
      val BuildingPalette2In = Thing ("BuildingPalette2In")
      val BuildPaletteIn = Thing ("BuildingPaletteIn")

      //Vals for makeing Thnigs
      val fixtNo = 2
      val posFix = 8
      val row = 4
      val col = 4
      val rs = List("R4", "R5")
      val opNamePickedUpCubes = "PickedUpCubes"
      val opNamePutDownCubes = "PutDownCubes"
      val palette = "palette"
      val At = "At"
      val Space = "Space"
      val a = "a"
      val g = "g"

      //Ints for telling if ops are complete
      var Int1 = 0
      var Int2 = 0

      //Makes Things for PickUpCubes
      val listOfPickedUpCubes = for {
        r <- rs
        f <- 1 to fixtNo
        p <- 1 to posFix
      } yield {
        Thing(s"$r$opNamePickedUpCubes$f$p")
      }

      //Makes things for PutDownCubes
      val listOfPutDownCubes = for {
        f <- 1 to row
        p <- 1 to col
      } yield {
        Thing(s"$opNamePutDownCubes$f$p")
      }

      // Status of building palettes
      val listOfStatusBuildingPalettes = for {
        r <- rs
        f <- 1 to fixtNo
        t <- 1 to 2
      } yield{
        Thing(s"$palette$f$At$r$Space$t")
      }

      //Things for showing which cubes to be placed
      val cubesToBePlaced = "cubesToBePlaced"
      val listOfCubesToPlaced = for {
        f <- 1 to row
        p <- 1 to col
      } yield {
        Thing(s"$cubesToBePlaced$f$p")
      }

      //Iniate parsers--------------------------------------------------------------------------------------------------------

      var thingList: List[Thing] = List(init, R2Booked, R4Booked, R5Booked, buildSpotBooked, buildPalette1Empty,
        buildPalette2Empty, buildingPaletteComplete, R2OPComplete, R4OPComplete, R5OPComplete, R4HoldingCube, R5HoldingCube,
        BuildingPalette1In, BuildingPalette2In,H1UpWithBuildPalette1,H1UpWithBuildPalette2,H2UpWithBuildPalette1,H2UpWithBuildPalette2,
        BuildPaletteIn,useTwoPalettes) ++ listOfPickedUpCubes ++ listOfPutDownCubes ++ listOfStatusBuildingPalettes ++ listOfCubesToPlaced

      val parserG = sp.domain.logic.PropositionParser(thingList)

      val parserA = sp.domain.logic.ActionParser(thingList)




      //Create gaurds-------------------------------------------------------------------------------------------------------

      //init guard
      val gInit = parserG.parseStr("init == true").right.get
      val gUseTwoPalettes = parserG.parseStr("useTwoPalettes == true").right.get
      //Guard for elivator 1
      val gH1UpWithBuildPalette1 = parserG.parseStr("H1UpWithBuildPalette1 == true").right.get
      val gH1UpWithBuildPalette2 = parserG.parseStr("H1UpWithBuildPalette2 == true").right.get
      //Guard for elivator 2
      val gH2OutWithBuildPalette1 = parserG.parseStr("H2UpWithBuildPalette1 == true").right.get
      val gH2OutWithBuildPalette2 = parserG.parseStr("H2UpWithBuildPalette1 == true").right.get
      //Guard for status of buildpalette
      val gBuildingPalette1In = parserG.parseStr("BuildingPalette1 == true").right.get
      val gBuildingPalette2In = parserG.parseStr("BuildingPalette1 == true").right.get
      val gBuildPaletteIn = parserG.parseStr("BuildingPalette == true").right.get
      //Guard for booked buildspot
      val gBuildSpotBooked =  parserG.parseStr("buildSpotBooked == true").right.get
      //Guards for robots holding cubes
      val gR4HoldingCube = parserG.parseStr("R4HoldingCube == true").right.get
      val gR5HoldingCube = parserG.parseStr("R5HoldingCube == true").right.get
      //Guards for  robotbookings
      val gR2Booked = parserG.parseStr("R2Booked == true").right.get
      val gR4Booked = parserG.parseStr("R4Booked == true").right.get
      val gR5Booked = parserG.parseStr("R5Booked == true").right.get
//      //Guards for Operation Completion
//      val gR2OPComplete = parserG.parseStr("R2OPComplete == true").right.get
//      val gR4OPComplete = parserG.parseStr("R4OPComplete == true").right.get
//      val gR5OPComplete = parserG.parseStr("R5OPComplete == true").right.get
      //Guards which tells when buildpalettes is empty
      val gBuildPalette1Empty = parserG.parseStr("buildPalette1Empty == true").right.get
      val gBuildPalette2Empty = parserG.parseStr("buildPalette2Empty == true").right.get
      //Guard for when buildningpalette is complete
      val gBuildingPaletteComplete = parserG.parseStr("buildingPaletteComplete == true").right.get
      //Guard which tells if cube is picked up from buildingspace
      val gListOfPickedUpCubes = for {
        r <- rs
        f <- 1 to fixtNo
        p <- 1 to posFix
      } yield {
        parserG.parseStr(s"$r$opNamePickedUpCubes$f$p == true").right.get
      }
      //Guard which tells if cube is placeed at buildingspot
      val gListOfPutDownCubes = for {
        f <- 1 to row
        p <- 1 to col
      } yield {
        parserG.parseStr(s"$opNamePutDownCubes$f$p == true").right.get
      }
      //Guards för buildingPalettes
      val gListOfStatusBuildingPalettes = for {
        f <- 1 to fixtNo
        r <- rs
        t <- 1 to 2
      } yield{
        parserG.parseStr(s"$palette$f$At$r$Space$t == true").right.get
      }
      val gCubesToBePlaced = "CubesToBePlaced"
      val gListOfCubesToPlaced = for {
        f <- 1 to row
        p <- 1 to col
      } yield {
        parserG.parseStr(s"$gCubesToBePlaced$f$p == true").right.get
      }

      //Create actions--------------------------------------------------------------------------------------------------------
      // example val aGenerateOperatorInstructions = parserA.parseStr("generateOperatorInstructions = True").right.get
      //
      //Actions for elivator 1 palette 1
      val aH1UpWithBuildPalette1True = parserA.parseStr("H1UpWithBuildPalette1 = true").right.get
      val aH1UpWithBuildPalette1False = parserA.parseStr("H1UpWithBuildPalette1 = false").right.get
      //Actions for elivator 1 palette 2
      val aH1UpWithBuildPalette2True = parserA.parseStr("H1UpWithBuildPalette2 = true").right.get
      val aH1UpWithBuildPalette2False = parserA.parseStr("H1UpWithBuildPalette2 = false").right.get
      //Actions for elivator 2 palette 1
      val aH2OutWithBuildPalette1True = parserA.parseStr("H2UpWithBuildPalette1 = true").right.get
      val aH2OutWithBuildPalette1False = parserA.parseStr("H2UpWithBuildPalette1 = false").right.get
      //Actions for elivator 2 palette 2
      val aH2OutWithBuildPalette2True = parserA.parseStr("H2UpWithBuildPalette2 = true").right.get
      val aH2OutWithBuildPalette2False = parserA.parseStr("H2UpWithBuildPalette2 = false").right.get
      //Actions for changing status of palettes - TRUE
      val aBuildingPalette1In = parserA.parseStr("BuildingPalette1In = true").right.get
      val aBuildingPalette2In = parserA.parseStr("BuildingPalette2In = true").right.get
      val aBuildPaletteIn = parserA.parseStr("BuildingPaletteIn = true").right.get
      //-----False
      val aBuildingPalette1Out = parserA.parseStr("BuildingPalette1In = false").right.get
      val aBuildingPalette2Out = parserA.parseStr("BuildingPalette2In = false").right.get
      val aBuildPaletteOut = parserA.parseStr("BuildingPaletteIn = false").right.get
      //init guard
      val aInit = parserA.parseStr("init = true").right.get
      val aInitDone = parserA.parseStr("init = false").right.get
      val aUseTwoPalettesTrue = parserA.parseStr("useTwoPalettes = true").right.get
      val aUseTwoPalettesFalse = parserA.parseStr("useTwoPalettes = false").right.get
      //Actions for booking robots
      val aBookR2 = parserA.parseStr("R2Booked = true").right.get
      val aBookR4 = parserA.parseStr("R4Booked = true").right.get
      val aBookR5 = parserA.parseStr("R5Booked = true").right.get
      //Actions for unbooking robots
      val aUnBookR2 = parserA.parseStr("R2Booked = false").right.get
      val aUnBookR4 = parserA.parseStr("R4Booked = false").right.get
      val aUnBookR5 = parserA.parseStr("R5Booked = false").right.get
      //Action which book Buildspot
      val aBuildSpotBook = parserA.parseStr("buildSpotBooked = true").right.get
      //Action which unbook buildspot
      val aBuildSpotUnBook = parserA.parseStr("buildSpotBooked = false").right.get
      //Change status of bulding palettes
      val aNewBuildingPaletteComplete = parserA.parseStr("buildingPaletteComplete = false").right.get
      val aBuildingPaletteIsComplete = parserA.parseStr("buildingPaletteComplete = true").right.get
      //Actions which indicate if R4 or R5 is holding a cube
      val aR4HoldingCube = parserA.parseStr("R4HoldingCube = true").right.get
      val aR5HoldingCube = parserA.parseStr("R5HoldingCube = true").right.get
      //Actions which indicate if R4 or R5 is not holding a cube
      val aR4NotHoldingCube = parserA.parseStr("R4HoldingCube = false").right.get
      val aR5NotHoldingCube = parserA.parseStr("R5HoldingCube = false").right.get
      //Actions which tells when buildpalettes is not empty
      val aBuildPalette1Empty = parserA.parseStr("buildPalette1Empty = true").right.get
      val aBuildPalette2Empty = parserA.parseStr("buildPalette2Empty = true").right.get
      //Actions which tells when buildpalettes is empty
      val aBuildPalette1NotEmpty = parserA.parseStr("buildPalette1Empty = false").right.get
      val aBuildPalette2NotEmpty = parserA.parseStr("buildPalette2Empty = false").right.get
      //Action which tells if cube is picked up from buildingspace
      val aListOfPickedUpCubesTrue = for {
        r <- rs
        f <- 1 to fixtNo
        p <- 1 to posFix
      } yield {
        parserA.parseStr(s"$r$opNamePickedUpCubes$f$p = true").right.get
      }
      val aListOfPickedUpCubesFalse = for {
        r <- rs
        f <- 1 to fixtNo
        p <- 1 to posFix
      } yield {
        parserA.parseStr(s"$r$opNamePickedUpCubes$f$p = false").right.get
      }
      //Action which tells if cube is placeed at buildingspot
      val aListOfPutDownCubesTrue = for {
        f <- 1 to row
        p <- 1 to col
      } yield {
        parserA.parseStr(s"$opNamePutDownCubes$f$p = true").right.get
      }
      val aListOfPutDownCubesFalse = for {
        f <- 1 to row
        p <- 1 to col
      } yield {
        parserA.parseStr(s"$opNamePutDownCubes$f$p = false").right.get
      }
      //Actionss för buildingPalettes
      val aChangeStatusBuildingPalettesTrue = for {
        f <- 1 to fixtNo
        r <- rs
        t <- 1 to 2
      } yield{
        parserA.parseStr(s"$palette$f$At$r$Space$t = true").right.get
      }
      //Actionss för buildingPalettes
      val aChangeStatusBuildingPalettesFalse = for {
        f <- 1 to fixtNo
        r <- rs
        t <- 1 to 2
      } yield{
        parserA.parseStr(s"$palette$f$At$r$Space$t = false").right.get
      }
      val aCubesToBePlaced = "cubesToBePlaced"
      val aListOfCubesToPlacedTrue = for {
        f <- 1 to row
        p <- 1 to col
      } yield {
        parserA.parseStr(s"$aCubesToBePlaced$f$p = true").right.get
      }
      val aListOfCubesToPlacedFalse = for {
        f <- 1 to row
        p <- 1 to col
      } yield {
        parserA.parseStr(s"$aCubesToBePlaced$f$p = false").right.get
      }

      //Operations----------------------------------------------------------------------------------------------------------

      //Example
      // val init = Operation("Init", List(PropositionCondition(AND(List()), List(aGenerateOperatorInstructions))),SPAttributes(), ID.newID)
      //init OP
      val OInitOperation = Operation("initOperation",List(PropositionCondition(AND(List(gInit)),List(aInit,aBuildPaletteOut,aBuildingPalette1Out,
        aUnBookR2,aUnBookR4,aUnBookR5,aBuildPalette1NotEmpty,aBuildPalette2NotEmpty,aH1UpWithBuildPalette1False,aH1UpWithBuildPalette2False,
        aH2OutWithBuildPalette1False,aH2OutWithBuildPalette2False,aBuildSpotUnBook,aNewBuildingPaletteComplete,aR4NotHoldingCube,
        aR5NotHoldingCube,aUseTwoPalettesFalse
      )++aListOfPutDownCubesFalse++aListOfPickedUpCubesFalse++aListOfCubesToPlacedFalse++aChangeStatusBuildingPalettesFalse)))
      //use two pallets
      //val OUseTwoPallets = Operation( "OUseTwoPallets",PropositionCondition(AND(List(AlwaysTrue)),List(aUseTwoPalettesTrue)))
      //operator Ops
      val OMoveInBuildingPalette1 = Operation("OMoveInBuildingPalette1", List(PropositionCondition(AND(List(NOT(gBuildingPalette1In),gInit)),List(aBuildingPalette1In,aInitDone))))
      val OMoveInBuildingPalette2 = Operation("OMoveInBuildingPalette1", List(PropositionCondition(AND(List(NOT(gBuildingPalette1In),gUseTwoPalettes)),List(aBuildingPalette2In))))
      val OMoveInBuildPalette = Operation("OMoveInBuildPalette", List(PropositionCondition(AND(List(gBuildPaletteIn)),List(aBuildPaletteIn))))
      //Elevator 1 Operations
      val OMoveUpPalette1WithElevator1 = Operation("OMoveUpPalette1WithElevator1",List(PropositionCondition(AND(List(gBuildingPalette1In)),List(aH1UpWithBuildPalette1True,aBuildingPalette1Out))))
      val OMoveDownPalette1WithElevator1 = Operation("OMoveDownPalette1WithElevator1",List(PropositionCondition(AND(List(gH1UpWithBuildPalette1,OR(List(gListOfStatusBuildingPalettes(0),gListOfStatusBuildingPalettes(1),gListOfStatusBuildingPalettes(2),gListOfStatusBuildingPalettes(3))))),List(aH1UpWithBuildPalette1False))))
      val OMoveUpPalette2WithElevator1 = Operation("OMoveUpPalette2WithElevator1",List(PropositionCondition(AND(List(gBuildingPalette2In)),List(aH1UpWithBuildPalette2True,aBuildingPalette2Out))))
      val OMoveDownPalette2WithElevator1 = Operation("OMoveDownPalette2WithElevator1",List(PropositionCondition(AND(List(gH1UpWithBuildPalette2,OR(List(gListOfStatusBuildingPalettes(4),gListOfStatusBuildingPalettes(5),gListOfStatusBuildingPalettes(6),gListOfStatusBuildingPalettes(7))))),List(aH1UpWithBuildPalette2False))))
      //R2 Operations
      val OR2Palette1ToR4Space1 = Operation("OR2Palette1ToR4Space1", List(PropositionCondition(AND(List(NOT(gListOfStatusBuildingPalettes(0)),NOT(gR2Booked),NOT(gR4Booked),gH1UpWithBuildPalette1)), List(aBookR2,aChangeStatusBuildingPalettesTrue(0)))))
      // (Pos1 clear) Action R2 Booked = True
      val OR2Palette1ToR4Space2 = Operation("OR2Palette1ToR4Space2", List(PropositionCondition(AND(List(NOT(gListOfStatusBuildingPalettes(1)),gListOfStatusBuildingPalettes(0),NOT(gR2Booked),NOT(gR4Booked),gH1UpWithBuildPalette1)), List(aBookR2,aChangeStatusBuildingPalettesTrue(1)))))
      // (Pos2 clear AND POS1 filled)
      val OR2Palette1ToR5Space1 = Operation("OR2Palette1ToR5Space1", List(PropositionCondition(AND(List(NOT(gListOfStatusBuildingPalettes(2)),NOT(gR2Booked),NOT(gR4Booked),gH1UpWithBuildPalette1)), List(aBookR2,aChangeStatusBuildingPalettesTrue(2)))))
      // (Pos1 clear)
      val OR2Palette1ToR5Space2 = Operation("OR2Palette1ToR5Space2", List(PropositionCondition(AND(List(NOT(gListOfStatusBuildingPalettes(3)),gListOfStatusBuildingPalettes(2),NOT(gR2Booked),NOT(gR4Booked),gH1UpWithBuildPalette1)), List(aBookR2,aChangeStatusBuildingPalettesTrue(3)))))
      // (Pos2 clear AND POS1 filled)
      val OR2Palette2ToR4Space1 = Operation("OR2Palette2ToR4Space1", List(PropositionCondition(AND(List(NOT(gListOfStatusBuildingPalettes(4)),NOT(gR2Booked),NOT(gR4Booked),gH1UpWithBuildPalette1)), List(aBookR2,aChangeStatusBuildingPalettesTrue(4)))))
      // (Pos1 clear) Action R2 Booked = True
      val OR2Palette2ToR4Space2 = Operation("OR2Palette2ToR4Space2", List(PropositionCondition(AND(List(NOT(gListOfStatusBuildingPalettes(5)),gListOfStatusBuildingPalettes(4),NOT(gR2Booked),NOT(gR4Booked),gH1UpWithBuildPalette1)), List(aBookR2,aChangeStatusBuildingPalettesTrue(5)))))
      // (Pos2 clear AND POS1 filled)
      val OR2Palette2ToR5Space1 = Operation("OR2Palette2ToR5Space1", List(PropositionCondition(AND(List(NOT(gListOfStatusBuildingPalettes(6)),NOT(gR2Booked),NOT(gR4Booked),gH1UpWithBuildPalette1)), List(aBookR2,aChangeStatusBuildingPalettesTrue(6)))))
      // (Pos1 clear)
      val OR2Palette2ToR5Space2 = Operation("OR2Palette2ToR5Space2", List(PropositionCondition(AND(List(NOT(gListOfStatusBuildingPalettes(7)),gListOfStatusBuildingPalettes(6),NOT(gR2Booked),NOT(gR4Booked),gH1UpWithBuildPalette1)), List(aBookR2,aChangeStatusBuildingPalettesTrue(7)))))
      // (Pos2 clear AND POS1 filled)
      val OR2PlaceBuildingPalette = Operation("OR2PlaceBuildingPalette", List(PropositionCondition(AND(List(gBuildPaletteIn,NOT(gR2Booked),NOT(gR4Booked),NOT(gR5Booked),NOT(gBuildSpotBooked))), List(aBookR2,aBuildSpotBook))))
      //
      val OR2RemoveBooking = Operation("OR2RemoveBooking", List(PropositionCondition(AND(List(gR2Booked)), List(aUnBookR2))))
      // After operations that books R2
      val OR4RemoveBooking = Operation("OR2RemoveBooking", List(PropositionCondition(AND(List(gR4Booked)), List(aUnBookR4))))
      // After operations that books R4
      val OR5RemoveBooking = Operation("OR2RemoveBooking", List(PropositionCondition(AND(List(gR5Booked)), List(aUnBookR5))))
      // After operations that books R5
      //operationerna nedan skall ändras så att de passar bättre och blir sumerade

      //Bygg en rad i taget och lägg in dodge och lagg in r4 plocka från 3,4 och gör om placeringar till 1,2,3,4 istället för 1,2


      //Wallschem ops
      val OWallSchemeOps = "OWallSchemeOps"
      val listOfWallSchemeOps = for{
        e <- 1 to 16 // 0 to 15 in list
      } yield{
        Operation (s"$OWallSchemeOps$e",List(PropositionCondition(AND(List(AlwaysTrue)),List(aListOfCubesToPlacedTrue(e-1)))))
      }
      //OPs for picking up cubes by R4 at space 1, row 1
      val OR4PickUpAt = "OR4PickUpAt"
      val OListR4PickUpAt11To14 = for {
        e <- 11 to 14 // 0 to 3 in list
      } yield {
        Operation(s"$OR4PickUpAt$e",List(PropositionCondition(AND(List(NOT(gR2Booked),NOT(gR5Booked),NOT(gR4Booked),OR(List(gListOfStatusBuildingPalettes(0),gListOfStatusBuildingPalettes(4))),gListOfCubesToPlaced(e-11))), List(aBookR4,aR4HoldingCube,aListOfPickedUpCubesTrue(e-11)))),SPAttributes("duration" -> 5))
      }
      //OPs for picking up cubes by R4 at space 1, row 2
      val OListR4PickUpAt15To18 = for {
        e <- 15 to 18 // 4 to 7 in list
      } yield {
        Operation(s"$OR4PickUpAt$e",List(PropositionCondition(AND(List(NOT(gR2Booked),NOT(gR5Booked),NOT(gR4Booked),OR(List(gListOfStatusBuildingPalettes(0),gListOfStatusBuildingPalettes(4))),gListOfCubesToPlaced(e-11),gListOfPickedUpCubes(e-4-11))), List(aBookR4,aR4HoldingCube,aListOfPickedUpCubesTrue(e-11)))),SPAttributes("duration" -> 5))
      }
      //OPs for picking up cubes by R4 at space 2, row 3 and 4
      val OListR4PickUpAt21To28 = for {
        e <- 21 to 28 // 8 to 15 in list
      } yield {
        Operation(s"$OR4PickUpAt$e",List(PropositionCondition(AND(List(NOT(gR2Booked),NOT(gR5Booked),NOT(gR4Booked),OR(List(gListOfStatusBuildingPalettes(1),gListOfStatusBuildingPalettes(5))),gListOfCubesToPlaced(e-13),gListOfPickedUpCubes(e-4-13))), List(aBookR4,aR4HoldingCube,aListOfPickedUpCubesTrue(e-13)))),SPAttributes("duration" -> 5))
      }
      //OPs for picking up cubes by R5 at space 1, row 1
      val OR5PickUpAt = "OR5PickUpAt"
      val OListR5PickUpAt11To14 = for {
        e <- 31 to 34 // 16 to 19 in list
      } yield {
        Operation(s"$OR5PickUpAt$e",List(PropositionCondition(AND(List(NOT(gR2Booked),NOT(gR5Booked),NOT(gR4Booked),OR(List(gListOfStatusBuildingPalettes(2),gListOfStatusBuildingPalettes(6))),gListOfCubesToPlaced(e-31))), List(aBookR5,aR5HoldingCube,aListOfPickedUpCubesTrue(e-15)))),SPAttributes("duration" -> 5))
      }
      //OPs for picking up cubes by R5 t space 1, row 2
      val OListR5PickUpAt15To18 = for {
        e <- 35 to 38 // 20 to 23 in list
      } yield {
        Operation(s"$OR5PickUpAt$e",List(PropositionCondition(AND(List(NOT(gR2Booked),NOT(gR5Booked),NOT(gR4Booked),OR(List(gListOfStatusBuildingPalettes(2),gListOfStatusBuildingPalettes(6))),gListOfCubesToPlaced(e-31),gListOfPickedUpCubes(e-19))), List(aBookR5,aR5HoldingCube,aListOfPickedUpCubesTrue(e-15)))),SPAttributes("duration" -> 5))
      }
      //OPs for picking up cubes by R5 t space 2, row 3 and 4
            val OListR5PickUpAt21To28 = for {
        e <- 41 to 48 // 24 to 31 in list
      } yield {
        Operation(s"$OR5PickUpAt$e",List(PropositionCondition(AND(List(NOT(gR2Booked),NOT(gR5Booked),NOT(gR4Booked),OR(List(gListOfStatusBuildingPalettes(3),gListOfStatusBuildingPalettes(7))),gListOfCubesToPlaced(e-33),gListOfPickedUpCubes(e-21))), List(aBookR5,aR5HoldingCube,aListOfPickedUpCubesTrue(e-33)))),SPAttributes("duration" -> 5))
      }

      //OPs for placing cubes with R4 11 - 14
      val OR4PlaceCubeAt = "OR4PlaceCubeAt"
      val OListR4PlaceCubeAt11To14 = for {
        e <- 11 to 14// 0 to 3 in list
      } yield {
        Operation(s"$OR4PlaceCubeAt$e",List(PropositionCondition(AND(List(NOT(gR4Booked),gR4HoldingCube,gListOfPickedUpCubes(e-11),NOT(gListOfPutDownCubes(e-11)))),List(aListOfPutDownCubesTrue(e-11),aR4NotHoldingCube))),SPAttributes("duration" -> 5))
      }
      //OPs for placing cubes with R4 21 - 24
      val OListR4PlaceCubeAt21To24 = for {
        e <- 21 to 24 // 4 to 7 in list
      } yield {
        Operation(s"$OR4PlaceCubeAt$e",List(PropositionCondition(AND(List(NOT(gR4Booked),gR4HoldingCube,gListOfPickedUpCubes(e-14),NOT(gListOfPutDownCubes(e-14)))),List(aListOfPutDownCubesTrue(e-14),aR4NotHoldingCube))),SPAttributes("duration" -> 5))
      }
      //OPs for placing cubes with R4 31 - 34
      val OListR4PlaceCubeAt31To34 = for {
        e <- 31 to 34 // 8 to 11 in list
      } yield {
        Operation(s"$OR4PlaceCubeAt$e",List(PropositionCondition(AND(List(NOT(gR4Booked),gR4HoldingCube,gListOfPickedUpCubes(e-23),NOT(gListOfPutDownCubes(e-23)))),List(aListOfPutDownCubesTrue(e-23),aR4NotHoldingCube))),SPAttributes("duration" -> 5))
      }
      val OListR4PlaceCubeAt41To44 = for {
        e <- 41 to 44 // 12 to 15 in list
      } yield {
        Operation(s"$OR4PlaceCubeAt$e",List(PropositionCondition(AND(List(NOT(gR4Booked),gR4HoldingCube,gListOfPickedUpCubes(e-29),NOT(gListOfPutDownCubes(e-29)))),List(aListOfPutDownCubesTrue(e-29),aR4NotHoldingCube))),SPAttributes("duration" -> 5))
      }

      //OPs for placing cubes with R5 11 - 14
      val OR5PlaceCubeAt = "OR5PlaceCubeAt"
      val OListR5PlaceCubeAt11To14 = for {
        e <- 11 to 14
      } yield {
        Operation(s"$OR5PlaceCubeAt$e",List(PropositionCondition(AND(List(NOT(gR5Booked),gR5HoldingCube,gListOfPickedUpCubes(e+5),NOT(gListOfPutDownCubes(e-11)))),List(aListOfPutDownCubesTrue(e-11),aR5NotHoldingCube))),SPAttributes("duration" -> 5))
      }
      //OPs for placing cubes with R5 21 - 24
      val OListR5PlaceCubeAt21To24 = for {
        e <- 21 to 24
      } yield {
        Operation(s"$OR5PlaceCubeAt$e",List(PropositionCondition(AND(List(NOT(gR5Booked),gR5HoldingCube,gListOfPickedUpCubes(e-1),NOT(gListOfPutDownCubes(e-17)))),List(aListOfPutDownCubesTrue(e-17),aR5NotHoldingCube))),SPAttributes("duration" -> 5))
      }
      //OPs for placing cubes with R5 31 - 44
      val OListR5PlaceCubeAt31To34 = for {
        e <- 31 to 34
      } yield {
        Operation(s"$OR5PlaceCubeAt$e",List(PropositionCondition(AND(List(NOT(gR5Booked),gR5HoldingCube,gListOfPickedUpCubes(e-7),NOT(gListOfPutDownCubes(e-23)))),List(aListOfPutDownCubesTrue(e-23),aR5NotHoldingCube))),SPAttributes("duration" -> 5))
      }
      val OListR5PlaceCubeAt41To44 = for {
        e <- 41 to 44
      } yield {
        Operation(s"$OR5PlaceCubeAt$e",List(PropositionCondition(AND(List(NOT(gR5Booked),gR5HoldingCube,gListOfPickedUpCubes(e-13),NOT(gListOfPutDownCubes(e-29)))),List(aListOfPutDownCubesTrue(e-29),aR5NotHoldingCube))),SPAttributes("duration" -> 5))
      }

      //Operation which tells when towers is comeplete
      // use XNOR for all 16 pos
      val OBuildingPaletteComplete = Operation("OBuildingPaletteComplete",List(PropositionCondition(AND(List(gListOfPutDownCubes(0),gListOfCubesToPlaced(0))),List(aBuildingPaletteIsComplete,aBuildPalette1Empty,aBuildPalette2Empty))))
      // inrcement finns i propsistion condition och göra tester

      //Elevator 2 Operations
      val OMoveUpPalette1WithElevator2 = Operation("OMoveUpPalette1WithElevator1",List(PropositionCondition(AND(List(gBuildPalette1Empty)),List(aH2OutWithBuildPalette1True))))
      val OMoveUpPalette2WithElevator2 = Operation("OMoveUpPalette2WithElevator1",List(PropositionCondition(AND(List(gBuildPalette2Empty)),List(aH2OutWithBuildPalette2True))))

      //Remove building palettes ops
      val OR2Palette1RemoveR4Space1 = Operation("OR2Palette1RemoveR4Space1", List(PropositionCondition(AND(List(NOT(gR2Booked),gListOfStatusBuildingPalettes(0),gH2OutWithBuildPalette1,NOT(gBuildSpotBooked))), List(aBookR2,aChangeStatusBuildingPalettesFalse(0)))))
      // Operation R4BuildFromPos1 Done
      val OR2Palette1RemoveR4Space2 = Operation("OR2Palette1RemoveR4Space2", List(PropositionCondition(AND(List(NOT(gR2Booked),gListOfStatusBuildingPalettes(1),gH2OutWithBuildPalette1,NOT(gBuildSpotBooked))), List(aBookR2,aChangeStatusBuildingPalettesFalse(1)))))
      // Operation R4BuildFromPos2 Done
      val OR2Palette1RemoveR5Space1 = Operation("OR2Palette1RemoveR5Space1", List(PropositionCondition(AND(List(NOT(gR2Booked),gListOfStatusBuildingPalettes(2),gH2OutWithBuildPalette1,NOT(gBuildSpotBooked))), List(aBookR2,aChangeStatusBuildingPalettesFalse(2)))))
      // Operation R5BuildFromPos1 Done
      val OR2Palette1RemoveR5Space2 = Operation("OR2Palette1RemoveR5Space2", List(PropositionCondition(AND(List(NOT(gR2Booked),gListOfStatusBuildingPalettes(3),gH2OutWithBuildPalette1,NOT(gBuildSpotBooked))), List(aBookR2,aChangeStatusBuildingPalettesFalse(3)))))
      // Operation R5BuildFromPos2 Done
      val OR2Palette2RemoveR4Space1 = Operation("OR2Palette2RemoveR4Space1", List(PropositionCondition(AND(List(NOT(gR2Booked),gListOfStatusBuildingPalettes(4),gH2OutWithBuildPalette2,NOT(gBuildSpotBooked))), List(aBookR2,aChangeStatusBuildingPalettesFalse(4)))))
      // Operation R4BuildFromPos1 Done
      val OR2Palette2RemoveR4Space2 = Operation("OR2Palette2RemoveR4Space2", List(PropositionCondition(AND(List(NOT(gR2Booked),gListOfStatusBuildingPalettes(5),gH2OutWithBuildPalette2,NOT(gBuildSpotBooked))), List(aBookR2,aChangeStatusBuildingPalettesFalse(5)))))
      // Operation R4BuialdFromPos2 Done
      val OR2Palette2RemoveR5Space1 = Operation("OR2Palette2RemoveR5Space1", List(PropositionCondition(AND(List(NOT(gR2Booked),gListOfStatusBuildingPalettes(6),gH2OutWithBuildPalette2,NOT(gBuildSpotBooked))), List(aBookR2,aChangeStatusBuildingPalettesFalse(6)))))
      // Operation R5BuildFromPos1 Done
      val OR2Palette2RemoveR5Space2 = Operation("OR2Palette2RemoveR5Space2", List(PropositionCondition(AND(List(NOT(gR2Booked),gListOfStatusBuildingPalettes(7),gH2OutWithBuildPalette2,NOT(gBuildSpotBooked))), List(aBookR2,aChangeStatusBuildingPalettesFalse(7)))))
      //Op for removing complete tower
      val OR2RemoveBuildingPalette = Operation("OR2RemoveBuildingPalette", List(PropositionCondition(AND(List(NOT(gR2Booked),NOT(gR5Booked),NOT(gR4Booked),gBuildSpotBooked,gBuildingPaletteComplete)), List(aBookR2,aBuildSpotUnBook))))

      // Ops for moving lowering H2
      val OMoveOutPalette1WithElevator2 = Operation("OMoveUpPalette1WithElevator1",List(PropositionCondition(AND(List(gH2OutWithBuildPalette1,OR(List(gListOfStatusBuildingPalettes(0),gListOfStatusBuildingPalettes(1),gListOfStatusBuildingPalettes(2),gListOfStatusBuildingPalettes(3))))),List(aH2OutWithBuildPalette1False))))
      val OMoveOutPalette2WithElevator2 = Operation("OMoveUpPalette2WithElevator1",List(PropositionCondition(AND(List(gH2OutWithBuildPalette2,OR(List(gListOfStatusBuildingPalettes(4),gListOfStatusBuildingPalettes(5),gListOfStatusBuildingPalettes(6),gListOfStatusBuildingPalettes(7))))),List(aH2OutWithBuildPalette2False))))
      //LIST With all OPS

      val allOPs: List[Operation] = List(OMoveInBuildingPalette1,OMoveInBuildingPalette2,OMoveInBuildPalette,OR2Palette1ToR4Space1,
        OR2Palette1ToR4Space2,OR2Palette1ToR5Space1,OR2Palette1ToR5Space2,OR2Palette2ToR4Space1,OR2Palette2ToR4Space2,OR2Palette2ToR5Space1,
        OR2Palette2ToR5Space2,OMoveUpPalette1WithElevator1,OMoveDownPalette1WithElevator1,OMoveUpPalette2WithElevator1,OMoveDownPalette2WithElevator1,
        OR2PlaceBuildingPalette,OR2RemoveBooking,OR4RemoveBooking,OR5RemoveBooking,OMoveUpPalette1WithElevator2,OMoveUpPalette2WithElevator2,
        OR2Palette1RemoveR4Space1,OR2Palette1RemoveR4Space2,OR2Palette1RemoveR5Space1,OR2Palette1RemoveR5Space2,OR2Palette2RemoveR4Space1,
        OR2Palette2RemoveR4Space2,OR2Palette2RemoveR5Space1,OR2Palette2RemoveR5Space2,OR2RemoveBuildingPalette,OMoveOutPalette1WithElevator2,
        OMoveOutPalette2WithElevator2,OBuildingPaletteComplete
      )++OListR4PickUpAt11To14++OListR4PickUpAt15To18++OListR4PickUpAt21To28++OListR5PickUpAt11To14++OListR5PickUpAt15To18++OListR5PickUpAt21To28++OListR4PlaceCubeAt11To14++OListR4PlaceCubeAt21To24++OListR4PlaceCubeAt31To34++OListR4PlaceCubeAt41To44++OListR5PlaceCubeAt11To14++OListR5PlaceCubeAt21To24++OListR5PlaceCubeAt31To34++OListR5PlaceCubeAt41To44++listOfWallSchemeOps

      replyTo ! Response(thingList ++ allOPs ++ List(OInitOperation), SPAttributes(), rnr.req.service, rnr.req.reqID)
      self ! PoisonPill
    }
  }
}





//object operationMaker {

  // Valuerestrictions for the robots. 

/*
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
  */

  /*class operationMaker {
/*
    def receive = {
      case r@Request(service, attr, ids, reqID) => {
        val replyTo = sender()
        implicit val rnr = RequestNReply(r, replyTo)
        

    val attr = SPAttributes("command"->SPAttributes("commandType"->"execute", "execute"->id,
      "parameters" -> State(paraMap)))
    //val pickOperations = operationMaker.r4listOfPutDown
    val placeOperations = operationMaker.r4listOfPutDown

    val aSOP = Parallel(Sequence(pickOperations(0), placeOperations(0), pickOperations(1),placeOperations(1)))

//    val thaSOP = SOPSpec("thaSOP", List(aSOP), SPAttributes())

   // askAService(Request("RunnerService", aSOP, List(pickOperations, placeOperations), ID.makeID))

//  val thaSOP = SOPSpec("thaSOP", List(aSOP), List(op1...), SPAttributes())
       //mottagare ! meddelande
    // }
*/
}*/
//}


//       val root = HierarchyRoot("Resources", List(h2._1, h3._1, toOper._1, toRobo._1, R5._1, R4._1, R2._1, h1._1, h4._1, sensorIH2._1, HierarchyNode(sopSpec.id), HierarchyNode(thaSOP.id)))
