import org.scalatest.{FlatSpec, Matchers}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable

class Test extends FlatSpec with Matchers {

  // Test the name provided in the config file.
  "Configuration" should "have correct name" in {
    val config =
      ConfigFactory.parseResources("montecarlo.conf")
    val name = config.getString("conf.name")
    name should be("montecarlo")
  }

  // Test the number of simulations provided in the config file.
  "Configuration" should "have correct # of simulations" in {
    val config =
      ConfigFactory.parseResources("montecarlo.conf")
    val numSims = config.getInt("sims.numSims")
    assert(numSims == 1000)
  }

  // If the portfolio given is empty, then we expect an investment of
  // 0.
  "Empty history" should "return 0 in investment" in {
    val portfolioMap =
      mutable.Map[String, Float]("AAPL" -> 100f, "GOOGL" -> 250f)
    val tickers = Set[String]("AAPL", "GOOGL", "OIL")
    val history = List[(String, Array[Float])]()

    val investment = MonteCarlo.runSimulation(portfolioMap, tickers, history)
    assert(investment == 0)  
  }

  // The goal of this simulation is to measure the performance
  // of a starting portfolio. Under those constraints, it doesn't
  // make sense to start with an empty portfolio.
  "Empty portfolio" should "return 0 in investment" in {
    val portfolioMap = mutable.Map[String, Float]()
    val tickers = Set[String]("AAPL", "GOOGL", "OIL")
    val history = List[(String, Array[Float])](
      ("11-2-2019", Array[Float](0.3f, -0.24f, 0.23f)),
      ("11-3-2019", Array[Float](-0.3f, -0.10f, -0.05f)),
      ("11-4-2019", Array[Float](0.02f, 0.22f, 0.10f)),
      ("11-5-2019", Array[Float](-0.18f, 0.01f, -0.07f)),
      ("11-6-2019", Array[Float](0.07f, -0.02f, 0.04f)))
    val investment = MonteCarlo.runSimulation(portfolioMap, tickers, history)
    assert(investment == 0)
  }

  // If the investments given in the portfolio are each 0, then
  // we expect no gains/losses in the end.
  "Initial investments of 0" should "return 0 in investment" in {
    val portfolioMap = mutable.Map[String, Float]("AAPL" -> 0f, "GOOGL" -> 0f)
    val tickers = Set[String]("AAPL", "GOOGL", "OIL")
    val history = List[(String, Array[Float])](
      ("11-2-2019", Array[Float](0.02f, 0.22f, 0.10f)),
      ("11-3-2019", Array[Float](-0.18f, 0.01f, -0.07f)),
      ("11-4-2019", Array[Float](0.07f, -0.02f, 0.04f)))
    val investment = MonteCarlo.runSimulation(portfolioMap, tickers, history)
    assert(investment == 0)
  }

  // If the number of values in our historical data doesn't
  // match the number of tickers then we don't expect
  // the function to crash, but instead return 0.
  "More history values than tickers" should "should return 0 in investment" in {
    val portfolioMap = mutable.Map[String, Float]("AAPL" -> 10f, "GOOGL" -> 15f)
    val tickers = Set[String]("AAPL", "GOOGL")
    val history = List[(String, Array[Float])](
      ("11-2-2019", Array[Float](0.02f, 0.22f, 0.10f)),
      ("11-3-2019", Array[Float](-0.18f, 0.01f, -0.07f)),
      ("11-4-2019", Array[Float](0.07f, -0.02f, 0.04f)))
    val investment = MonteCarlo.runSimulation(portfolioMap, tickers, history)
    assert(investment == 0)
  }
  
  // If the number of tickers doesn't match
  // the number of historical values then we don't expect
  // the function to crash, but instead return 0.
  "More tickers than history values" should "should return 0 in investment" in {
    val portfolioMap = mutable.Map[String, Float]("AAPL" -> 10f, "GOOGL" -> 15f)
    val tickers = Set[String]("AAPL", "GOOGL", "OIL", "GE")
    val history = List[(String, Array[Float])](
      ("11-2-2019", Array[Float](0.02f, 0.22f, 0.10f)),
      ("11-3-2019", Array[Float](-0.18f, 0.01f, -0.07f)),
      ("11-4-2019", Array[Float](0.07f, -0.02f, 0.04f)))
    val investment = MonteCarlo.runSimulation(portfolioMap, tickers, history)
    assert(investment == 0)
  }

  // If the input given to the simulation function is valid, then we should
  // expect a valid result. This should be non-negative.
  "Valid input data" should "should not return a negative in investment" in {
    val portfolioMap = mutable.Map[String, Float]("AAPL" -> 10f, "GOOGL" -> 15f)
    val tickers = Set[String]("AAPL", "GOOGL", "OIL")
    val history = List[(String, Array[Float])](
       ("11-2-2019", Array[Float](0.02f, 0.22f, 0.10f)),
       ("11-3-2019", Array[Float](-0.18f, 0.01f, -0.07f)),
       ("11-4-2019", Array[Float](0.07f, -0.02f, 0.04f)))
    val investment = MonteCarlo.runSimulation(portfolioMap, tickers, history)
    assert(investment >= 0)
  }





}