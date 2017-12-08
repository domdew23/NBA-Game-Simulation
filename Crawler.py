import requests
import os
from bs4 import BeautifulSoup

def spider(url):
	source_code = requests.get(url)
	text = source_code.text
	soup = BeautifulSoup(text, 'html.parser')

	headers = []
	data = []
	print(text)
	for th in soup.find_all('th'):
		header = th.text.strip()
		headers.append(header)
	for td in soup.find_all('td'):
		info = td.text.strip()
		data.append(info)
	print(headers)
	print(data)

spider('https://stats.nba.com/players/traditional/?sort=PTS&dir=-1&Season=2017-18&SeasonType=Regular%20Season&PerMode=Per48&StarterBench=Starters&TeamID=1610612752')

