import org.apache.spark.{SparkConf, SparkContext}
import scala.collection.Map
import scala.util.Random.shuffle
import scala.util.Random.nextInt
import scala.math.min

object MonteCarlo {

  def calculateGain(stockChanges: Set[(String, Float)],
                    portfolioMap: Map[String, Float]): Float ={
    // Calculate gain for day given the stock changes
    stockChanges.map(x => portfolioMap.getOrElse(x._1, 0.0f)*x._2/100.0f).sum
  }

  def exploreInvestment(): Unit = {

  }

  def main(args: Array[String]): Unit = {


    val mylist = List(1,2,3,4)

    val sample = shuffle(mylist).take(2)
    println(sample)
    val sample2 = shuffle(mylist).take(3)
    println(sample2)
    val sample3 = shuffle(mylist).take(1)
    println(sample3)

    //Create a SparkContext to initialize Spark
    val conf = new SparkConf()
    conf.setMaster("local")
    conf.setAppName("MonteCarlo")
    val sc = new SparkContext(conf)

    val portfolio = sc.textFile("src/main/resources/portfolio.txt")

    // Get symbols and investments from user
    val portfolioRDD = portfolio.map(line => {
      val columns = line.replace('$', ' ')
        .split(",").map(column => column.trim)
      (columns(0), columns(1).toFloat)
    })

    // Calculate total investments of portfolio
    val totalInvestments: Float = portfolioRDD.reduce((x, y) => ("total", x._2 + y._2))._2.longValue()
    // Map each stock ticker in portfolio to its percentage of total investments
    val portfolioMap = portfolioRDD.collectAsMap()

    // Load the text into a Spark RDD, which is a distributed representation of each line of text
    val stocks = sc.textFile("src/main/resources/stock_data.csv")
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

    historyRDD.foreach(x => {
      val (date, changes) = x
      // Group tickers and changes into tuples
      val stockChanges = tickers zip changes
      // Calculate gain for the day
      val gain = calculateGain(stockChanges, portfolioMap)

      // Use heuristic to decide to buy/sell
      if(gain < 0){
        // Get tickers currently invested in
        val keys = portfolioMap.keys.toSet
      }
      else if(gain > 0){
        // Get tickers currently invested in
        val keys = portfolioMap.keys.toSet
        // Get tickers with no investments
        val remTickers = shuffle(tickers.diff(keys))
        // Calculate # of simulations to run at this decision point
        val numSims = min(3, remTickers.size)
        // Choose tickers to invest in for each simulation
        val simsTickers = remTickers.take(numSims)

      }

      val investmentGain = totalInvestments + gain
      println(f"date: $date, change: $gain, current money: $investmentGain")
    })


    /*
    val input = "12-10-1999, 0.67, 0.89, 45.78,-90.84545"
    val columns = input.split(",").map(column => column.trim)
    val values = columns.slice(1, columns.size).map(column => column.toFloat)
    values.foreach(value => println(value))
    */
    /*
    //word count
    val counts = textFile.flatMap(line => line.split(" "))
      .map(word => (word, 1))
      .reduceByKey(_ + _)

    counts.foreach(println)
    System.out.println("Total words: " + counts.count());
    //counts.saveAsTextFile("/tmp/shakespeareWordCount")
    counts.saveAsTextFile("hdfs:///tmp/shakespeareWordCount")

     */
  }
}