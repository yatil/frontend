package model.commercial.dfp

import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets, GoogleCredential}
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.common.collect.Lists
import java.io.{InputStreamReader, BufferedReader}
import com.google.api.ads.common.lib.auth.GoogleClientSecretsBuilder
import com.google.api.ads.common.lib.auth.GoogleClientSecretsBuilder.Api
import com.google.api.ads.common.lib.exception.ValidationException

object GetRefreshToken extends App {

  val SCOPE = "https://www.google.com/apis/ads/publisher"

  val CALLBACK_URL = "urn:ietf:wg:oauth:2.0:oob"

  def getOAuth2Credential(clientSecrets: GoogleClientSecrets): GoogleCredential = {
    val authorizationFlow = new GoogleAuthorizationCodeFlow.Builder(
      new NetHttpTransport(),
      new JacksonFactory(),
      clientSecrets,
      Lists.newArrayList(SCOPE))
      .setAccessType("offline").build()

    val authorizeUrl =
      authorizationFlow.newAuthorizationUrl().setRedirectUri(CALLBACK_URL).build()
    System.out.println("Paste this url in your browser: \n" + authorizeUrl + '\n')

    System.out.println("Type the code you received here: ")
    val authorizationCode = new BufferedReader(new InputStreamReader(System.in)).readLine()

    val tokenRequest =
      authorizationFlow.newTokenRequest(authorizationCode)
    tokenRequest.setRedirectUri(CALLBACK_URL)
    val tokenResponse = tokenRequest.execute()

    val credential = new GoogleCredential.Builder()
      .setTransport(new NetHttpTransport())
      .setJsonFactory(new JacksonFactory())
      .setClientSecrets(clientSecrets)
      .build()

    credential.setFromTokenResponse(tokenResponse)

    credential
  }

  var clientSecrets: GoogleClientSecrets = null
  try {
    clientSecrets = new GoogleClientSecretsBuilder()
      .forApi(Api.DFP)
      .fromFile()
      .build()
  } catch {
    case e: ValidationException =>
      System.err.println(
        "Please input your client ID and secret into your ads.properties file, which is either "
          + "located in your home directory in your src/main/resources directory, or "
          + "on your classpath. If you do not have a client ID or secret, please create one in "
          + "the API console: https://code.google.com/apis/console#access")
      System.exit(1)
  }

  val credential = getOAuth2Credential(clientSecrets)

  System.out.printf("Your refresh token is: %s\n", credential.getRefreshToken)

  System.out.printf("In your ads.properties file, modify:\n\napi.dfp.refreshToken=%s\n",
    credential.getRefreshToken)
}

