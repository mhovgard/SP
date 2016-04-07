package sp.modelingTester

import akka.actor._
import sp.domain._
import sp.domain.Logic._
import sp.system.messages._

/**
  * Created by kristofer on 2016-04-06.
  */
class ModelingTester(mh: ActorRef) extends Actor with MyModel {
  val modelID = ID.newID
  def receive = {
    case "load model" =>
      mh ! CreateModel(modelID, "testingModelingTrait")
      mh ! UpdateIDs(modelID, getModel, SPAttributes("info"->"New model"))
    case "do modeling" =>
      val ids = List[IDAble]()
      val model = new sp.modelingTester.MyModeling{val ids = ids}
      import model._

      items.o1.copy(weird = items.o1.weird.copy(tomte = true))

  }

}

trait MyModel {

  val v1Model = Thing("v1", SPAttributes("initial"->false), ID.makeID("74023bf0-273c-4824-a0f7-9e7274950887").get)
  val dublV1Model = Thing("v1", SPAttributes("initial"->true), ID.makeID("c43b570f-00cf-49c8-b479-63e8fada246c").get)
  val v2Model = Thing("v2", SPAttributes("initial"->"kalle"), ID.makeID("fe072e2c-eb43-4774-bbc5-7714652ce1c8").get)
  val o1Model = Operation(
    "o1",
    List(PropositionCondition(AND(List(EQ(SVIDEval(v1Model.id), ValueHolder(false)))), List())),
    SPAttributes("time"->5, "weird"->SPAttributes("tomte"->false)),
    ID.makeID("3957b439-8fe7-4d7a-a884-93cd9aa030ce").get
  )
  val o2Model = Operation(
    "o2",
    List(PropositionCondition(AND(List(EQ(SVIDEval(v2Model.id), ValueHolder(true)))), List())),
    SPAttributes("time"->30),
    ID.makeID("7eca3e51-c625-49f1-9379-f67e62ddb753").get
  )

  val hModel = HierarchyRoot("h1",
    List(
      HierarchyNode(o1Model.id,
        List(HierarchyNode(v1Model.id))
      ),
      HierarchyNode(o2Model.id,
        List(HierarchyNode(v2Model.id))
      )
    ),
    SPAttributes(),
    ID.makeID("c17d5838-4bfa-4a1c-b58b-38269bf133a1").get
  )

  def getModel = List(v1Model, dublV1Model, v2Model, o1Model, o2Model, hModel)

}

abstract class MyModeling {
  val ids: List[IDAble]
  val idMap = ids.map(i => i.id -> i).toMap
  val itemV1 = idMap(ID.makeID("74023bf0-273c-4824-a0f7-9e7274950887").get).asInstanceOf[Thing]
  val itemV1_2 = idMap(ID.makeID("c43b570f-00cf-49c8-b479-63e8fada246c").get).asInstanceOf[Thing]
  val itemV2 = idMap(ID.makeID("fe072e2c-eb43-4774-bbc5-7714652ce1c8").get).asInstanceOf[Thing]
  val itemO1 = idMap(ID.makeID("3957b439-8fe7-4d7a-a884-93cd9aa030ce").get).asInstanceOf[Operation]
  val itemO2 = idMap(ID.makeID("7eca3e51-c625-49f1-9379-f67e62ddb753").get).asInstanceOf[Operation]
  val itemH = idMap(ID.makeID("c17d5838-4bfa-4a1c-b58b-38269bf133a1").get).asInstanceOf[HierarchyRoot]




  trait IDAbleModel {def toIDAble: IDAble}



  case class V1Class(name: String, id: ID, attributes: SPAttributes, initial: Boolean) extends IDAbleModel {
    val attr = attributes + ("initial"->initial)
    def toIDAble = Thing(name, attr, id)
  }
  case object V1Values{
    val id_74023bf0_273c_4824_a0f7_9e7274950887 = "74023bf0-273c-4824-a0f7-9e7274950887"
    val name_v1 = "v1"
    val initial_false = false
  }

  case class V1_2Class(name: String, id: ID, attributes: SPAttributes, initial: Boolean) extends IDAbleModel {
    val attr = attributes + ("initial"->initial)
    def toIDAble = Thing(name, attr, id)
  }
  case object V1_2Values{
    val id_c43b570f_00cf_49c8_b479_63e8fada246c = "c43b570f-00cf-49c8-b479-63e8fada246c"
    val name_v1_2 = "v1_2"
    val initial_true = true
  }

  case class V2Class(name: String, id: ID, attributes: SPAttributes, initial: String) extends IDAbleModel {
    val attr = attributes + ("initial"->initial)
    def toIDAble = Thing(name, attr, id)
  }
  case object V2Values{
    val id_fe072e2c_eb43_4774_bbc5_7714652ce1c8 = "fe072e2c-eb43-4774-bbc5-7714652ce1c8"
    val name_v2 = "v2"
    val initial_true = "kalle"
  }

  case class _weirdO1(tomte: Boolean)
  case object _weirdO1Values{
    val tomte_false = false
  }
  case class O1Class(name: String, id: ID, attributes: SPAttributes, conditions: List[Condition], time: Int, weird: _weirdO1) extends IDAbleModel {
    val attr = attributes + ("time"->time) + ("weird"->SPAttributes("tomte"->weird.tomte))
    def toIDAble = Operation(name, conditions, attr, id)
  }
  case object O1Values{
    val id_3957b439_8fe7_4d7a_a884_93cd9aa030ce = "3957b439-8fe7-4d7a-a884-93cd9aa030ce"
    val name_o1 = "o1"
    val time_5 = 5
    val weird = _weirdO1Values
  }

  case class O2Class(name: String, id: ID, attributes: SPAttributes, conditions: List[Condition], time: Int) extends IDAbleModel {
    val attr = attributes + ("time"->time)
    def toIDAble = Operation(name, conditions, attr, id)
  }
  case object O2Values{
    val id_3957b439_8fe7_4d7a_a884_93cd9aa030ce = "3957b439-8fe7-4d7a-a884-93cd9aa030ce"
    val name_o2 = "o2"
    val time_30 = 30
  }

  case object items{
    val v1 = V1Class(name = itemV1.name, id = itemV1.id, attributes = itemV1.attributes, initial = false)
    val v1V = V1Values
    val v1_2 = V1Class(name = itemV1_2.name, id = itemV1_2.id, attributes = itemV1_2.attributes, initial = true)
    val v1_2v = V1_2Values
    val v2 = V2Class(name = itemV1_2.name, id = itemV1_2.id, attributes = itemV1_2.attributes, initial = "kalle")
    val v2v = V2Values
    val o1 = O1Class(name = itemO1.name, id = itemO1.id, attributes = itemO1.attributes, conditions = itemO1.conditions, time = 5, weird = _weirdO1(false))
    val o1V = O1Values
    val o2 = O2Class(name = itemO2.name, id = itemO2.id, attributes = itemO2.attributes, conditions = itemO2.conditions, time = 30)
    val o2V = O2Values
  }


  case object operations {
    val o1 = items.o1
    val o2 = items.o2
  }
  case object things {
    val v1 = items.v1
    val v1_2 = items.v1_2
    val v2 = items.v2
  }


  val v1 = items.v1
  val v1_2 = items.v1_2
  val v2 = items.v2
  val o2 = items.o2
  val o1 = items.o1



  case object _V1_2Initial{val value_false = false}
  //  case object _V1_2ID{val value_c43b570f_00cf_49c8_b479_63e8fada246c = "c43b570f-00cf-49c8-b479-63e8fada246c"}
  //  case object _V1_2Name{val v1_2 = "v1_2"}
  //  case object V1_2{
  //    val id = itemV1_2.id
  //    val idV = _V1_2ID
  //    val name = itemV1_2.name
  //    val nameV = _V1_2Name
  //    val attributes = itemV1_2.attributes
  //    val initial = false
  //    val initialV = _V1_2Initial
  //  }
  //
  //  case object _V2Initial{val value_false = false}
  //  case object _V2ID{val value_74023bf0_273c_4824_a0f7_9e7274950887 = "74023bf0-273c-4824-a0f7-9e7274950887"}
  //  case object _V2Name{val v2 = "v2"}
  //  case object V2{
  //    val id = itemV2.id
  //    val idV = _V2ID
  //    val name = itemV2.name
  //    val nameV = _V2Name
  //    val attributes = itemV2.attributes
  //    val initial = false
  //    val initialV = _V2Initial
  //  }
  //
  //  case object _O2Initial{val value_false = false}
  //  case object _O2ID{val value_7eca3e51_c625_49f1_9379_f67e62ddb753 = "7eca3e51-c625-49f1-9379-f67e62ddb753"}
  //  case object _O2Name{val o2 = "o2"}
  //  case object O2{
  //    val id = itemO2.id
  //    val idV = _O2ID
  //    val name = itemO2.name
  //    val nameV = _O2Name
  //    val attributes = itemO2.attributes
  //    val initial = false
  //    val initialV = _O2Initial
  //  }



}
