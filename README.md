# CobaltTester
CobaltTester is a simple tool to test and track cobalt instances. It uses a score system to determine how good an instance is. You can see it in action [here](https://instances.hyper.lol).

## How does it work?
It loads a list of instances, then performs various tests to see if they work. It then calculates a score on how many tests were successful.
You can see what tests it runs [here](https://github.com/hyperdefined/CobaltTester/blob/master/backend/test_urls).

Load instances -> Make sure API/frontend exist -> Perform tests -> Output results to `web/index.md`

## Contribute
* `backend` - Module that loads and tests the instances.
* `web` - Module for building the site, using Jekyll.
 
## License
This program is released under MIT License. See [LICENSE](https://github.com/hyperdefined/CobaltTester/blob/master/LICENSE).