package sp.services.sopmaker

import akka.actor._
import sp.domain._

import scala.annotation.tailrec

/**
 * This message starts the identification. Returns the
 * relations identified.
 * TODO: I need to update this later for better performance. Mainly using
 * ints and arrays instead of all the objects.
 *
 * @param ops The ops should have all conditions
 *            that should be used. So add Specs
 *            before
 */
case class MakeASOP(ops: List[ID], relations: RelationMap)

class SOPMaker extends Actor with Groupify {
  def receive = {
    case MakeASOP(ops, rels) => {

    }
  }
}

//TODO: Move these to domain.logic. SOP logic
trait Groupify {


  def makeSOPsFromOpsID(ops: List[ID]): List[SOP] = ops map SOP.apply
  def makeSOPRelationMapFromRelationMap(rels: RelationMap): Map[Set[SOP], SOP] = {
    rels.relations.map{case (pair, rel) =>
      val sopPair: Set[SOP] = Set(SOP(pair.o1), SOP(pair.o2))
      sopPair -> rel
    }
  }

  /**
   * Identifies the relation between two SOPs. Requires that all children have relation in the map
   * @param sop1
   * @param sop2
   * @param relations The SOP relation map containing relation among children
   * @return a sop containing the sops
   */
  def identifySOPRelation(sop1: SOP, sop2: SOP, relations: Map[Set[SOP], SOP]): SOP = {
    if (relations.contains(Set(sop1, sop2))) relations(Set(sop1, sop2))
    else {
      val relationBetweenPairs = (for {
        s1 <- {if (sop1.children.isEmpty) List(sop1) else sop1.children}
        s2 <- {if (sop2.children.isEmpty) List(sop2) else sop2.children}
      } yield {
        if (s1 == s2) Other()
        else if (!relations.contains(Set(s1, s2))){
          identifySOPRelation(s1, s2, relations).modify(List())
        } else
        relations(Set(s1, s2)).modify(List())
      }) toSet

      if (relationBetweenPairs.size == 1) relationBetweenPairs.head.modify(List(sop1, sop2))
      else if (relationBetweenPairs == Set(Sequence(), SometimeSequence())) Sequence(sop1, sop2)
      else Other(sop1, sop2)
    }
  }

  /**
   * Takes a list of newly created groups and identifies the relation among them
   * based on the relation of the children
   * @param sops the list od groups
   * @param relations the current relation map
   * @return an Updated relation map
   */
  def updateSOPRelationMap(sops: List[SOP], relations: Map[Set[SOP], SOP]) = {
    val createdRelations = for {
      s1 <- sops
      s2 <- sops if s1 != s2
      pairRelation <- relations.get(Set(s1,s2))
    } yield Set(s1, s2) -> pairRelation
    relations ++ createdRelations.toMap
  }



  def groupify(sopsToGroup: List[SOP],
               relations: Map[Set[SOP], SOP],
               relationToGroup: SOP => Boolean,
               createSOP: List[SOP] => SOP): List[SOP] = {

    val sops = sopsToGroup map { sop => if (sop.children.isEmpty) sop else sop.modify(groupify(sop.children.toList, relations, relationToGroup, createSOP))}

    val relatedPairs = for {
      x <- sops
      y <- sops
      rel <- relations.get(Set(x, y)) if relationToGroup(rel)
    } yield Set(x, y)

    def mergeTheGroups(theGroups: Set[Set[SOP]]): Set[Set[SOP]] = {
      val merge = theGroups.foldLeft(Set[Set[SOP]]())({
        (b, a) => {
          val filter = b partition(!_.intersect(a).isEmpty)
          val union = a ++ filter._1.foldLeft(Set[SOP]())(_ ++ _)
          filter._2 + union
        }
      })
      if (merge != theGroups) mergeTheGroups(merge)
      else merge
    }
    val mergeIntoGroups = mergeTheGroups(relatedPairs.toSet)

    val sopsAddedToGroup = relatedPairs.foldLeft(Set[SOP]())((a, b) => a ++ b)
    val sopsNotAddedToGroup = sops filter (!sopsAddedToGroup.contains(_))

    val newGroups = mergeIntoGroups map (set => createSOP(set.toList)) toList

    newGroups ++ sopsNotAddedToGroup

  }


}

trait Sequencify {

}
