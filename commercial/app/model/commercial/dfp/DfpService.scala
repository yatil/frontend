package model.commercial.dfp

import com.google.api.ads.dfp.axis.v201311._
import com.google.api.ads.dfp.axis.utils.v201311.StatementBuilder
import common.Logging

object DfpService extends Logging {

  val lineItemService = dfpServices.get(session, classOf[LineItemServiceInterface])

  def syncCommercialGroups(orderId: Long, commercialGroups: Seq[CommercialGroup]) {
    val inDfp = DfpService.fetchCommercialGroups(orderId)

    val toInsert = commercialGroups filterNot (group => inDfp.map(_.title).contains(group.title))
    if (!toInsert.isEmpty) insertCommercialGroups(toInsert)

    val toUpdate = {
      val common = inDfp filter (group => commercialGroups.map(_.title).contains(group.title))
      common filterNot (group => commercialGroups.contains(group))
    }
    if (!toUpdate.isEmpty) updateCommercialGroups(toUpdate)

    val toArchive = inDfp filterNot (group => commercialGroups.contains(group))
    if (!toArchive.isEmpty) archiveCommercialGroups(toArchive)
  }

  def fetchCommercialGroups(orderId: Long): Seq[CommercialGroup] = {
    val statement = new StatementBuilder()
      .where("orderId = :orderId")
      .withBindVariableValue("orderId", orderId)
      .toStatement

    val lineItems = Option {
      lineItemService.getLineItemsByStatement(statement).getResults
    }.map {
      _.toSeq
    }.getOrElse(Nil)

    lineItems.filterNot(_.getIsArchived).map(CommercialGroup(_))
  }

  def insertCommercialGroups(commercialGroups: Seq[CommercialGroup]) {
    log.info("Inserting " + commercialGroups)

    val lineItems = lineItemService.createLineItems(commercialGroups.map(LineItem(_)).toArray)

    lineItems foreach {
      createdLineItem =>
        log.info(s"Created a line item with ID [${createdLineItem.getId}] and name [${createdLineItem.getName}]")
    }
  }

  def updateCommercialGroups(commercialGroups: Seq[CommercialGroup]) {
    // TODO
    println("Updating " + commercialGroups)
  }

  def archiveCommercialGroups(commercialGroups: Seq[CommercialGroup]) {
    log.info("Archiving " + commercialGroups)

    val action = new ArchiveLineItems()
    // TODO: include orderId
    val titles = commercialGroups.map(_.title).mkString("'", "', '", "'")
    val statement = new StatementBuilder().where(s"name in ($titles)").toStatement
    val updateResult = lineItemService.performLineItemAction(action, statement)
    log.info(s"Archived ${updateResult.getNumChanges} line items")
  }
}
