package model.commercial.dfp


object DfpServiceClient extends App {

  //val allOffers = Await.result(OffersApi.getAllOffers, 30.seconds)
  //DfpService.syncTravelOffers(allOffers.take(10))
  //DfpService.syncTravelOffers(Nil)
  //DfpService.stop

  //println("here")

  val travelOfferGroups = Seq(
    CommercialGroup(orderId, "traveloffer-germany", Target("travel", Some("germany")))
  )
  DfpService.syncCommercialGroups(orderId, travelOfferGroups)
  //val z = x.map(z => CommercialGroup(z))
  //println("z " + z)
}
