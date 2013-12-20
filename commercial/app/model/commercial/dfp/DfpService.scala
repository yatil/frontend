package model.commercial.dfp

import com.google.api.ads.dfp.axis.v201311._
import com.google.api.ads.dfp.axis.utils.v201311.StatementBuilder
import java.util.Random

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

  private def insertCommercialGroups(orderId: Long, commercialGroups: Seq[CommercialGroup]) {
    println("Inserting " + commercialGroups)

    val creativeIds = insertCreatives()

    val lineItems = lineItemService.createLineItems {
      commercialGroups.map(group => LineItem(orderId, group)).toArray
    }
    for (lineItem <- lineItems) {
      println(s"Inserted line item ${lineItem.getId}: ${lineItem.getName}")
    }

    setLineItemCreatives(lineItems(0).getId, creativeIds)
  }

  private def updateCommercialGroups(commercialGroups: Seq[CommercialGroup]) {
    // TODO
    println("Updating " + commercialGroups)
  }

  private def archiveCommercialGroups(commercialGroups: Seq[CommercialGroup]) {
    println("Archiving " + commercialGroups)

    val action = new ArchiveLineItems()
    // TODO: include orderId
    val titles = commercialGroups.map(_.title).mkString("'", "', '", "'")
    val statement = new StatementBuilder().where(s"name in ($titles)").toStatement
    val updateResult = lineItemService.performLineItemAction(action, statement)
    println(s"Archived ${updateResult.getNumChanges} line items")
  }

  private def insertCreatives(): Seq[Long] = {
    val customCreative = new CustomCreative
    customCreative.setName("Custom creative #" + new Random().nextInt(Integer.MAX_VALUE))
    customCreative.setSize(new Size(300, 250, false))
    customCreative.setAdvertiserId(advertiserId)
    customCreative.setDestinationUrl("http://google.com")
    customCreative.setHtmlSnippet("<a href='%%CLICK_URL_UNESC%%%%DEST_URL%%'>" + "<img src='something'/>" + "</a><br>Click above for great deals!")

    val creatives = creativeService.createCreatives(Array(customCreative))

    for (creative <- creatives) {
      println(s"Created creative ${creative.getId}: ${creative.getName}, preview URL [${creative.getPreviewUrl}]")
    }

    creatives.map(_.getId.toLong).toSeq
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
