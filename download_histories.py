from alpha_vantage.timeseries import TimeSeries
import os
import time
import pandas as pd
import argparse as ap
import sys
from functools import reduce

def download_history(companies):
  
  key='CJEJW8HFG5HCFZU5'
  ts = TimeSeries(key, output_format='pandas')
  
  companies = companies[:500] # api call limit 500
  # create filenames to be saved in current directory
  filenames = [os.path.join(os.getcwd(), company + '.csv') for company in companies]
  
  dataframes = []
  counter = 0
  for company, filename in zip(companies, filenames):

    if counter >= 1 and counter % 5 == 0:
      time.sleep(60); # api call frequency is 5 per minute
    # read 100 days of stock data for company
    dataframe, meta = ts.get_daily(symbol=company)
    dataframe.to_csv(index=True, path_or_buf=filename)
    dataframes.append(dataframe)
    counter += 1
  
  return filenames, dataframes

if __name__ == '__main__':

  parser = ap.ArgumentParser(description='Compile stock input.')
  parser.add_argument('-d', '--download', help='download histories', action='store_true')
  parser.add_argument('-c', '--store-change', help='store percentage change per company in csv', action='store_true')
  parser.add_argument('-f', '--companies-file', help='read companies from specific file', type=ap.FileType('r'))    
  args = parser.parse_args()
    
  if args.companies_file:
    # read companies from user specified file
    lines = args.companies_file.readlines()
    companies = [line.strip() for line in lines]
    filenames = [os.path.join(os.getcwd(), company + '.csv') for company in companies]  
  else:
    # read companies from default file
    with open('companies_list.txt') as f:
      lines = f.readlines()
      companies = [line.strip() for line in lines]
      filenames = [os.path.join(os.getcwd(), company + '.csv') for company in companies] 

  dataframes = []
  if args.download:
    print('downloading...')    
    filenames, dataframes = download_history(companies)
    for filename in filenames[:10]: # limit to list 10 files
      print(filename)
  else:
    # read data frames from corresponding csv files
    dataframes = [pd.read_csv(filename, index_col='date') for filename in filenames]

  print('compiling...')
  for df, company in zip(dataframes, companies):
    # drop every column but open
    df.drop(['5. volume','4. close','2. high','3. low'], axis=1, inplace=True)
    df.columns = ['open'] # rename open column
    # calculate percent change from previous day on record
    df[company] = (df['open'] - df['open'].shift(1))/df['open'].shift(1) * 100.0
    df.drop(['open'], axis=1, inplace=True)
    # drop first row, no percent change on first day
    df.drop(df.index[:1], inplace=True)
    # optionally store each companies percent change in it's own file
    if args.store_change:
      filename = os.path.join(os.getcwd(), company + '_change.csv')
      df.to_csv(index=True, path_or_buf=filename)
 
  filename = os.path.join(os.getcwd(), 'stock_data.csv')
  print(filename)
  # merge data frames based on the dates
  df_merged = reduce(lambda  left,right: pd.merge(left,right,on=['date'], how='outer'), dataframes) 
  df_merged.to_csv(index=True, path_or_buf=filename)
