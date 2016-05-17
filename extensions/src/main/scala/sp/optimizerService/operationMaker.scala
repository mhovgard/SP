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
import com.typesafe.config._
import scala.concurrent.Future
import akka.util._
import akka.pattern.ask
import scala.concurrent._
import scala.concurrent.duration._
import sp.supremicaStuff.auxiliary.DESModelingSupport

object operationMaker extends SPService {
  val specification = SPAttributes(
    "service" -> SPAttributes(
      "group"->  "Modles",
      "description" -> "Makes model and SOP"
    )
  )
  val transformation: List[TransformValue[_]] = List()
  def props = ServiceLauncher.props(Props(classOf[operationMaker]))
}


class operationMaker extends Actor with ServiceSupport with DESModelingSupport {
  def receive = {
    case r@Request(service, attr, ids, reqID) => {
      val replyTo = sender()
      implicit val rnr = RequestNReply(r, replyTo)

      //inti
      val init1 = Thing("init1")
      val init2 = Thing("init2")
      val useTwoPalettes = Thing("useTwoPalettes")
      //For the buildingspace were the tower should be built
      val buildSpotBooked = Thing("buildSpotBooked")
      //Things for elivator 1
      val H1UpWithBuildPalette1 = Thing("H1UpWithBuildPalette1")
      val H1UpWithBuildPalette2 = Thing("H1UpWithBuildPalette2")
      //Things for elivator 2
      val H2UpWithBuildPalette1 = Thing("H2UpWithBuildPalette1")
      val H2UpWithBuildPalette2 = Thing("H2UpWithBuildPalette2")
      //Robot 4 dodge
      val R4Dodge = Thing("R4Dodge")
      //Robot 5 dodge
      val R5Dodge = Thing("R5Dodge")
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
      val BuildingPalette1In = Thing("BuildingPalette1In")
      val BuildingPalette2In = Thing("BuildingPalette2In")
      val BuildPaletteIn = Thing("BuildingPaletteIn")
      //Row 1 complete
      val Row1Complete = Thing("Row1Complete")
      //Row 2 complete
      val Row2Complete = Thing("Row2Complete")
      //Row 3 complete
      val Row3Complete = Thing("Row3Complete")

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
      } yield {
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

      val thingList: List[Thing] = List(init1, init2, R2Booked, R4Booked, R5Booked, buildSpotBooked, buildPalette1Empty, R5Dodge, R4Dodge,
        buildPalette2Empty, buildingPaletteComplete, R2OPComplete, R4OPComplete, R5OPComplete, R4HoldingCube, R5HoldingCube,
        BuildingPalette1In, BuildingPalette2In, H1UpWithBuildPalette1, H1UpWithBuildPalette2, H2UpWithBuildPalette1, H2UpWithBuildPalette2,
        BuildPaletteIn, useTwoPalettes, Row1Complete, Row2Complete, Row3Complete
      ) ++ listOfPickedUpCubes ++ listOfPutDownCubes ++ listOfStatusBuildingPalettes ++ listOfCubesToPlaced

      val parserG = sp.domain.logic.PropositionParser(thingList)

      val parserA = sp.domain.logic.ActionParser(thingList)




      //Create gaurds-------------------------------------------------------------------------------------------------------

      //init guard
      val gInit1 = parserG.parseStr("init1 == true").right.get
      val gInit2 = parserG.parseStr("init2 == true").right.get
      val gUseTwoPalettes = parserG.parseStr("useTwoPalettes == true").right.get
      //Guards for rows complete
      val gRow1Complete = parserG.parseStr("Row1Complete == true").right.get
      val gRow2Complete = parserG.parseStr("Row2Complete == true").right.get
      val gRow3Complete = parserG.parseStr("Row3Complete == true").right.get
      //Guard for elivator 1
      val gH1UpWithBuildPalette1 = parserG.parseStr("H1UpWithBuildPalette1 == true").right.get
      val gH1UpWithBuildPalette2 = parserG.parseStr("H1UpWithBuildPalette2 == true").right.get
      //Guard for R4 dodge
      val gR4Dodge = parserG.parseStr("R4Dodge == true").right.get
      //Guard for R5 dodge
      val gR5Dodge = parserG.parseStr("R5Dodge == true").right.get
      // Guard for elivator 2
      val gH2OutWithBuildPalette1 = parserG.parseStr("H2UpWithBuildPalette1 == true").right.get
      val gH2OutWithBuildPalette2 = parserG.parseStr("H2UpWithBuildPalette1 == true").right.get
      //Guard for status of buildpalette
      val gBuildingPalette1In = parserG.parseStr("BuildingPalette1 == true").right.get
      val gBuildingPalette2In = parserG.parseStr("BuildingPalette1 == true").right.get
      val gBuildPaletteIn = parserG.parseStr("BuildingPalette == true").right.get
      //Guard for booked buildspot
      val gBuildSpotBooked = parserG.parseStr("buildSpotBooked == true").right.get
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
      } yield {
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
      //Actons for rows complete True
      val aRow1CompleteTrue = parserA.parseStr("Row1Complete = true").right.get
      val aRow2CompleteTrue = parserA.parseStr("Row2Complete = true").right.get
      val aRow3CompleteTrue = parserA.parseStr("Row3Complete = true").right.get
      //Actons for rows complete Falsee
      val aRow1CompleteFalse = parserA.parseStr("Row1Complete = false").right.get
      val aRow2CompleteFalse = parserA.parseStr("Row2Complete = false").right.get
      val aRow3CompleteFalse = parserA.parseStr("Row3Complete = false").right.get
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
      //Action for R4 dodge
      val aR4DodgeTrue = parserA.parseStr("R4Dodge = true").right.get
      //val aR4DodgeFalse = parserA.parseStr("R4Dodge = false").right.get
      //Action for R5 dodge
      val aR5DodgeTrue = parserA.parseStr("R5Dodge = true").right.get
      //val aR5DodgeFalse = parserA.parseStr("R5Dodge = false").right.get
      //Actions for changing status of palettes - TRUE
      val aBuildingPalette1In = parserA.parseStr("BuildingPalette1In = true").right.get
      val aBuildingPalette2In = parserA.parseStr("BuildingPalette2In = true").right.get
      val aBuildPaletteIn = parserA.parseStr("BuildingPaletteIn = true").right.get
      //-----False
      val aBuildingPalette1Out = parserA.parseStr("BuildingPalette1In = false").right.get
      val aBuildingPalette2Out = parserA.parseStr("BuildingPalette2In = false").right.get
      val aBuildPaletteOut = parserA.parseStr("BuildingPaletteIn = false").right.get
      //init guard
      val aInit1 = parserA.parseStr("init1 = true").right.get
      val aInit2 = parserA.parseStr("init2 = true").right.get
      val aInit1Done = parserA.parseStr("init1 = false").right.get
      val aInit2Done = parserA.parseStr("init2 = false").right.get
      //val aUseTwoPalettesTrue = parserA.parseStr("useTwoPalettes = true").right.get
      //val aUseTwoPalettesFalse = parserA.parseStr("useTwoPalettes = false").right.get
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
      } yield {
        parserA.parseStr(s"$palette$f$At$r$Space$t = true").right.get
      }
      //Actionss för buildingPalettes
      val aChangeStatusBuildingPalettesFalse = for {
        f <- 1 to fixtNo
        r <- rs
        t <- 1 to 2
      } yield {
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
      val OInitOperation = Operation("initOperation", List(PropositionCondition(AND(List(gInit1)), List(aInit1Done, aBuildPaletteOut, aBuildingPalette1Out,
        aUnBookR2, aUnBookR4, aUnBookR5, aBuildPalette1NotEmpty, aBuildPalette2NotEmpty, aH1UpWithBuildPalette1False, aH1UpWithBuildPalette2False,
        aH2OutWithBuildPalette1False, aH2OutWithBuildPalette2False, aBuildSpotUnBook, aNewBuildingPaletteComplete, aR4NotHoldingCube,
        aR5NotHoldingCube, aRow1CompleteFalse, aRow2CompleteFalse, aRow3CompleteFalse, aInit2
      ) ++ aListOfPutDownCubesFalse ++ aListOfPickedUpCubesFalse ++ aListOfCubesToPlacedFalse ++ aChangeStatusBuildingPalettesFalse)), SPAttributes("duration" -> 0))
      //use two pallets
      //val OUseTwoPallets = Operation("OUseTwoPallets", List(PropositionCondition(AND(List(AlwaysTrue)), List(aUseTwoPalettesTrue))), SPAttributes("duration" -> 0))
      //Wallschem ops
      val OWallSchemeOps = "OWallSchemeOps"
      val listOfWallSchemeOps = for {
        e <- 1 to 16 // 0 to 15 in list
      } yield {
        Operation(s"$OWallSchemeOps$e", List(PropositionCondition(AND(List(AlwaysFalse)), List(aListOfCubesToPlacedTrue(e - 1)))), SPAttributes("duration" -> 0))
      }
      //Operator Ops
      val OMoveInBuildingPalette1 = Operation("OMoveInBuildingPalette1", List(PropositionCondition(AND(List(NOT(gBuildingPalette1In), NOT(gInit2))), List(aBuildingPalette1In))), SPAttributes("duration" -> 5))
      val OMoveInBuildingPalette2 = Operation("OMoveInBuildingPalette1", List(PropositionCondition(AND(List(NOT(gBuildingPalette2In), NOT(gInit2))), List(aBuildingPalette2In))), SPAttributes("duration" -> 5))
      val OMoveInBuildPalette = Operation("OMoveInBuildPalette", List(PropositionCondition(AND(List(gInit2)), List(aBuildPaletteIn, aInit2Done))), SPAttributes("duration" -> 0))
      //Elevator 1 Operations
      val OMoveUpPalette1WithElevator1 = Operation("OMoveUpPalette1WithElevator1", List(PropositionCondition(AND(List(gBuildingPalette1In)), List(aH1UpWithBuildPalette1True, aBuildingPalette1Out))), SPAttributes("duration" -> 5))
      val OMoveDownPalette1WithElevator1 = Operation("OMoveDownPalette1WithElevator1", List(PropositionCondition(AND(List(gH1UpWithBuildPalette1, OR(List(gListOfStatusBuildingPalettes(0), gListOfStatusBuildingPalettes(1), gListOfStatusBuildingPalettes(2), gListOfStatusBuildingPalettes(3))))), List(aH1UpWithBuildPalette1False))), SPAttributes("duration" -> 5))
      val OMoveUpPalette2WithElevator1 = Operation("OMoveUpPalette2WithElevator1", List(PropositionCondition(AND(List(gBuildingPalette2In)), List(aH1UpWithBuildPalette2True, aBuildingPalette2Out))), SPAttributes("duration" -> 5))
      val OMoveDownPalette2WithElevator1 = Operation("OMoveDownPalette2WithElevator1", List(PropositionCondition(AND(List(gH1UpWithBuildPalette2, OR(List(gListOfStatusBuildingPalettes(4), gListOfStatusBuildingPalettes(5), gListOfStatusBuildingPalettes(6), gListOfStatusBuildingPalettes(7))))), List(aH1UpWithBuildPalette2False))), SPAttributes("duration" -> 5))
      //R4 Dodge operations
      val OR4ToDodge = Operation("R4toDodge", List(PropositionCondition(OR(List(AND(List(OR(List(gH1UpWithBuildPalette1, gH1UpWithBuildPalette2)))), AND(List(gBuildingPaletteComplete)))), List(aR4DodgeTrue))), SPAttributes("duration" -> 3))
      //val OR4FromDodge = Operation("OR4FromDodge",List(PropositionCondition(OR(List(AND(List()),AND(List()))),List(aR4DodgeFalse,aUnBookR4))))
      //R5 Dodge operations
      val OR5ToDodge = Operation("R5toDodge", List(PropositionCondition(OR(List(AND(List(OR(List(gH1UpWithBuildPalette1, gH1UpWithBuildPalette2)))), AND(List(gBuildingPaletteComplete)))), List(aR5DodgeTrue))), SPAttributes("duration" -> 3))
      //val OR5FromDodge = Operation("OR5FromDodge",List(PropositionCondition(OR(List(AND(List()),AND(List()))),List(aR5DodgeFalse,aUnBookR2))))
      //R2 Operations
      val OR2Palette1ToR4Space1 = Operation("OR2Palette1ToR4Space1", List(PropositionCondition(AND(List(gR4Dodge, NOT(gListOfStatusBuildingPalettes(0)), NOT(gR2Booked), NOT(gR4Booked), gH1UpWithBuildPalette1)), List(aBookR2, aChangeStatusBuildingPalettesTrue(0))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      // (Pos1 clear) Action R2 Booked = True
      val OR2Palette1ToR4Space2 = Operation("OR2Palette1ToR4Space2", List(PropositionCondition(AND(List(gR4Dodge, NOT(gListOfStatusBuildingPalettes(1)), gListOfStatusBuildingPalettes(0), NOT(gR2Booked), NOT(gR4Booked), gH1UpWithBuildPalette1)), List(aBookR2, aChangeStatusBuildingPalettesTrue(1))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      // (Pos2 clear AND POS1 filled)
      val OR2Palette1ToR5Space1 = Operation("OR2Palette1ToR5Space1", List(PropositionCondition(AND(List(gR4Dodge, gR5Dodge, NOT(gListOfStatusBuildingPalettes(2)), NOT(gR2Booked), NOT(gR4Booked), gH1UpWithBuildPalette1)), List(aBookR2, aChangeStatusBuildingPalettesTrue(2))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      // (Pos1 clear)
      val OR2Palette1ToR5Space2 = Operation("OR2Palette1ToR5Space2", List(PropositionCondition(AND(List(gR4Dodge, gR5Dodge, NOT(gListOfStatusBuildingPalettes(3)), gListOfStatusBuildingPalettes(2), NOT(gR2Booked), NOT(gR4Booked), gH1UpWithBuildPalette1)), List(aBookR2, aChangeStatusBuildingPalettesTrue(3))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      // (Pos2 clear AND POS1 filled)
      val OR2Palette2ToR4Space1 = Operation("OR2Palette2ToR4Space1", List(PropositionCondition(AND(List(gR4Dodge, NOT(gListOfStatusBuildingPalettes(4)), NOT(gR2Booked), NOT(gR4Booked), gH1UpWithBuildPalette2)), List(aBookR2, aChangeStatusBuildingPalettesTrue(4))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      // (Pos1 clear) Action R2 Booked = True
      val OR2Palette2ToR4Space2 = Operation("OR2Palette2ToR4Space2", List(PropositionCondition(AND(List(gR4Dodge, NOT(gListOfStatusBuildingPalettes(5)), gListOfStatusBuildingPalettes(4), NOT(gR2Booked), NOT(gR4Booked), gH1UpWithBuildPalette2)), List(aBookR2, aChangeStatusBuildingPalettesTrue(5))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      // (Pos2 clear AND POS1 filled)
      val OR2Palette2ToR5Space1 = Operation("OR2Palette2ToR5Space1", List(PropositionCondition(AND(List(gR4Dodge, gR5Dodge, NOT(gListOfStatusBuildingPalettes(6)), NOT(gR2Booked), NOT(gR4Booked), gH1UpWithBuildPalette2)), List(aBookR2, aChangeStatusBuildingPalettesTrue(6))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      // (Pos1 clear)
      val OR2Palette2ToR5Space2 = Operation("OR2Palette2ToR5Space2", List(PropositionCondition(AND(List(gR4Dodge, gR5Dodge, NOT(gListOfStatusBuildingPalettes(7)), gListOfStatusBuildingPalettes(6), NOT(gR2Booked), NOT(gR4Booked), gH1UpWithBuildPalette2)), List(aBookR2, aChangeStatusBuildingPalettesTrue(7))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      // (Pos2 clear AND POS1 filled)
      val OR2PlaceBuildingPalette = Operation("OR2PlaceBuildingPalette", List(PropositionCondition(AND(List(gR4Dodge, gR5Dodge, gBuildPaletteIn, NOT(gR2Booked), NOT(gR4Booked), NOT(gR5Booked), NOT(gBuildSpotBooked))), List(aBookR2, aBuildSpotBook)),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      //old booking ops
      //val OR2RemoveBooking = Operation("OR2RemoveBooking", List(PropositionCondition(AND(List(gR2Booked)), List(aUnBookR2))), SPAttributes("duration" -> 0))
      // After operations that books R2
      val OR4RemoveBooking = Operation("OR2RemoveBooking", List(PropositionCondition(AND(List(gR4Booked,NOT(gR4HoldingCube))), List(aUnBookR4))), SPAttributes("duration" -> 0))
      // After operations that books R4
      val OR5RemoveBooking = Operation("OR2RemoveBooking", List(PropositionCondition(AND(List(gR5Booked,NOT(gR5HoldingCube))), List(aUnBookR5))), SPAttributes("duration" -> 0))
      // After operations that books R5
      //operationerna nedan skall ändras så att de passar bättre och blir sumerade

      //Bygg en rad i taget och lägg in dodge och lagg in r4 plocka från 3,4 och gör om placeringar till 1,2,3,4 istället för 1,2


      //OPs for picking up cubes by R4 at space 1, row 1
      val OR4PickUpAt = "R4pickCube"
      val OListR4PickUpAt11To14 = for {
        e <- 11 to 14 // 0 to 3 in list
      } yield {
        Operation(s"$OR4PickUpAt$e", List(PropositionCondition(AND(List(NOT(gR2Booked), NOT(gR5Booked), NOT(gR4Booked), OR(List(gListOfStatusBuildingPalettes(0), gListOfStatusBuildingPalettes(4))), gListOfCubesToPlaced(e - 11))), List(aBookR4, aR4HoldingCube, aListOfPickedUpCubesTrue(e - 11)))), SPAttributes("duration" -> 4))
      }
      //OPs for picking up cubes by R4 at space 1, row 2
      val OListR4PickUpAt15To18 = for {
        e <- 15 to 18 // 4 to 7 in list
      } yield {
        Operation(s"$OR4PickUpAt$e", List(PropositionCondition(AND(List(gRow1Complete, NOT(gR2Booked), NOT(gR5Booked), NOT(gR4Booked), OR(List(gListOfStatusBuildingPalettes(0), gListOfStatusBuildingPalettes(4))), gListOfCubesToPlaced(e - 11), gListOfPickedUpCubes(e - 4 - 11))), List(aBookR4, aR4HoldingCube, aListOfPickedUpCubesTrue(e - 11)))), SPAttributes("duration" -> 4))
      }
      //OPs for picking up cubes by R4 at space 2, row 4 and 3
      val OListR4PickUpAt21To24 = for {
        e <- 21 to 24 // 8 to 15 in list
      } yield {
        Operation(s"$OR4PickUpAt$e", List(PropositionCondition(AND(List(gRow2Complete, NOT(gR2Booked), NOT(gR5Booked), NOT(gR4Booked), OR(List(gListOfStatusBuildingPalettes(1), gListOfStatusBuildingPalettes(5))), gListOfCubesToPlaced(e - 13), gListOfPickedUpCubes(e - 4 - 13))), List(aBookR4, aR4HoldingCube, aListOfPickedUpCubesTrue(e - 13)))), SPAttributes("duration" -> 4))
      }
      val OListR4PickUpAt25To28 = for {
        e <- 25 to 28 // 8 to 15 in list
      } yield {
        Operation(s"$OR4PickUpAt$e", List(PropositionCondition(AND(List(gRow3Complete, NOT(gR2Booked), NOT(gR5Booked), NOT(gR4Booked), OR(List(gListOfStatusBuildingPalettes(1), gListOfStatusBuildingPalettes(5))), gListOfCubesToPlaced(e - 13), gListOfPickedUpCubes(e - 4 - 13))), List(aBookR4, aR4HoldingCube, aListOfPickedUpCubesTrue(e - 13)))), SPAttributes("duration" -> 4))
      }
      val OListR4PickUpAt31To34 = for {
        e <- 31 to 34 // 16 to 19 in list
      } yield {
        Operation(s"$OR4PickUpAt$e", List(PropositionCondition(AND(List(NOT(gR2Booked), NOT(gR5Booked), NOT(gR4Booked), OR(List(gListOfStatusBuildingPalettes(2), gListOfStatusBuildingPalettes(6))), gListOfCubesToPlaced(e - 31))), List(aBookR4, aR4HoldingCube, aListOfPickedUpCubesTrue(e - 15)))), SPAttributes("duration" -> 4))
      }
      //OPs for picking up cubes by R5 t space 1, row 2
      val OListR4PickUpAt35To38 = for {
        e <- 35 to 38 // 20 to 23 in list
      } yield {
        Operation(s"$OR4PickUpAt$e", List(PropositionCondition(AND(List(gRow1Complete, NOT(gR2Booked), NOT(gR5Booked), NOT(gR4Booked), OR(List(gListOfStatusBuildingPalettes(2), gListOfStatusBuildingPalettes(6))), gListOfCubesToPlaced(e - 31), gListOfPickedUpCubes(e - 19))), List(aBookR4, aR4HoldingCube, aListOfPickedUpCubesTrue(e - 15)))), SPAttributes("duration" -> 4))
      }
      //OPs for picking up cubes by R5 t space 2, row 3 and 4
      val OListR4PickUpAt41To44 = for {
        e <- 41 to 44 // 24 to 31 in list
      } yield {
        Operation(s"$OR4PickUpAt$e", List(PropositionCondition(AND(List(gRow2Complete, NOT(gR2Booked), NOT(gR5Booked), NOT(gR4Booked), OR(List(gListOfStatusBuildingPalettes(3), gListOfStatusBuildingPalettes(7))), gListOfCubesToPlaced(e - 33), gListOfPickedUpCubes(e - 21))), List(aBookR4, aR4HoldingCube, aListOfPickedUpCubesTrue(e - 17)))), SPAttributes("duration" -> 4))
      }
      val OListR4PickUpAt45To48 = for {
        e <- 45 to 48 // 24 to 31 in list
      } yield {
        Operation(s"$OR4PickUpAt$e", List(PropositionCondition(AND(List(gRow3Complete, NOT(gR2Booked), NOT(gR5Booked), NOT(gR4Booked), OR(List(gListOfStatusBuildingPalettes(3), gListOfStatusBuildingPalettes(7))), gListOfCubesToPlaced(e - 33), gListOfPickedUpCubes(e - 21))), List(aBookR4, aR4HoldingCube, aListOfPickedUpCubesTrue(e - 17)))), SPAttributes("duration" -> 4))
      }
      //OPs for picking up cubes by R5 at space 1, row 1
      val OR5PickUpAt = "R5pickCube"
      val OListR5PickUpAt31To34 = for {
        e <- 31 to 34 // 16 to 19 in list
      } yield {
        Operation(s"$OR5PickUpAt$e", List(PropositionCondition(AND(List(NOT(gR2Booked), NOT(gR5Booked), NOT(gR4Booked), OR(List(gListOfStatusBuildingPalettes(2), gListOfStatusBuildingPalettes(6))), gListOfCubesToPlaced(e - 31))), List(aBookR5, aR5HoldingCube, aListOfPickedUpCubesTrue(e - 15)))), SPAttributes("duration" -> 6))
      }
      //OPs for picking up cubes by R5 t space 1, row 2
      val OListR5PickUpAt35To38 = for {
        e <- 35 to 38 // 20 to 23 in list
      } yield {
        Operation(s"$OR5PickUpAt$e", List(PropositionCondition(AND(List(gRow1Complete, NOT(gR2Booked), NOT(gR5Booked), NOT(gR4Booked), OR(List(gListOfStatusBuildingPalettes(2), gListOfStatusBuildingPalettes(6))), gListOfCubesToPlaced(e - 31), gListOfPickedUpCubes(e - 19))), List(aBookR5, aR5HoldingCube, aListOfPickedUpCubesTrue(e - 15)))), SPAttributes("duration" -> 6))
      }
      //OPs for picking up cubes by R5 t space 2, row 3 and 4
      val OListR5PickUpAt41To44 = for {
        e <- 41 to 44 // 24 to 31 in list
      } yield {
        Operation(s"$OR5PickUpAt$e", List(PropositionCondition(AND(List(gRow2Complete, NOT(gR2Booked), NOT(gR5Booked), NOT(gR4Booked), OR(List(gListOfStatusBuildingPalettes(3), gListOfStatusBuildingPalettes(7))), gListOfCubesToPlaced(e - 33), gListOfPickedUpCubes(e - 21))), List(aBookR5, aR5HoldingCube, aListOfPickedUpCubesTrue(e - 17)))), SPAttributes("duration" -> 6))
      }
      val OListR5PickUpAt45To48 = for {
        e <- 45 to 48 // 24 to 31 in list
      } yield {
        Operation(s"$OR5PickUpAt$e", List(PropositionCondition(AND(List(gRow3Complete, NOT(gR2Booked), NOT(gR5Booked), NOT(gR4Booked), OR(List(gListOfStatusBuildingPalettes(3), gListOfStatusBuildingPalettes(7))), gListOfCubesToPlaced(e - 33), gListOfPickedUpCubes(e - 21))), List(aBookR5, aR5HoldingCube, aListOfPickedUpCubesTrue(e - 17)))), SPAttributes("duration" -> 6))
      }
      //OPs for placing cubes with R4 11 - 14
      val OR4PlaceCubeAt = "R4putDownCube"
      val OListR4PlaceCubeAt11To14 = for {
        e <- 11 to 14 // 0 to 3 in list
      } yield {
        Operation(s"$OR4PlaceCubeAt$e", List(PropositionCondition(AND(List(gR4Booked, gR4HoldingCube, NOT(gListOfPutDownCubes(e - 11)), OR(List(gListOfPickedUpCubes(e - 11), gListOfPickedUpCubes(e + 5))))), List(aListOfPutDownCubesTrue(e - 11), aR4NotHoldingCube)),PropositionCondition(AND(List(gR4Booked)),List(aUnBookR4))), SPAttributes("duration" -> 6))
      }
      //OPs for placing cubes with R4 21 - 24
      val OListR4PlaceCubeAt21To24 = for {
        e <- 21 to 24 // 4 to 7 in list
      } yield {
        Operation(s"$OR4PlaceCubeAt$e", List(PropositionCondition(AND(List(gR4Booked, gR4HoldingCube, NOT(gListOfPutDownCubes(e - 17)), OR(List(gListOfPickedUpCubes(e - 17), gListOfPickedUpCubes(e - 1))))), List(aListOfPutDownCubesTrue(e - 17), aR4NotHoldingCube)),PropositionCondition(AND(List(gR4Booked)),List(aUnBookR4))), SPAttributes("duration" -> 6))
      }
      //OPs for placing cubes with R4 31 - 34
      val OListR4PlaceCubeAt31To34 = for {
        e <- 31 to 34 // 8 to 11 in list
      } yield {
        Operation(s"$OR4PlaceCubeAt$e", List(PropositionCondition(AND(List(gR4Booked, gR4HoldingCube, NOT(gListOfPutDownCubes(e - 23)), OR(List(gListOfPickedUpCubes(e - 23), gListOfPickedUpCubes(e - 7))))), List(aListOfPutDownCubesTrue(e - 23), aR4NotHoldingCube)),PropositionCondition(AND(List(gR4Booked)),List(aUnBookR4))), SPAttributes("duration" -> 6))
      }
      val OListR4PlaceCubeAt41To44 = for {
        e <- 41 to 44 // 12 to 15 in list
      } yield {
        Operation(s"$OR4PlaceCubeAt$e", List(PropositionCondition(AND(List(gR4Booked, gR4HoldingCube, NOT(gListOfPutDownCubes(e - 29)), OR(List(gListOfPickedUpCubes(e - 29), gListOfPickedUpCubes(e - 13))))), List(aListOfPutDownCubesTrue(e - 29), aR4NotHoldingCube)),PropositionCondition(AND(List(gR4Booked)),List(aUnBookR4))), SPAttributes("duration" -> 6))
      }

      //OPs for placing cubes with R5 11 - 14
      val OR5PlaceCubeAt = "R5putDownCube"
      val OListR5PlaceCubeAt11To14 = for {
        e <- 11 to 14
      } yield {
        Operation(s"$OR5PlaceCubeAt$e", List(PropositionCondition(AND(List(gR5Booked, gR5HoldingCube, NOT(gListOfPutDownCubes(e - 11)), gListOfPickedUpCubes(e + 5))), List(aListOfPutDownCubesTrue(e - 11), aR5NotHoldingCube)),PropositionCondition(AND(List(gR5Booked)),List(aUnBookR5))), SPAttributes("duration" -> 6))
      }
      //OPs for placing cubes with R5 21 - 24
      val OListR5PlaceCubeAt21To24 = for {
        e <- 21 to 24
      } yield {
        Operation(s"$OR5PlaceCubeAt$e", List(PropositionCondition(AND(List(gR5Booked, gR5HoldingCube, NOT(gListOfPutDownCubes(e - 17)), gListOfPickedUpCubes(e - 1))), List(aListOfPutDownCubesTrue(e - 17), aR5NotHoldingCube)),PropositionCondition(AND(List(gR5Booked)),List(aUnBookR5))), SPAttributes("duration" -> 6))
      }
      //OPs for placing cubes with R5 31 - 44
      val OListR5PlaceCubeAt31To34 = for {
        e <- 31 to 34
      } yield {
        Operation(s"$OR5PlaceCubeAt$e", List(PropositionCondition(AND(List(gR5Booked, gR5HoldingCube, NOT(gListOfPutDownCubes(e - 23)), gListOfPickedUpCubes(e - 7))), List(aListOfPutDownCubesTrue(e - 23), aR5NotHoldingCube)),PropositionCondition(AND(List(gR5Booked)),List(aUnBookR5))), SPAttributes("duration" -> 6))
      }
      val OListR5PlaceCubeAt41To44 = for {
        e <- 41 to 44
      } yield {
        Operation(s"$OR5PlaceCubeAt$e", List(PropositionCondition(AND(List(gR5Booked, gR5HoldingCube, NOT(gListOfPutDownCubes(e - 29)), gListOfPickedUpCubes(e - 13))), List(aListOfPutDownCubesTrue(e - 29), aR5NotHoldingCube)),PropositionCondition(AND(List(gR5Booked)),List(aUnBookR5))), SPAttributes("duration" -> 6))
      }

      //Operation which tells when towers is comeplete
      val OBuildingPaletteComplete = Operation("OBuildingPaletteComplete",
        List(PropositionCondition(AND(List(
          OR(List(AND(List(gListOfPutDownCubes(0), gListOfCubesToPlaced(0))), NOT(AND(List(gListOfPutDownCubes(0), gListOfCubesToPlaced(0)))))),
          OR(List(AND(List(gListOfPutDownCubes(1), gListOfCubesToPlaced(1))), NOT(AND(List(gListOfPutDownCubes(1), gListOfCubesToPlaced(1)))))),
          OR(List(AND(List(gListOfPutDownCubes(2), gListOfCubesToPlaced(2))), NOT(AND(List(gListOfPutDownCubes(2), gListOfCubesToPlaced(2)))))),
          OR(List(AND(List(gListOfPutDownCubes(3), gListOfCubesToPlaced(3))), NOT(AND(List(gListOfPutDownCubes(3), gListOfCubesToPlaced(3)))))),
          OR(List(AND(List(gListOfPutDownCubes(4), gListOfCubesToPlaced(4))), NOT(AND(List(gListOfPutDownCubes(4), gListOfCubesToPlaced(4)))))),
          OR(List(AND(List(gListOfPutDownCubes(5), gListOfCubesToPlaced(5))), NOT(AND(List(gListOfPutDownCubes(5), gListOfCubesToPlaced(5)))))),
          OR(List(AND(List(gListOfPutDownCubes(6), gListOfCubesToPlaced(6))), NOT(AND(List(gListOfPutDownCubes(6), gListOfCubesToPlaced(6)))))),
          OR(List(AND(List(gListOfPutDownCubes(7), gListOfCubesToPlaced(7))), NOT(AND(List(gListOfPutDownCubes(7), gListOfCubesToPlaced(7)))))),
          OR(List(AND(List(gListOfPutDownCubes(8), gListOfCubesToPlaced(8))), NOT(AND(List(gListOfPutDownCubes(8), gListOfCubesToPlaced(8)))))),
          OR(List(AND(List(gListOfPutDownCubes(9), gListOfCubesToPlaced(9))), NOT(AND(List(gListOfPutDownCubes(9), gListOfCubesToPlaced(9)))))),
          OR(List(AND(List(gListOfPutDownCubes(10), gListOfCubesToPlaced(10))), NOT(AND(List(gListOfPutDownCubes(10), gListOfCubesToPlaced(10)))))),
          OR(List(AND(List(gListOfPutDownCubes(11), gListOfCubesToPlaced(11))), NOT(AND(List(gListOfPutDownCubes(11), gListOfCubesToPlaced(11)))))),
          OR(List(AND(List(gListOfPutDownCubes(12), gListOfCubesToPlaced(12))), NOT(AND(List(gListOfPutDownCubes(12), gListOfCubesToPlaced(12)))))),
          OR(List(AND(List(gListOfPutDownCubes(13), gListOfCubesToPlaced(13))), NOT(AND(List(gListOfPutDownCubes(13), gListOfCubesToPlaced(13)))))),
          OR(List(AND(List(gListOfPutDownCubes(14), gListOfCubesToPlaced(14))), NOT(AND(List(gListOfPutDownCubes(14), gListOfCubesToPlaced(14)))))),
          OR(List(AND(List(gListOfPutDownCubes(15), gListOfCubesToPlaced(15))), NOT(AND(List(gListOfPutDownCubes(15), gListOfCubesToPlaced(15))))))
        )), List(aBuildingPaletteIsComplete, aBuildPalette1Empty, aBuildPalette2Empty))), SPAttributes("duration" -> 0))
      // Rows Done
      val ORow1Complete = Operation("ORow1Complete",
        List(PropositionCondition(AND(List(
          OR(List(AND(List(gListOfPutDownCubes(0), gListOfCubesToPlaced(0))), NOT(AND(List(gListOfPutDownCubes(0), gListOfCubesToPlaced(0)))))),
          OR(List(AND(List(gListOfPutDownCubes(1), gListOfCubesToPlaced(1))), NOT(AND(List(gListOfPutDownCubes(1), gListOfCubesToPlaced(1)))))),
          OR(List(AND(List(gListOfPutDownCubes(2), gListOfCubesToPlaced(2))), NOT(AND(List(gListOfPutDownCubes(2), gListOfCubesToPlaced(2)))))),
          OR(List(AND(List(gListOfPutDownCubes(3), gListOfCubesToPlaced(3))), NOT(AND(List(gListOfPutDownCubes(3), gListOfCubesToPlaced(3))))))
        )), List(aRow1CompleteTrue))), SPAttributes("duration" -> 0))
      val ORow2Complete = Operation("ORow2Complete",
        List(PropositionCondition(AND(List(
          OR(List(AND(List(gListOfPutDownCubes(4), gListOfCubesToPlaced(4))), NOT(AND(List(gListOfPutDownCubes(4), gListOfCubesToPlaced(4)))))),
          OR(List(AND(List(gListOfPutDownCubes(5), gListOfCubesToPlaced(5))), NOT(AND(List(gListOfPutDownCubes(5), gListOfCubesToPlaced(5)))))),
          OR(List(AND(List(gListOfPutDownCubes(6), gListOfCubesToPlaced(6))), NOT(AND(List(gListOfPutDownCubes(6), gListOfCubesToPlaced(6)))))),
          OR(List(AND(List(gListOfPutDownCubes(7), gListOfCubesToPlaced(7))), NOT(AND(List(gListOfPutDownCubes(7), gListOfCubesToPlaced(7))))))
        )), List(aRow2CompleteTrue))), SPAttributes("duration" -> 0))
      val ORow3Complete = Operation("ORow3Complete",
        List(PropositionCondition(AND(List(
          OR(List(AND(List(gListOfPutDownCubes(8), gListOfCubesToPlaced(8))), NOT(AND(List(gListOfPutDownCubes(8), gListOfCubesToPlaced(8)))))),
          OR(List(AND(List(gListOfPutDownCubes(9), gListOfCubesToPlaced(9))), NOT(AND(List(gListOfPutDownCubes(9), gListOfCubesToPlaced(9)))))),
          OR(List(AND(List(gListOfPutDownCubes(10), gListOfCubesToPlaced(10))), NOT(AND(List(gListOfPutDownCubes(10), gListOfCubesToPlaced(10)))))),
          OR(List(AND(List(gListOfPutDownCubes(11), gListOfCubesToPlaced(11))), NOT(AND(List(gListOfPutDownCubes(11), gListOfCubesToPlaced(11))))))
        )), List(aRow3CompleteTrue))), SPAttributes("duration" -> 0))

      //Elevator 2 Operations
      val OMoveUpPalette1WithElevator2 = Operation("OMoveUpPalette1WithElevator2", List(PropositionCondition(AND(List(gBuildPalette1Empty, NOT(gH2OutWithBuildPalette1))), List(aH2OutWithBuildPalette1True))), SPAttributes("duration" -> 5))
      val OMoveUpPalette2WithElevator2 = Operation("OMoveUpPalette2WithElevator2", List(PropositionCondition(AND(List(gBuildPalette2Empty, NOT(gH2OutWithBuildPalette2))), List(aH2OutWithBuildPalette2True))), SPAttributes("duration" -> 5))

      //Remove building palettes ops
      val OR2Palette1RemoveR4Space1 = Operation("OR2Palette1RemoveR4Space1", List(PropositionCondition(AND(List(gR4Dodge, NOT(gR2Booked), gListOfStatusBuildingPalettes(0), gH2OutWithBuildPalette1, NOT(gBuildSpotBooked))), List(aBookR2, aChangeStatusBuildingPalettesFalse(0))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      // Operation R4BuildFromPos1 Done
      val OR2Palette1RemoveR4Space2 = Operation("OR2Palette1RemoveR4Space2", List(PropositionCondition(AND(List(gR4Dodge, NOT(gR2Booked), gListOfStatusBuildingPalettes(1), gH2OutWithBuildPalette1, NOT(gBuildSpotBooked))), List(aBookR2, aChangeStatusBuildingPalettesFalse(1))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      // Operation R4BuildFromPos2 Done
      val OR2Palette1RemoveR5Space1 = Operation("OR2Palette1RemoveR5Space1", List(PropositionCondition(AND(List(gR4Dodge, gR5Dodge, NOT(gR2Booked), gListOfStatusBuildingPalettes(2), gH2OutWithBuildPalette1, NOT(gBuildSpotBooked))), List(aBookR2, aChangeStatusBuildingPalettesFalse(2))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      // Operation R5BuildFromPos1 Done
      val OR2Palette1RemoveR5Space2 = Operation("OR2Palette1RemoveR5Space2", List(PropositionCondition(AND(List(gR4Dodge, gR5Dodge, NOT(gR2Booked), gListOfStatusBuildingPalettes(3), gH2OutWithBuildPalette1, NOT(gBuildSpotBooked))), List(aBookR2, aChangeStatusBuildingPalettesFalse(3))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      // Operation R5BuildFromPos2 Done
      val OR2Palette2RemoveR4Space1 = Operation("OR2Palette2RemoveR4Space1", List(PropositionCondition(AND(List(gR4Dodge, NOT(gR2Booked), gListOfStatusBuildingPalettes(4), gH2OutWithBuildPalette2, NOT(gBuildSpotBooked))), List(aBookR2, aChangeStatusBuildingPalettesFalse(4))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      // Operation R4BuildFromPos1 Done
      val OR2Palette2RemoveR4Space2 = Operation("OR2Palette2RemoveR4Space2", List(PropositionCondition(AND(List(gR4Dodge, NOT(gR2Booked), gListOfStatusBuildingPalettes(5), gH2OutWithBuildPalette2, NOT(gBuildSpotBooked))), List(aBookR2, aChangeStatusBuildingPalettesFalse(5))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      // Operation R4BuialdFromPos2 Done
      val OR2Palette2RemoveR5Space1 = Operation("OR2Palette2RemoveR5Space1", List(PropositionCondition(AND(List(gR4Dodge, gR5Dodge, NOT(gR2Booked), gListOfStatusBuildingPalettes(6), gH2OutWithBuildPalette2, NOT(gBuildSpotBooked))), List(aBookR2, aChangeStatusBuildingPalettesFalse(6))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      // Operation R5BuildFromPos1 Done
      val OR2Palette2RemoveR5Space2 = Operation("OR2Palette2RemoveR5Space2", List(PropositionCondition(AND(List(gR4Dodge, gR5Dodge, NOT(gR2Booked), gListOfStatusBuildingPalettes(7), gH2OutWithBuildPalette2, NOT(gBuildSpotBooked))), List(aBookR2, aChangeStatusBuildingPalettesFalse(7))),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))
      //Op for removing complete tower
      val OR2RemoveBuildingPalette = Operation("OR2RemoveBuildingPalette", List(PropositionCondition(AND(List(gR4Dodge, gR5Dodge, NOT(gR2Booked), NOT(gR5Booked), NOT(gR4Booked), gBuildSpotBooked, gBuildingPaletteComplete)), List(aBookR2, aBuildSpotUnBook)),PropositionCondition(AND(List(gR2Booked)),List(aUnBookR2))), SPAttributes("duration" -> 20))

      // Ops for moving lowering H2
      val OMoveOutPalette1WithElevator2 = Operation("OMoveUpPalette1WithElevator1", List(PropositionCondition(AND(List(gH2OutWithBuildPalette1, OR(List(gListOfStatusBuildingPalettes(0), gListOfStatusBuildingPalettes(1), gListOfStatusBuildingPalettes(2), gListOfStatusBuildingPalettes(3))))), List(aH2OutWithBuildPalette1False))), SPAttributes("duration" -> 5))
      val OMoveOutPalette2WithElevator2 = Operation("OMoveUpPalette2WithElevator1", List(PropositionCondition(AND(List(gH2OutWithBuildPalette2, OR(List(gListOfStatusBuildingPalettes(4), gListOfStatusBuildingPalettes(5), gListOfStatusBuildingPalettes(6), gListOfStatusBuildingPalettes(7))))), List(aH2OutWithBuildPalette2False))), SPAttributes("duration" -> 5))
      //LISTs With all OPS
      val allOPs: List[Operation] = List(OInitOperation, OMoveInBuildingPalette1, OMoveInBuildingPalette2, OMoveInBuildPalette, OR2Palette1ToR4Space1,
        OR2Palette1ToR4Space2, OR2Palette1ToR5Space1, OR2Palette1ToR5Space2, OR2Palette2ToR4Space1, OR2Palette2ToR4Space2, OR2Palette2ToR5Space1,
        OR2Palette2ToR5Space2, OMoveUpPalette1WithElevator1, OMoveDownPalette1WithElevator1, OMoveUpPalette2WithElevator1, OMoveDownPalette2WithElevator1,
        OR2PlaceBuildingPalette, OR4RemoveBooking, OR5RemoveBooking, OMoveUpPalette1WithElevator2, OMoveUpPalette2WithElevator2,
        OR2Palette1RemoveR4Space1, OR2Palette1RemoveR4Space2, OR2Palette1RemoveR5Space1, OR2Palette1RemoveR5Space2, OR2Palette2RemoveR4Space1,
        OR2Palette2RemoveR4Space2, OR2Palette2RemoveR5Space1, OR2Palette2RemoveR5Space2, OR2RemoveBuildingPalette, OMoveOutPalette1WithElevator2,
        OMoveOutPalette2WithElevator2, OBuildingPaletteComplete, OR4ToDodge, OR5ToDodge, ORow1Complete, ORow2Complete, ORow3Complete
      ) ++ OListR4PickUpAt11To14 ++ OListR4PickUpAt15To18 ++ OListR4PickUpAt21To24 ++ OListR4PickUpAt25To28 ++ OListR5PickUpAt31To34 ++ OListR5PickUpAt35To38 ++ OListR5PickUpAt41To44 ++ OListR5PickUpAt45To48 ++ OListR4PlaceCubeAt11To14 ++ OListR4PlaceCubeAt21To24 ++ OListR4PlaceCubeAt31To34 ++ OListR4PlaceCubeAt41To44 ++ OListR5PlaceCubeAt11To14 ++ OListR5PlaceCubeAt21To24 ++ OListR5PlaceCubeAt31To34 ++ OListR5PlaceCubeAt41To44 ++ OListR4PickUpAt31To34 ++ OListR4PickUpAt35To38 ++ OListR4PickUpAt41To44 ++ OListR4PickUpAt45To48 ++ listOfWallSchemeOps

      //Test optimizer algoritim----------------------------------------------------------------------------------------------------------------------------------------

      case class Transition(gCostIn: Int, headIn: Node, tailIn: Node, OPsIn: List[Operation]) {
        var gCost: Int = gCostIn
        var head: Node = headIn
        var tail: Node = tailIn
        var OPs: List[Operation] = OPsIn
      }

      case class Node(nameIn: Int, stateIn: State, inIn: Transition, outIn: List[Transition], gCostIn: Int) {
        var name: Int = nameIn
        var state: State = stateIn
        var inTran: Transition = inIn
        var outTran: List[Transition] = outIn
//        var hCost: Int = hCostIn    //behövs ej för dijksstra
        var gCost: Int = gCostIn
//        var fCost: Int = fCostIn    //behövs ej för dijksstra
      }

      def createOpsStateVars(ops: List[Operation]) = {
        ops.map(o => o.id -> sp.domain.logic.OperationLogic.OperationState.inDomain).toMap
      }

      val wallScheme: List[List[Int]] = List(List(1, 1, 1, 0), List(0, 0, 0, 0), List(0, 0, 0, 0), List(0, 0, 0, 0))

      val initOPs: List[Operation] = List(OInitOperation)

      val statevars = thingList.map(sv => sv.id -> sv.inDomain).toMap ++ createOpsStateVars(initOPs)


      implicit val props = EvaluateProp(statevars, Set[SPValue](), ThreeStateDefinition)
      val idleState = getIdleState(thingList.toSet)

      val initState = idleState match {
        case State(map) => State(map ++ allOPs.map(_.id -> OperationState.init).toMap)
      }

      var nameInt: Int = 0
      var tempGCost: Int = 0
      var sizeOfWallInt: Int = 0

      val initNode = Node(nameInt, initState, null, null, 0)

      for (a <- 0 to 3) {
        val currentList = wallScheme(a)
        for (b <- 0 to 3) {
          if (currentList(b) != 0) {
            tempGCost = tempGCost + 9
            initNode.state = listOfWallSchemeOps(nameInt).next(initNode.state)
 //           initNode.hCost = tempGCost //behövs ej för dijksstra
            sizeOfWallInt = sizeOfWallInt + 1
          }
          nameInt = nameInt + 1
        }
      }

      var examinedNode: Node = initNode
      var openNodeList: List[Node] = List()
      var closedNodeList: List[Node] = List(examinedNode)


      var loopVar: Boolean  = true

      while (loopVar) {

        closedNodeList = closedNodeList ++ List(examinedNode)

        val enabledOps = allOPs.filter(_.conditions.filter(_.attributes.getAs[String]("kind").getOrElse("") == "precondition").headOption
        match {
          case Some(cond) => cond.eval(examinedNode.state)
          case None => true
        })


        //if(enabledOps != null && examinedNode.inTran.OPs != null) {
          var transitions: List[Transition] = enabledOps.map{o =>
            Transition(100,null, examinedNode, o :: examinedNode.inTran.OPs)
          }
          import sp.domain.Logic._
          transitions.map{tempTran =>
            tempTran.OPs.map{ o =>
              val tempGCost: Int = o.attributes.getAs[Int]("duration").getOrElse(-1)
              if (tempGCost < tempTran.gCost) {
                tempTran.gCost = tempGCost
              }
            }
          }
        //}


          examinedNode.outTran = transitions

        transitions.map{ tempTran =>
            nameInt = nameInt +1
            var newNode: Node = new Node(
              nameInt,
              examinedNode.state,
              tempTran,
              null,
//              examinedNode.hCost - tempTran.gCost*tempTran.OPs.size,        //fixa rätt kostnad, behövs ej för dijksstra
              tempTran.gCost + examinedNode.gCost                                //fixa rätt kostnad
//              examinedNode.gCost + examinedNode.hCost                                   //fixa rätt kostnad, behövs ej för dijksstra
            )
//            if(newNode.hCost < 0){
//              newNode.hCost = 0
//            }
            tempTran.head = newNode
            openNodeList = openNodeList ++ List(newNode)
          }
          closedNodeList.map{n =>
            if(!(closedNodeList contains n)){
              if(n.gCost < examinedNode.gCost){
                examinedNode = n
              }
            }
          }
          examinedNode.inTran.OPs.map{ o =>
            examinedNode.state = o.next(examinedNode.state)
          }
          if(examinedNode.outTran == null){
            loopVar = false                                          //terminate the while loop
          }
        }
        var pathOfOPs: List[Operation] = List()
        while (examinedNode.inTran != null){
          pathOfOPs = pathOfOPs ++ examinedNode.inTran.OPs
        examinedNode = examinedNode.inTran.tail
        }
      replyTo ! Response(pathOfOPs, SPAttributes(), rnr.req.service, rnr.req.reqID)
      self ! PoisonPill
    }
  }
}

/*

object operationMaker extends SPService {
  val specification = SPAttributes(
    "service" -> SPAttributes(
      "group"-> "Operations",
      "description" -> "Makes operations"
    )
  )



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


}
*/


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


