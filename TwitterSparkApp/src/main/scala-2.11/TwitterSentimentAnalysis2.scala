/**
 * Created by Bharat on 9/22/2015.
 */

import com.cybozu.labs.langdetect.DetectorFactory
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.{Seconds, StreamingContext}
//import org.elasticsearch.spark._
import scala.util.Try

object TwitterSentimentAnalysis2 {

  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir","G:\\winutils")
//

    LogUtils.setStreamingLogLevels()

    DetectorFactory.loadProfile("src/main/resources/profiles")


    val filters = args

    System.setProperty("twitter4j.oauth.consumerKey", "mFwRMJuVGTAlDjS3VrJobxYgK")
    System.setProperty("twitter4j.oauth.consumerSecret", "ZsARLDmvpMRjY0B3I9LxoyAHvKt3GpZ0OsmhJ0zhnEqJyrPZkA")
    System.setProperty("twitter4j.oauth.accessToken", "2771116699-o0w3ZGW4lH9cnokp1JjDK1bmQjOeBMXWqz3bgnJ")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "Dzkbp7hQxc6NK3zoRJZ0CwrGks13D0LNJPkPWRLOfWtEQ")


    val sparkConf = new SparkConf().setAppName("TwitterSentimentAnalysis").setMaster("local[2]")

    val ssc = new StreamingContext(sparkConf, Seconds(1))

    val tweets = TwitterUtils.createStream(ssc, None, filters)

    tweets.print()

    tweets.foreachRDD{(rdd, time) =>
      rdd.map(t => {
        Map(
          "user"-> t.getUser.getScreenName,
          "created_at" -> t.getCreatedAt.toInstant.toString,
          "location" -> Option(t.getGeoLocation).map(geo => { s"${geo.getLatitude},${geo.getLongitude}" }),
          "text" -> t.getText,
          "hashtags" -> t.getHashtagEntities.map(_.getText),
          "retweet" -> t.getRetweetCount,
          "language" -> detectLanguage(t.getText).toUpperCase(),
          "sentiment" -> SentimentAnalysisUtils.detectSentiment(t.getText).toString
        )
      }).saveAsTextFile("src/main/resources/output2/TwitterSentimentaAnalysis.txt")//saveToEs("twitter/tweet")
      val topList = rdd.take(10)//10
      SocketClient.sendCommandToRobot( rdd.count() +" is the total tweets sentimentally analyzed with Spark ")
    }
    //val topList = rdd.toString.take(1)//10
//    val topList = rdd.take(10)//10
//    SocketClient.sendCommandToRobot( rdd.count() +" is the total tweets analyzed")
    ssc.start()
    ssc.awaitTermination()

  }

  def detectLanguage(text: String) : String = {

    Try {
      val detector = DetectorFactory.create()
      detector.append(text)
      detector.detect()
    }.getOrElse("unknown")

  }
}
