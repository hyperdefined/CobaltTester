# CobaltTester
CobaltTester is a simple tool to test and track cobalt instances. It uses a score system to determine how good an instance is. You can see it in action [here](https://instances.hyper.lol).

## How does it work?
It loads a list of instances, then performs various tests to see if they work. It then calculates a score on how many tests were successful.
You can see what tests it runs [here](https://github.com/hyperdefined/CobaltTester/blob/master/backend/test_urls).

Load instances -> Make sure API/frontend exist -> Perform tests -> Output results to `web/index.md` -> Build site via Jekyll.

## Contributing
The project contains 2 parts:
* `backend` - Module that loads and tests the instances.
* `web` - Module for building the site, using Jekyll.

### Backend
For the backend, you will need Java 17+ and Maven installed. To build, use `mvn package`. This will create a jar file located in `backend/target/` (use the one called `CobaltTester-latest.jar`).

### Web
For web, you will need Jekyll installed. To build, use `bundle exec jekyll build`. The contents are placed into `_site`. The `template.md` is what the backend uses to create a `index.md`.

Due note: the web module has my own analytics included (using [umami](https://umami.is/)). You probably want to remove it.

### `run.sh` Script
In the root directory, there is a script to automate this process. Compile the jar and move it to `backend/CobaltTester-latest.jar`.

Afterwards, run it, and pass it a location of where you want the website output to be. Example: `bash run.sh /var/www/mysite.com`.

### I want to add my instance to your site!
You can fork this repository, add your instance to `backend/instances`, and make a pull request!

If you need help running it on your end, please ping `hyperdefined` in the [cobalt discord](https://discord.gg/pQPt8HBUPu).

## License
This program is released under MIT License. See [LICENSE](https://github.com/hyperdefined/CobaltTester/blob/master/LICENSE).
