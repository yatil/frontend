package model.commercial.dfp


object DfpServiceClient extends App {

  val travelOfferGroups = Seq(
    CommercialGroup("traveloffer-spain", Target("travel", Some("spain"))),
    CommercialGroup("traveloffer-portugal", Target("travel", Some("portugal")))
  )

  val jobGroups = Seq(
    CommercialGroup("job-spain", Target("business", Some("legal"))),
    CommercialGroup("job-portugal", Target("business", Some("media")))
  )

  DfpService.syncCommercialGroups(travelOffersOrderId, travelOfferGroups)

  DfpService.syncCommercialGroups(jobsOrderId, jobGroups)
}
