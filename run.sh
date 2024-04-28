#!/bin/bash

if [ "$#" -eq 0 ]; then
    echo "Not arguments provided, please see the README.md!"
    exit 1
fi

# set these for jekyll
export GEM_HOME="$HOME/gems"
export PATH="$HOME/gems/bin:$PATH"

# Move into backend folder, run the jar
cd backend || exit
echo Running backend jar...
java -jar CobaltTester-1.0-SNAPSHOT.jar

# Move into web folder, build the site
cd ../web || exit
echo Building site...
bundle exec jekyll build

# Copy the instances.json to site output
echo Copying instances.json to site output...
cp ../web/instances.json _site
# Remove the old location
echo Deleting $1...
sudo rm -r $1

echo Moving _site to $1
# Move output folder to final location
sudo mv _site/ $1