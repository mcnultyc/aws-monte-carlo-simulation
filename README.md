# Compiling data
1. `pip install alpha_vantage`
2. `pip install pandas`
3. `python download_histories.sh -d`
    * This will use the alpha vantage api to download 20 years of historical data 
    for each company listed in [companies_list.txt](companies_list.txt). The file 
    created will be named stock_data.csv.

The format of the input file will have the following format, where each value under 
the tickers represents the percent change per day.

|date|GOOGL|GE|AAPL|GS|OIL|
|-----|-----|-----|-----|-----|-----|
|2019-11-13|-1.52|-1.41|0.027|-0.55|1.37 |
|2019-11-14|-0.14|0.98|-0.99|-0.18|-1.95 |

The video tutorial showing how to deploy the spark application on amazon emr is provided
here: https://youtu.be/7AzEDFJIBeM.

# Unit Tests

There are 8 unit tests and they can be ran my entering `sbt test` in the terminal.

# Deploying to Amazon EMR

The video tutorial showing how to deploy the spark application on amazon emr is provided
here: https://youtu.be/7AzEDFJIBeM. You'll need to build the fat jar by entering
`sbt assembly` in the project directory. This will be the jar submitted to amazon emr.
Next you'll need to create an s3 bucket and upload the jar and input files. Lastly
you'll have to create a cluster and submit the spark application. The output file
is named [stats.txt](stats.txt).

# Monte Carlo Simulation

## The input

The input provided to the program is a csv with [stock data](src/main/resources/stock_data.csv) from the past 
20 years for the companies listed in [companies_list.txt](companies_list.txt). The portfolio used by
the simulations is in [portfolio.txt](src/main/resources/portfolio.txt). Each line of this file consists
of a company the user invested in, followed by the amount they've invested. The number of simulations that
will be ran will depend on the [configuration file](src/main/resources/montecarlo.conf).

## Simulations

The Monte Carlo financial simulation provided is fairly straightforward. 
The simulation goes day by day, and on each day decides whether to buy or
sell using a simple heuristic. If on a given day their investments cost them
money they randomly choose a company to sell, and then reinvest that money
in another randomly chosen company. If instead their investments gain them
money then they'll invest that money in a randomly selected stock. It will execute 
a 1000 of these simulations, a default which can be updated in the [config file](src/main/resources/montecarlo.conf).
These simulations will be parallelized using spark RDDs. Each simulation will output the value
of its investment. The program them computes statistics such as the 5th, 25th, 50th, 75th, 95th
percentiles, the mean, and standard deviation of the simulations ran.
