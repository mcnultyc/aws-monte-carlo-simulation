import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.{Map, mutable}
import scala.util.Random.shuffle

import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkClientException
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest

object MonteCarlo {

  def calculateGain(stockChanges: List[(String, Float)],
                    portfolioMap: mutable.Map[String, Float]): Float ={
    // Calculate gain for day given the stock changes
    stockChanges.map(x => portfolioMap.getOrElse(x._1, 0.0f)*x._2/100.0f).sum
  }

  def runSimulation(portfolioMap: mutable.Map[String, Float], tickers: Set[String],
                    history: List[(String, Array[Float])]): Float = {
    // Calculate total investments
    var totalInvestments = portfolioMap.values.toList.sum

    history.foreach(x => {
      val (date, changes) = x
      // Group tickers and changes into tuples
      val stockChanges = (tickers zip changes).toList
      // Calculate gain for the day
      val gain = calculateGain(stockChanges, portfolioMap)
      // Use heuristic to decide to buy/sell
      if(gain < 0){
        // Get ticker currently invested in
        val investedTickers = shuffle(portfolioMap.keys.toSet)
        if(investedTickers.size > 0){
          // Select a ticker to sell
          val droppedTicker = investedTickers.head
          // Get tickers with no investments, excluding ticker to be dropped
          val remTickers = (tickers -- investedTickers) - droppedTicker
          if(remTickers.size > 0){
            // Select a ticker to reinvest previous investment
            val newInvestedTicker = shuffle(remTickers).head
            // Transfer investment into selected ticker
            portfolioMap(newInvestedTicker) = portfolioMap(droppedTicker)
            portfolioMap(droppedTicker) = 0.0f
          }
        }
      }
      else if(gain > 0){
        // Get tickers currently invested in
        val investedTickers = portfolioMap.keys.toSet
        // Get tickers with no investments
        val remTickers = tickers -- investedTickers
        if(remTickers.size > 0){
          val newInvestedTicker = shuffle(remTickers).head
          // Reinvest gain into selected ticker
          portfolioMap(newInvestedTicker) = gain
        }
      }
      totalInvestments += gain
    })
    totalInvestments
  }

  def main(args: Array[String]): Unit = {

    //Create a SparkContext to initialize Spark
    val conf = new SparkConf()
    //conf.setMaster("local[4]")
    conf.setAppName("MonteCarlo")
    val sc = new SparkContext(conf)

        

    val portfolio = sc.textFile("s3n://wordcountanalysis3/portfolio.txt")

    // Get symbols and investments from user
    val portfolioRDD = portfolio.map(line => {
      val columns = line.replace('$', ' ')
        .split(",").map(column => column.trim)
      (columns(0), columns(1).toFloat)
    })

    // Calculate total investments of portfolio
    val totalInvestments: Float = portfolioRDD.reduce((x, y) => ("total", x._2 + y._2))._2.longValue()
    // Map each stock ticker in portfolio to its percentage of total investments
    val map = portfolioRDD.collectAsMap()
    val portfolioMap = mutable.Map[String, Float]() ++= map

    // Load the text into a Spark RDD, which is a distributed representation of each line of text
    val stocks = sc.textFile("s3n://wordcountanalysis3/stock_data.csv")
    // Get the header from the stocks data
    val header = stocks.first.split(",").map(column => column.trim)
    // Get tickers from header
    val tickers = header.slice(1, header.size).toSet

    // Convert stock data from csv to date and values
    val historyRDD = stocks.flatMap(line => {
      val columns = line.split(",").map(column => column.trim)
      // Skip the header of the file
      if(columns.contains("date")){
        List()
      }
      else {
        // Convert percent change in price to floats
        val values = columns.slice(1, columns.size).map(column => column.toFloat)
        List((columns(0), values))
      }
    })
    // Convert history RDD to scala list
    val history = historyRDD.toLocalIterator.toList;
    // Execute simulations in parallel and sort the results in ascending order
    val trials = sc.parallelize(1 to 1000, 100)
      .map(i => runSimulation(portfolioMap.clone(), tickers, history))
      .sortBy(x => x, true)


    val size = 1000
    // Group RDD index with their corresponding values
    val trialLookup = trials.zipWithIndex().map(x => (x._2, x._1))
    // List of percentiles for statistics
    val percentiles = List(0.05, 0.25, 0.5, 0.75, 0.95)
    percentiles.foreach(x =>{
      // Calculate index for percentile
      val index = (x * size.toFloat).toInt
      val percentile = x * 100
      val totalInvestment = trialLookup.lookup(index).head
      println(s"$percentile percentile: $totalInvestment$$")
    })

    trials.saveAsTextFile("s3n://wordcountanalysis3/output.txt")
  }
}
