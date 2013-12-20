package model.commercial.dfp


object DfpServiceClient extends App {

  val travelOfferGroups = Seq(
    CommercialGroup("spain", Target("travel", Some("spain"))),
    CommercialGroup("portugal", Target("travel", Some("portugal")))
  )

  val jobGroups = Seq(
    CommercialGroup("theologian", Target("business", Some("legal"))),
    CommercialGroup("farmer", Target("business", Some("media"))),
    CommercialGroup("consultant", Target("business", Some("arts")))
  )

  DfpService.syncCommercialGroups(travelOffersOrderId, travelOfferGroups)

  DfpService.syncCommercialGroups(jobsOrderId, jobGroups)
}
