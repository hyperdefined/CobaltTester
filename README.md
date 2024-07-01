# CobaltTester
CobaltTester is a simple tool to test and track cobalt instances. It uses a score system to determine how good an instance is. You can see it in action [here](https://instances.hyper.lol).

## How does it work?
It loads a list of instances, then performs various tests to see if they work. It then calculates a score on how many tests were successful.
You can see what tests it runs [here](https://github.com/hyperdefined/CobaltTester/blob/master/backend/tests.json).

Load instances -> Make sure API/frontend exist -> Perform tests -> Output results to `web/index.md` -> Build site via Jekyll.

## Contributing
The project contains 2 parts:
* `backend` - Module that loads and tests the instances.
* `web` - Module for building the site, using Jekyll.

## How to use?
You can see a live demo at [instances.hyper.lol](https://instances.hyper.lol). You can also look at the [wiki](https://github.com/hyperdefined/CobaltTester/wiki/Running-CobaltTester).

### I want to add my instance to your site!
You can fork this repository, add your instance to `backend/instances`, and make a pull request!

If you need help running it on your end, please ping `hyperdefined` in the [cobalt discord](https://discord.gg/pQPt8HBUPu).

## License
This program is released under MIT License. See [LICENSE](https://github.com/hyperdefined/CobaltTester/blob/master/LICENSE).
