package model.commercial.dfp

import com.google.api.ads.dfp.axis.v201311._
import com.google.api.ads.dfp.axis.utils.v201311.StatementBuilder
import scala.Array

object DfpService {

  sealed case class CommercialGroupWithArchivalStatus(commercialGroup: CommercialGroup, isArchived: Boolean) {
    val title = commercialGroup.title
  }

  private val lineItemService = dfpServices.get(session, classOf[LineItemServiceInterface])
  private val creativeService = dfpServices.get(session, classOf[CreativeServiceInterface])
  private val licaService = dfpServices.get(session, classOf[LineItemCreativeAssociationServiceInterface])

  def syncCommercialGroups(orderId: Long, commercialGroups: Seq[CommercialGroup]) {

    def isInImportedCommercialGroups(group: CommercialGroupWithArchivalStatus) = {
      commercialGroups.contains(group.commercialGroup)
    }

    val inDfp = DfpService.fetchCommercialGroupsWithArchivalStatus(orderId)

    val toInsert = commercialGroups filterNot (group => inDfp.map(_.title).contains(group.title))
    if (!toInsert.isEmpty) insertCommercialGroups(orderId, toInsert)

    val toUpdate = {
      val common = inDfp filter (group => commercialGroups.map(_.title).contains(group.title))
      common filterNot isInImportedCommercialGroups
    }
    if (!toUpdate.isEmpty) updateCommercialGroups(toUpdate.map(_.commercialGroup))

    val toArchive = inDfp filterNot (group => isInImportedCommercialGroups(group) || group.isArchived)
    if (!toArchive.isEmpty) archiveCommercialGroups(toArchive.map(_.commercialGroup))
  }

  private def fetchCommercialGroupsWithArchivalStatus(orderId: Long): Seq[CommercialGroupWithArchivalStatus] = {
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

  private def insertCommercialGroups(orderId: Long, groups: Seq[CommercialGroup]) {
    println("Inserting " + groups)

    val lineItems = lineItemService.createLineItems {
      groups.map(group => LineItem(orderId, group)).toArray
    }
    for (lineItem <- lineItems) {
      println(s"Created line item ${lineItem.getId}: ${lineItem.getName}")
    }

    for (group <- groups) {
      val creativeIds = insertCreatives(group)
      setLineItemCreatives(lineItems(0).getId, creativeIds)
    }
  }

  private def updateCommercialGroups(groups: Seq[CommercialGroup]) {
    // TODO
    println("Updating " + groups)
  }

  private def archiveCommercialGroups(groups: Seq[CommercialGroup]) {
    println("Archiving " + groups)

    val action = new ArchiveLineItems()
    // TODO: include orderId
    val titles = groups.map(_.title).mkString("'", "', '", "'")
    val statement = new StatementBuilder().where(s"name in ($titles)").toStatement
    val updateResult = lineItemService.performLineItemAction(action, statement)
    println(s"Archived ${updateResult.getNumChanges} line items")
  }

  private def insertCreatives(group: CommercialGroup): Seq[Long] = {

    val toInsert = group.htmlSnippets.foldLeft(Array[Creative]()) {
      (acc, snippet) =>
        val creative = new CustomCreative
        creative.setName(s"${group.title} #${acc.size + 1}")
        creative.setSize(new Size(300, 250, false))
        creative.setAdvertiserId(advertiserId)
        creative.setDestinationUrl(group.destinationUrl)
        creative.setHtmlSnippet(snippet)
        acc :+ creative
    }

    val inserted = creativeService.createCreatives(toInsert)

    for (creative <- inserted) {
      println(s"Created creative ${creative.getId}: ${creative.getName}, preview URL [${creative.getPreviewUrl}]")
    }

    inserted.map(_.getId.toLong).toSeq
  }

  private def deleteCreative() {
    println("deleting creative")
  }

  private def setLineItemCreatives(lineItemId: Long, creativeIds: Seq[Long]) {
    val toCreate = creativeIds.foldLeft(Array[LineItemCreativeAssociation]()) {
      (acc, creativeId) =>
        val lica = new LineItemCreativeAssociation
        lica.setLineItemId(lineItemId)
        lica.setCreativeId(creativeId)
        acc :+ lica
    }

    val licas = licaService.createLineItemCreativeAssociations(toCreate)

    for (lica <- licas) {
      println(s"Created association between line item ${lica.getLineItemId} and creative ${lica.getCreativeId}")
    }
  }
}
