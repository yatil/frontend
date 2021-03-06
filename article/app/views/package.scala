package views

import common.Edition
import model.Article
import play.api.mvc.RequestHeader
import support._

object BodyCleaner {
  def apply(article: Article, html: String)(implicit request: RequestHeader) = withJsoup(BulletCleaner(html))(
    InBodyElementCleaner,
    UnindentBulletParents,
    PictureCleaner(article.bodyImages),
    InBodyLinkCleaner("in body link")(Edition(request)),
    BlockNumberCleaner,
    TweetCleaner,
    WitnessCleaner,
    VideoEmbedCleaner(article.bodyVideos),
    new TagLinker(article),
    InlineSlotGenerator(article.wordCount)
  )
}
