//> using scala 3.7.2
//> using dep com.lihaoyi::os-lib:0.11.5
//> using dep com.lihaoyi::mainargs:0.7.6
//> using dep net.ruippeixotog::scala-scraper:3.2.0
//> using dep org.seleniumhq.selenium:selenium-java:4.35.0
//> using dep org.seleniumhq.selenium:htmlunit-driver:4.13.0

// import mainargs.*
import mainargs.{ParserForMethods, TokensReader, arg, main}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.devtools.v139.network.Network
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxOptions}
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.devtools.NetworkInterceptor
import org.openqa.selenium.remote.http.{
  Filter,
  HttpHandler,
  HttpRequest,
  HttpResponse
}
import os.*

import java.time.Duration
import java.util.Optional
import java.util.concurrent.CopyOnWriteArrayList
import org.openqa.selenium.support.ui.WebDriverWait

implicit object PathRead extends TokensReader.Simple[os.Path]:
  def shortName = "path"

  def read(strings: Seq[String]) = Right(os.Path(strings.head, os.pwd))

type Name = String
type Url = String
case class Video(name: Name, masterJsonUrl: Url)

val recordingsRootUrl = "https://www.naplnenahojnost.cz/archiv-zivych-vysilani"
val chaptersRootUrl =
  "https://www.naplnenahojnost.cz/kapitola/37slovajezisovaknaplnenehojnosti?utm_source=newsletter&utm_medium=email&utm_campaign=Kapitola37&utm_content=campaign"

def doOne(url: String): Option[Video] =
  val options = new ChromeOptions()
  println("Hello")
  options.setPageLoadStrategy(PageLoadStrategy.NORMAL)
  val driver = new ChromeDriver(options)
  driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))

  val devTools = driver.getDevTools
  devTools.createSession()

  val filter: Filter = next =>
    req =>
      req.addHeader("cheese", "brie")
      val res = next.apply(req)
      res.addHeader("vegetable", "peas")

  new NetworkInterceptor(
    driver,
    new Filter:
      override def apply(t: HttpHandler): HttpHandler = ???
  )

  driver.get(url)
  ???

def scrapeRecordings(): Seq[Video] =
  val browser = JsoupBrowser()
  val doc = browser.get(recordingsRootUrl)

  val urls = doc >> elementList("iframe") >> attr("src")

  println(urls)
  println("Hello")

  doOne(urls.head)
  Seq()

def scrapeChapters(): Seq[Video] = ???

/** Does not check for already downloaded videos */
def downloadVideo(outDir: Path, video: Video, vimeoDlPath: Path): Boolean =
  val outFile = outDir / video.name / ".mp4"
  val call = os.call(
    (vimeoDlPath, "--combine", s"-i ${video.masterJsonUrl}", s"-o $outFile"),
    stdout = os.Inherit,
    stderr = os.Inherit
  )

  call.exitCode == 0

@main(
  name = "hojnost getter",
  doc =
    "Uses vimeo-dl to download all videos from a given url. Doesn't download already downloaded videos."
)
def run(
  @arg("output directory") outDir: Path = null,
  @arg("vimeo-dl path") vimeoDlPath: Path = null
): Unit =

  scrapeRecordings()

  ???
  require(os.isDir(outDir), "output directory must be a directory")
  require(os.isExecutable(vimeoDlPath), "vimeo-dl path must be an executable")

  val recordingsFolder = outDir / "live"
  os.makeDir.all(recordingsFolder)
  val chaptersFolder = outDir / "chapters"
  os.makeDir.all(chaptersFolder)

def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)
