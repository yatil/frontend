package model.commercial.dfp


object DfpServiceClient extends App {

  val travelOfferGroups = Seq(
    CommercialGroup("spain",
      Target("travel", Some("spain")),
      Seq(""),
      ""
    ),
    CommercialGroup("portugal",
      Target("travel", Some("portugal")),
      Seq(""),
      ""
    )
  )

  val jobGroups = Seq(
    CommercialGroup("cleric",
      Target("business", Some("arts")),
      Seq("<link rel=\"stylesheet\" href=\"http://assets.guim.co.uk/stylesheets/commercial.474b54573415e1dfe383f04312006398.css\" /> <div class=\"commercial jobs-component\"> <div class=\"commercial__head\"> <h3 class=\"commercial__title\"> <a href=\"%OASToken%http://jobs.theguardian.com/\"> <span>Guardian <span class=\"em\">Jobs</span></span> </a> </h3> </div> <div class=\"commercial__body\"> <ul class=\"lineitems\"> <li class=\"lineitem\"> <h4 class=\"lineitem__title\"><a href=\"%OASToken%http://jobs.theguardian.com/job/4767123\">Culture Reporter</a></h4> <p class=\"lineitem__description\"> <img class=\"lineitem__image lineitem__image-logo\" src=\"http://jobs.theguardian.com/getasset/?uiAssetID=7B7E150B-29B3-4B51-BD8E-E968EA9D0EE6\" /> We are looking for a Culture Reporter to generate stories and cover breaking news relating to Popular Culture, Film and Music for the Guardian’s digital platforms and in print.</p> </li> </ul> </div> <div class=\"commercial__foot\"> <a class=\"commercial__link\" href=\"%OASToken%http://jobs.theguardian.com/\">Find your next Arts &amp; heritage job on Guardian Jobs <i class=\"i i-double-arrow-right-blue\"></i></a> </div> </div>"),
      "http://jobs.theguardian.com/"),
    CommercialGroup("gardener",
      Target("business", Some("media")),
      Seq("<link rel=\"stylesheet\" href=\"http://assets.guim.co.uk/stylesheets/commercial.474b54573415e1dfe383f04312006398.css\" /> <div class=\"commercial jobs-component\"> <div class=\"commercial__head\"> <h3 class=\"commercial__title\"> <a href=\"%OASToken%http://jobs.theguardian.com/\"> <span>Guardian <span class=\"em\">Jobs</span></span> </a> </h3> </div> <div class=\"commercial__body\"> <ul class=\"lineitems\"> <li class=\"lineitem\"> <h4 class=\"lineitem__title\"><a href=\"%OASToken%http://jobs.theguardian.com/job/4764506\">Senior Digital Designer</a></h4> <p class=\"lineitem__description\"> <img class=\"lineitem__image lineitem__image-logo\" src=\"http://jobs.theguardian.com/getasset/?uiAssetID=32496EBC-7B6E-402C-8A26-53BBFDC0FD3E\" /> FTC 12 Months. A highly creative role and fantastic opportunity for an experienced digital designer. You’ll be designing websites for a leading Broadcaster, so you’ll need to be creatively motivated with an excellent knowledge of digital design packages.</p> </li> </ul> </div> <div class=\"commercial__foot\"> <a class=\"commercial__link\" href=\"%OASToken%http://jobs.theguardian.com/\">Find your next Design job on Guardian Jobs <i class=\"i i-double-arrow-right-blue\"></i></a> </div> </div>"),
      "http://jobs.theguardian.com/"),
    CommercialGroup("archivist",
      Target("business", Some("legal")),
      Seq(
        "<link rel=\"stylesheet\" href=\"http://assets.guim.co.uk/stylesheets/commercial.474b54573415e1dfe383f04312006398.css\" /> <div class=\"commercial jobs-component\"> <div class=\"commercial__head\"> <h3 class=\"commercial__title\"> <a href=\"%OASToken%http://jobs.theguardian.com/\"> <span>Guardian <span class=\"em\">Jobs</span></span> </a> </h3> </div> <div class=\"commercial__body\"> <ul class=\"lineitems\"> <li class=\"lineitem\"> <h4 class=\"lineitem__title\"><a href=\"%OASToken%http://jobs.theguardian.com/job/4753149\">Practice Manager</a></h4> <p class=\"lineitem__description\"> <img class=\"lineitem__image lineitem__image-logo\" src=\"http://jobs.theguardian.com/getasset/?uiAssetID=12158D61-147F-4F5B-9D33-EFDD849240C8\" /> This is a truly rare opportunity for a former litigation lawyer or accomplished business person, with a demonstrated knowledge of the litigation and e-discovery process. This is a significant business owner role within a dynamic growth company.</p> </li> </ul> </div> <div class=\"commercial__foot\"> <a class=\"commercial__link\" href=\"%OASToken%http://jobs.theguardian.com/\">Find your next Housing job on Guardian Jobs <i class=\"i i-double-arrow-right-blue\"></i></a> </div> </div>",
        "<link rel=\"stylesheet\" href=\"http://assets.guim.co.uk/stylesheets/commercial.474b54573415e1dfe383f04312006398.css\" /> <div class=\"commercial jobs-component\"> <div class=\"commercial__head\"> <h3 class=\"commercial__title\"> <a href=\"%OASToken%http://jobs.theguardian.com/\"> <span>Guardian <span class=\"em\">Jobs</span></span> </a> </h3> </div> <div class=\"commercial__body\"> <ul class=\"lineitems\"> <li class=\"lineitem\"> <h4 class=\"lineitem__title\"><a href=\"%OASToken%http://jobs.theguardian.com/job/4764630\">Head of Business Development</a></h4> <p class=\"lineitem__description\"> <img class=\"lineitem__image lineitem__image-logo\" src=\"http://jobs.theguardian.com/getasset/?uiAssetID=6149F219-8FC1-4088-B03F-2A338C41A8D3\" /> This is a key post in the senior management team of the charity, reporting directly to the CEO. Leading a team of three paid staff plus volunteers, responsibilities include fundraising, membership, training, communications, IT and general operations.</p> </li> </ul> </div> <div class=\"commercial__foot\"> <a class=\"commercial__link\" href=\"%OASToken%http://jobs.theguardian.com/\">Find your next Charities job on Guardian Jobs <i class=\"i i-double-arrow-right-blue\"></i></a> </div> </div>"),
      "http://jobs.theguardian.com/"
    )
  )

  DfpService.syncCommercialGroups(travelOffersOrderId, travelOfferGroups)

  DfpService.syncCommercialGroups(jobsOrderId, jobGroups)
}
