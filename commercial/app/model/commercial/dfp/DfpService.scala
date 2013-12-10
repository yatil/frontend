package model.commercial.dfp

import com.google.api.ads.dfp.axis.v201311._
import com.google.api.ads.dfp.axis.utils.v201311.{DateTimes, StatementBuilder}
import org.joda.time.{Duration, Instant}
import scala.{Array, Option}
import common.Logging

object DfpService extends Logging {

  val lineItemService = dfpServices.get(session, classOf[LineItemServiceInterface])
  val networkService = dfpServices.get(session, classOf[NetworkServiceInterface])
  val targetingService = dfpServices.get(session, classOf[CustomTargetingServiceInterface])

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

  def toLineItem(commercialGroup: CommercialGroup): LineItem = {

    val targeting = {
      val rootAdUnitId = networkService.getCurrentNetwork.getEffectiveRootAdUnitId

      val adUnitTargeting = new AdUnitTargeting()
      adUnitTargeting.setAdUnitId(rootAdUnitId)
      adUnitTargeting.setIncludeDescendants(true)

      val inventoryTargeting = new InventoryTargeting()
      inventoryTargeting.setTargetedAdUnits(Array(adUnitTargeting))

      val travelSection = new CustomTargetingValue()
      travelSection.setCustomTargetingKeyId(sectionKeyId)
      travelSection.setDisplayName("Travel")
      travelSection.setName("travel")
      travelSection.setMatchType(CustomTargetingValueMatchType.EXACT)
      val travelSectionId = {
        val statement = new StatementBuilder().where("name = :name").withBindVariableValue("name", "travel").toStatement
        val results = targetingService.getCustomTargetingValuesByStatement(statement)
        if (results.getTotalResultSetSize.toInt == 0) {
          val values = targetingService.createCustomTargetingValues(Array(travelSection))
          values(0).getId
        } else {
          results.getResults(0).getId
        }
      }

      val customCriteria0 = new CustomCriteria()
      customCriteria0.setKeyId(sectionKeyId)
      customCriteria0.setValueIds(Array(travelSectionId))
      customCriteria0.setOperator(CustomCriteriaComparisonOperator.IS)

      val targeting = new Targeting()
      targeting.setInventoryTargeting(inventoryTargeting)

      commercialGroup.target.keyword match {
        case Some(keyword) => {
          val keywordId = {
            val statement = new StatementBuilder().where("name = :name").withBindVariableValue("name", keyword).toStatement
            val results = targetingService.getCustomTargetingValuesByStatement(statement)
            if (results.getTotalResultSetSize.toInt == 0) {
              val k = new CustomTargetingValue()
              k.setCustomTargetingKeyId(keywordKeyId)
              k.setName(keyword)
              k.setMatchType(CustomTargetingValueMatchType.EXACT)
              val values = targetingService.createCustomTargetingValues(Array(k))
              values(0).getId
            } else {
              results.getResults(0).getId
            }
          }
          val customCriteria1 = new CustomCriteria()
          customCriteria1.setKeyId(keywordKeyId)
          customCriteria1.setValueIds(Array(keywordId))
          customCriteria1.setOperator(CustomCriteriaComparisonOperator.IS)
          val customCriteriaSet = new CustomCriteriaSet()
          customCriteriaSet.setLogicalOperator(CustomCriteriaSetLogicalOperator.AND)
          customCriteriaSet.setChildren(Array(customCriteria0, customCriteria1))
          targeting.setCustomTargeting(customCriteriaSet)
        }
        case None =>
      }

      targeting
    }

    def fillRequiredProperties(lineItem: LineItem): LineItem = {
      lineItem.setLineItemType(LineItemType.STANDARD)
      lineItem.setOrderId(commercialGroup.orderId)
      lineItem.setName(commercialGroup.title)

      lineItem.setStartDateTimeType(StartDateTimeType.IMMEDIATELY)
      lineItem.setEndDateTime(DateTimes.toDateTime(Instant.now().plus(Duration.standardDays(30L)), "America/New_York"))

      lineItem.setTargeting(targeting)

      lineItem.setCostPerUnit(new Money("GBP", 2000000L))
      lineItem.setCostType(CostType.CPM)
      lineItem.setUnitsBought(500000L)

      val size = new Size()
      size.setWidth(300)
      size.setHeight(250)
      size.setIsAspectRatio(false)
      val creativePlaceholder = new CreativePlaceholder()
      creativePlaceholder.setSize(size)
      lineItem.setCreativePlaceholders(Array(creativePlaceholder))

      lineItem
    }

    val lineItem = new LineItem()
    fillRequiredProperties(lineItem)
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

    val lineItems = lineItemService.createLineItems(commercialGroups.map(toLineItem).toArray)

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


object Target {

  val targetingService = dfpServices.get(session, classOf[CustomTargetingServiceInterface])

  def apply(targeting: Targeting): Target = {
    val criteria = Option {
      targeting.getCustomTargeting
    }.map(_.getChildren).map(_.toSeq).getOrElse(Nil).map(_.asInstanceOf[CustomCriteriaSet])
      .flatMap {
      _.getChildren.map(_.asInstanceOf[CustomCriteria])
    }

    val targetValues = if (criteria.isEmpty) {
      Nil
    } else {
      val valueIds = criteria.map(_.getValueIds.mkString("'", "', '", "'")).mkString("', '")
      // TODO: use bind value
      val statement = new StatementBuilder().where(s"id in ($valueIds)").toStatement
      Option {
        targetingService.getCustomTargetingValuesByStatement(statement).getResults
      }.map(_.toSeq).getOrElse(Nil)
    }

    Target(
      section = targetValues.find(_.getCustomTargetingKeyId.toLong == sectionKeyId).map(_.getName).getOrElse("*"),
      keyword = targetValues.find(_.getCustomTargetingKeyId.toLong == keywordKeyId).map(_.getName)
    )
  }
}


case class Target(section: String, keyword: Option[String])


object CommercialGroup {

  def apply(lineItem: LineItem): CommercialGroup = {
    CommercialGroup(
      orderId = lineItem.getOrderId,
      title = lineItem.getName,
      target = Target(lineItem.getTargeting)
    )
  }
}


case class CommercialGroup(orderId: Long, title: String, target: Target)
