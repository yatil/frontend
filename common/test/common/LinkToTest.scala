package common

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import play.api.Play
import common.editions.Uk
import views.support.TestTrail
import play.api.test.FakeRequest
import play.api.mvc.RequestHeader
import model.{ApiContentWithMeta, Content}
import com.gu.openplatform.contentapi.model.{Content => ApiContent}
import org.joda.time.DateTime

class LinkToTest extends FlatSpec with Matchers {

  Play.unsafeApplication

  implicit val fakeRequest: RequestHeader = FakeRequest()

  implicit val edition = Uk

  object TestLinkTo extends LinkTo {
    override lazy val host = "http://www.foo.com"
  }

  "LinkTo" should "leave 'other' urls unchanged" in {
    val otherUrl = "http://somewhere.com/foo/bar.html?age=7#TOP"
    TestLinkTo(otherUrl, edition) should be (otherUrl)
  }

  it should "modify the host of Guardian urls" in {
    TestLinkTo("http://www.theguardian.com/foo/bar.html?age=7#TOP", edition) should be ("http://www.foo.com/foo/bar.html?age=7#TOP")
  }

  it should "editionalise the front url" in {
    TestLinkTo("http://www.theguardian.com", edition) should be ("http://www.foo.com/uk")
  }

  it should "editionalise the front path" in {
    TestLinkTo("/", edition) should be ("http://www.foo.com/uk")
  }

  it should "not modify protocol relative paths" in {
    TestLinkTo("//www.youtube.com/embed/jLoG-fNir0c?enablejsapi=1&version=3", edition) should be ("//www.youtube.com/embed/jLoG-fNir0c?enablejsapi=1&version=3")
  }

  it should "strip leading and trailing whitespace" in {
    TestLinkTo("  http://www.foo.com/uk   ", edition) should be ("http://www.foo.com/uk")
  }

  it should "give correct url based on trail" in {
    val testTrail = TestTrail("/media/2014/feb/07/much-doge")
    TestLinkTo(testTrail) should be ("http://www.foo.com/media/2014/feb/07/much-doge")
  }

  it should "give correct url based on content" in {
    val testContent = ApiContent("/media/2014/feb/07/much-doge", None, None, DateTime.now, "", "", "", elements=None)
    val content = Content(ApiContentWithMeta(testContent))
    TestLinkTo(content) should be ("http://www.foo.com/media/2014/feb/07/much-doge")
  }

}
