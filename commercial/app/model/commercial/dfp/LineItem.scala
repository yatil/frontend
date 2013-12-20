package model.commercial.dfp

import com.google.api.ads.dfp.axis.v201311._
import com.google.api.ads.dfp.axis.utils.v201311.{DateTimes, StatementBuilder}
import org.joda.time.{Duration, Instant}

object LineItem {

  val networkService = dfpServices.get(session, classOf[NetworkServiceInterface])
  val targetingService = dfpServices.get(session, classOf[CustomTargetingServiceInterface])

  def apply(orderId: Long, commercialGroup: CommercialGroup): LineItem = {

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

    val lineItem = new LineItem()
    lineItem.setLineItemType(LineItemType.SPONSORSHIP)
    lineItem.setOrderId(orderId)
    lineItem.setName(commercialGroup.title)

    lineItem.setStartDateTimeType(StartDateTimeType.IMMEDIATELY)
    lineItem.setEndDateTime(DateTimes.toDateTime(Instant.now().plus(Duration.standardDays(30L)), "America/New_York"))

    lineItem.setTargeting(targeting)

    lineItem.setCostPerUnit(new Money("GBP", 2000000L))
    lineItem.setCostType(CostType.CPM)
    lineItem.setUnitsBought(1)

    val size = new Size()
    size.setWidth(300)
    size.setHeight(250)
    size.setIsAspectRatio(false)
    val creativePlaceholder = new CreativePlaceholder()
    creativePlaceholder.setSize(size)
    lineItem.setCreativePlaceholders(Array(creativePlaceholder))

    lineItem
  }
}
