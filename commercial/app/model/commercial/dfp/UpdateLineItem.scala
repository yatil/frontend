package model.commercial.dfp

import com.google.api.ads.dfp.axis.factory.DfpServices
import com.google.api.ads.dfp.lib.client.DfpSession
import com.google.api.ads.dfp.axis.v201311.{LineItemType, LineItemServiceInterface}

object UpdateLineItem extends App {

  def runExample(dfpServices: DfpServices, session: DfpSession) {
    val lineItemService = dfpServices.get(session, classOf[LineItemServiceInterface])

    val lineItem = lineItemService.getLineItem(81831993)

    if (lineItem.getLineItemType == LineItemType.STANDARD) {
      lineItem.setPriority(6)

      val lineItems = lineItemService.updateLineItems(Array(lineItem))

      lineItems foreach {
        updatedLineItem => {
          println(s"Line item with ID [${updatedLineItem.getId}] and name [${updatedLineItem.getName}] was updated.")
        }
      }
    }
  }

  runExample(dfpServices, session)
}
