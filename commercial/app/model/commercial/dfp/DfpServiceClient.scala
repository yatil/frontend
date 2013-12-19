package model.commercial.dfp


object DfpServiceClient extends App {

  val travelOfferGroups = Seq(
    CommercialGroup(orderId, "traveloffer-spain", Target("travel", Some("spain")))
  )

  DfpService.syncCommercialGroups(orderId, travelOfferGroups)
}
