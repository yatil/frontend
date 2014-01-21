package controllers

import play.api.mvc.Security.AuthenticatedBuilder

object IdentityAuthenticated extends AuthenticatedBuilder(req =>
  // TODO: validate token is genuine
  req.headers.get("X-GU-ID-Client-Access-Token")
)
