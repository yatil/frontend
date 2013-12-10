package model.commercial.dfp

import com.google.api.ads.dfp.axis.factory.DfpServices
import com.google.api.ads.dfp.lib.client.DfpSession
import com.google.api.ads.dfp.axis.v201211.NetworkServiceInterface

object GetAllNetworks extends App {

  def runExample(dfpServices: DfpServices, session: DfpSession) {
    val networkService = dfpServices.get(session, classOf[NetworkServiceInterface])

    val networks = networkService.getAllNetworks

    networks foreach {
      network => println(s"Network with network code [${network.getNetworkCode}] and display name [${network.getDisplayName}] was found.")
    }

    println(s"Number of networks found: ${networks.length}")
  }

  runExample(dfpServices, session)
}
