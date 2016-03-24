
import sp.domain._
import sp.psl._
import sp.domain.Logic._


class operationMaker {


  //Init parsers
  val parserG = sp.domain.logic.PropositionParser(List())
  val parserA = sp.domain.logic.ActionParser(List())

  // Things for Guards
  val cubeSpace1Booked = Thing("cubeSpace1Boked")                              //v1
  val cubeSpace2Booked = Thing("cubeSpace2Boked")                              //v2
  val cubeSpace3Booked = Thing("cubeSpace3Boked")                              //v3
  val cubeSpace4Booked = Thing("cubedSpace4Boked")                             //v4
  val buildSpaceBooked = Thing("buildSpaceBooked")
  val R2Booked =  Thing("R2Booked")
  val R5Booked =  Thing("R5Booked")
  val R4Booked =  Thing("R5Booked")
  val R4Dodge =   Thing("R4Dodge")
  val R5Dodge =   Thing("R5Dodge")

  // Things for Actions
  val R2MoveCubePalletTo1 = Thing("R2MoveBuildPalletTo1")
  val R2MoveCubePalletTo2 = Thing("R2MoveBuildPalletTo3")
  val R2MoveCubePalletTo3 = Thing("R2MoveBuildPalletTo3")
  val R2MoveCubePalletTo4 = Thing("R2MoveBuildPalletTo4")
  val R2MoveBuildPallet = Thing("R2MoveBuildPallet")



  //Create gaurds
  val buildSpace1Booked = parserG.parseStr("buildSpace1Booked == true").right.get
  val buildSpace2Booked = parserG.parseStr("buildSpace2Booked == true").right.get
  val buildSpace3Booked = parserG.parseStr("buildSpace3Booked == true").right.get
  val buildSpace4Booked = parserG.parseStr("buildSpace4Booked == true").right.get
  val R2booked = parserG.parseStr("R2Booked == false").right.get
  val R4booked = parserG.parseStr("R4Booked == false").right.get
  val R5booked = parserG.parseStr("R5Booked == false").right.get
  val R4dodge = parserG.parseStr("R4Dodge == true").right.get
  val R5dodge = parserG.parseStr("R4Dodge == true").right.get

  //Create actions
  val aGenerateOperatorInstructions = parserA.parseStr("generateOperatorInstructions = True").right.get

  //Operations
  val init = Operation("Init", List(PropositionCondition(AND(List()), List(aGenerateOperatorInstructions))),SPAttributes(), ID.newID)
  val R2PalettToR5Pos1 = Operation //
  val R2PalettToR5Pos2 = Operation
  val R2PalettToR4Pos1 = Operation
  val R2PalettToR4Pos2 = Operation
  val R2PalettRemoveR5Pos1 = Operation
  val R2PalettRemoveR5Pos2 = Operation
  val R2PalettRemoveR4Pos1 = Operation
  val R2PalettRemoveR4Pos2 = Operation
  val R2PlaceBuildingPalett = Operation
  val R2RemoveBuildingPalett = Operation
}



/*

  //Init parsers
  val parserG = sp.domain.logic.PropositionParser(List(newBuildOrder, systemReady))
  val parserA = sp.domain.logic.ActionParser(List(generateOperatorInstructions))

  // Things for Guards
  val newBuildOrder = Thing("newBuildOrder")
  val systemReady = Thing("systemReady")
  val operatorInstructionsGenerated = Thing("operatorInstructionsGenerated")
  val palettReady = Thing("palettReady")


  // Things for Actions
  val generateOperatorInstructions = Thing("generateOperatorInstructions")



  //Create gaurds
  val gNewBuildOrder = parserG.parseStr("newBuildOrder == true").right.get
  val gSystemReady = parserG.parseStr("systemReady == true").right.get
  //Create actions
  val aGenerateOperatorInstructions = parserA.parseStr("generateOperatorInstructions = True").right.get

  //Operations
  val init = Operation("Init", List(PropositionCondition(AND(List(gNewBuildOrder,gSystemReady)), List(aGenerateOperatorInstructions))),SPAttributes(), ID.newID)
  //När det finns instruktioner genererade och palett ej redo, visa instruktioner
  val materialInput = Operation("materialInput", List(PropositionCondition()))
  //När en palett är redo, kör band och sätt palett ej redo
  val flexLinkRun = Operation
*/