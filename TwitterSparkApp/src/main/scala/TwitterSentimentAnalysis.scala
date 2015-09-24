/**
 * Created by Bharat on 9/21/2015.
 */
//import org.apache.spark.sql.hive._
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
//import org.apache.spark.sql.hive._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._


object TwitterSentimentAnalysis {
  System.setProperty("hadoop.home.dir","F:\\winutils")
  Logger.getRootLogger.setLevel(Level.WARN)
  //Class that needs to be registered must be outside of the main class

  case class Person(key: Int, value: String)

  def main(args: Array[String]) {

    //val filters = args
    //val filters = Array("ps3", "ps4", "playstation", "sony", "vita", "psvita")
    val filters = Array("food", "nutrition", "diet", "healthy", "diseasefree", "physician")
    //val filers = "ThisIsSparkStreamingFilter_100K_per_Second"

    val delimeter = "|"

    System.setProperty("twitter4j.oauth.consumerKey", "mFwRMJuVGTAlDjS3VrJobxYgK")
    System.setProperty("twitter4j.oauth.consumerSecret", "ZsARLDmvpMRjY0B3I9LxoyAHvKt3GpZ0OsmhJ0zhnEqJyrPZkA")
    System.setProperty("twitter4j.oauth.accessToken", "2771116699-o0w3ZGW4lH9cnokp1JjDK1bmQjOeBMXWqz3bgnJ")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "Dzkbp7hQxc6NK3zoRJZ0CwrGks13D0LNJPkPWRLOfWtEQ")

    System.setProperty("twitter4j.http.useSSL", "true")

    val conf = new SparkConf().setAppName("TwitterApp").setMaster("local[4]")//.set("spark.eventLog.enabled", "true")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(5))

    val tweetStream = TwitterUtils.createStream(ssc, None, filters)

    val tweetRecords = tweetStream.map(status =>
    {

      def getValStr(x: Any): String = { if (x != null && !x.toString.isEmpty()) x.toString + "|" else "|" }


      var tweetRecord =
        getValStr(status.getUser().getId()) +
          getValStr(status.getUser().getScreenName()) +
          getValStr(status.getUser().getFriendsCount()) +
          getValStr(status.getUser().getFavouritesCount()) +
          getValStr(status.getUser().getFollowersCount()) +
          getValStr(status.getUser().getLang()) +
          getValStr(status.getUser().getLocation()) +
          getValStr(status.getUser().getName()) +
          getValStr(status.getId()) +
          getValStr(status.getCreatedAt()) +
          getValStr(status.getGeoLocation()) +
          getValStr(status.getInReplyToUserId()) +
          getValStr(status.getPlace()) +
          getValStr(status.getRetweetCount()) +
          getValStr(status.getRetweetedStatus()) +
          getValStr(status.getSource()) +
          getValStr(status.getInReplyToScreenName()) +
          getValStr(status.getText())

      tweetRecord

    })

    tweetRecords.print

    tweetRecords.filter(t => (t.length > 0)).saveAsTextFiles("src/main/resources/output/TwitterSentimentaAnalysis.txt", "data")
  //  tweetRecords.filter(t => (t.getLength() > 0)).saveAsTextFiles("/user/hive/warehouse/social.db/tweeter_data/tweets", "data")
   // tweetRecords.filter(t => (t.getLength() > 0)).saveAsTextFiles("/user/hive/warehouse/social.db/tweeter_data/tweets", "data")
//	val topList = rdd.take(10)//10
//    SocketClient.sendCommandToRobot( rdd.count() +" is the total tweets analyzed")
    ssc.start()
    ssc.awaitTermination()
    ssc.stop()

  }

}