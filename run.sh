#!/bin/bash
PROJECT_ROOT="$(pwd)"
BACKEND="$(pwd)/backend"
WEB="$(pwd)/web"
BUILD_WEB=false
WEB_DIR="$(pwd)/web-output"
WEB_TYPE="dev"

# Make sure using at least Java 21
if (( $(java -version 2>&1 | grep -Po '(?<=")[0-9]{2}') < 21 )); then
    echo "You must need at least Java 21 installed. See the wiki for help!"
    exit 1
fi

# Make sure maven is installed
if ! command -v mvn > /dev/null 2>&1; then
    echo "You must have Maven installed.  See the wiki for help!"
    exit 1
fi

# Check to see if we are building for web
if [ "$1" = "web" ]; then
    BUILD_WEB=true
    # Make sure jekyll is installed
    if ! command -v jekyll > /dev/null 2>&1; then
        echo "You must have Jekyll installed to build for web. See the wiki for help!"
        exit 1
    fi
	# Make sure type was specified
    if ! [ -n "$2" ]; then
        echo "You must specify either 'dev' or 'production' for the web build."
        exit 1
    fi
	WEB_TYPE="$2"
	# Make sure it's either dev or production
    if [[ "$WEB_TYPE" != "dev" && "$WEB_TYPE" != "production" ]]; then
        echo "Invalid web type specified. You must specify either 'dev' or 'production'."
        exit 1
    fi
	
    # If we are doing production, build the site into static files
    # We need to make sure an output folder was specified
	if [ "$WEB_TYPE" = "production" ]; then
    # Check for the output folder
		if [ -n "$3" ]; then
			if [ -d "$3" ]; then
				echo "Outputting web to $3"
                WEB_DIR="$3"
			else
				echo "The output folder does not exist, creating $3..."
				mkdir -p "$3"
			fi
		else
			echo "No output folder provided."
			exit 1
		fi
	fi
fi

# Pull all changes first
git pull

# Build the jar file
cd "$BACKEND" || exit
mvn clean package
echo "Finished building jar"
mv "$BACKEND/target/CobaltTester-latest.jar" "$BACKEND"

# Run the jar based on if we want to generate web or not
if [ "$BUILD_WEB" = true ]; then
    # Run jar
    java -jar "$BACKEND/CobaltTester-latest.jar" web
    echo "Finished running cobalt tests, building web..."
    # Copy instance.json for web
    cp "$BACKEND/instances.json" "$WEB"
    cd "$WEB" || exit
    # Make sure we have all dependencies
    bundle install
	# Build or serve based on what was provided
    if [ "$WEB_TYPE" = "production" ]; then
        export JEKYLL_ENV=production
        bundle exec jekyll build
        cd "$PROJECT_ROOT" || exit
        # Move static files into output folder
        find "$WEB_DIR" -mindepth 1 -delete
		mv "$WEB/_site"/* "$WEB_DIR/"
    else
        bundle exec jekyll serve
    fi
else
    # If we are not building for web, just run the jar only
    java -jar "$BACKEND/CobaltTester-latest.jar"
    echo "Finished running cobalt tests. Results are located at $BACKEND/instances.json"
fi