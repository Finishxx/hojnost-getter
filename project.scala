//> using scala 3.7.2
//> using dep com.lihaoyi::os-lib:0.11.5
//> using dep com.lihaoyi::mainargs:0.7.6
//> using dep net.ruippeixotog::scala-scraper:3.2.0

import mainargs.*
import os.*

implicit object PathRead extends TokensReader.Simple[os.Path]:
  def shortName = "path"

  def read(strings: Seq[String]) = Right(os.Path(strings.head, os.pwd))

type Name = String
type Url = String
case class Video(name: Name, masterJsonUrl: Url)

def scrapeRecordings(): Seq[Video] = ???
def scrapeChapters(): Seq[Video] = ???

/** Does not check for already downloaded videos */
def downloadVideo(outDir: Path, video: Video, vimeoDlPath: Path): Boolean =
  val outFile = outDir / video.name / ".mp4"
  val call = os.call(
    (vimeoDlPath, "--combine", s"-i ${video.masterJsonUrl}", s"-o $outFile"),
    stdout = os.Inherit,
    stderr = os.Inherit
  )

  if call.exitCode == 0 then return true else false

@main(
  name = "hojnost getter",
  doc =
    "Uses vimeo-dl to download all videos from a given url. Doesn't download already downloaded videos."
)
def main(
  @arg("output directory") outDir: Path,
  @arg("vimeo-dl path") vimeoDlPath: Path
): Unit =

  require(os.isDir(outDir), "output directory must be a directory")

  val recordingsFolder = outDir / "live"
  os.makeDir.all(recordingsFolder)
  val chaptersFolder = outDir / "chapters"
  os.makeDir.all(chaptersFolder)
