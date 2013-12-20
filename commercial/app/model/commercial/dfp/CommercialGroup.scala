package model.commercial.dfp

import com.google.api.ads.dfp.axis.v201311._
import com.google.api.ads.dfp.axis.utils.v201311.StatementBuilder

object CommercialGroup {

  def apply(lineItem: LineItem): CommercialGroup = {
    CommercialGroup(
      title = lineItem.getName,
      target = Target(lineItem.getTargeting)
    )
  }
}

case class CommercialGroup(title: String, target: Target)


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
