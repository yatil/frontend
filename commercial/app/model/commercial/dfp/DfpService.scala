package model.commercial.dfp

import com.google.api.ads.dfp.axis.v201311._
import com.google.api.ads.dfp.axis.utils.v201311.StatementBuilder

object DfpService {

  val lineItemService = dfpServices.get(session, classOf[LineItemServiceInterface])

  def syncCommercialGroups(orderId: Long, commercialGroups: Seq[CommercialGroup]) {

    def isInGivenGroups(group: CommercialGroupWithArchivalStatus) = {
      commercialGroups.contains(group.commercialGroup)
    }

    val inDfp = DfpService.fetchCommercialGroupsWithArchivalStatus(orderId)

    val toInsert = commercialGroups filterNot (group => inDfp.map(_.title).contains(group.title))
    if (!toInsert.isEmpty) insertCommercialGroups(orderId, toInsert)

    val toUpdate = {
      val common = inDfp filter (group => commercialGroups.map(_.title).contains(group.title))
      common filterNot isInGivenGroups
    }
    if (!toUpdate.isEmpty) updateCommercialGroups(toUpdate.map(_.commercialGroup))

    val toArchive = inDfp filterNot (group => isInGivenGroups(group) && !group.isArchived)
    if (!toArchive.isEmpty) archiveCommercialGroups(toArchive.map(_.commercialGroup))
  }

  def fetchCommercialGroupsWithArchivalStatus(orderId: Long): Seq[CommercialGroupWithArchivalStatus] = {
    val statement = new StatementBuilder()
      .where("orderId = :orderId")
      .withBindVariableValue("orderId", orderId)
      .toStatement

    val lineItems = Option {
      lineItemService.getLineItemsByStatement(statement).getResults
    }.map {
      _.toSeq
    }.getOrElse(Nil)

    lineItems.map {
      lineItem => CommercialGroupWithArchivalStatus(CommercialGroup(lineItem), lineItem.getIsArchived)
    }
  }

  def insertCommercialGroups(orderId: Long, commercialGroups: Seq[CommercialGroup]) {
    println("Inserting " + commercialGroups)

    val lineItems = lineItemService.createLineItems {
      commercialGroups.map(group => LineItem(orderId, group)).toArray
    }

    lineItems foreach {
      createdLineItem =>
        println(s"Created a line item with ID [${createdLineItem.getId}] and name [${createdLineItem.getName}]")
    }
  }

  def updateCommercialGroups(commercialGroups: Seq[CommercialGroup]) {
    // TODO
    println("Updating " + commercialGroups)
  }

  def archiveCommercialGroups(commercialGroups: Seq[CommercialGroup]) {
    println("Archiving " + commercialGroups)

    val action = new ArchiveLineItems()
    // TODO: include orderId
    val titles = commercialGroups.map(_.title).mkString("'", "', '", "'")
    val statement = new StatementBuilder().where(s"name in ($titles)").toStatement
    val updateResult = lineItemService.performLineItemAction(action, statement)
    println(s"Archived ${updateResult.getNumChanges} line items")
  }
}


case class CommercialGroupWithArchivalStatus(commercialGroup: CommercialGroup, isArchived: Boolean) {
  val title = commercialGroup.title
}
