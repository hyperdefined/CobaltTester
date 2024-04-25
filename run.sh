#!/bin/bash
export GEM_HOME="$HOME/gems"
export PATH="$HOME/gems/bin:$PATH"
cd /home/hyper/CobaltTester/ || exit
rm -r _site
java -jar CobaltTester-1.0-SNAPSHOT.jar
bundle exec jekyll build
cp instances.json _site