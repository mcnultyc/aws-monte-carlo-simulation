import org.apache.spark.{SparkConf, SparkContext}

object MonteCarlo {
  def main(args: Array[String]): Unit = {

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
    val totalInvestments = portfolioRDD.reduce((x, y) => ("total", x._2 + y._2))._2.longValue()
    // Map each stock ticker in portfolio to its percentage of total investments
    val portfolioMap = portfolioRDD.map(x => (x._1, x._2/totalInvestments)).collectAsMap()

    // Load the text into a Spark RDD, which is a distributed representation of each line of text
    val stocks = sc.textFile("src/main/resources/stock_data.csv")

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
    val historySize = historyRDD.count()
    val numTrials = 1000


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