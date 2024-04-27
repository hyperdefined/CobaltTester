#!/bin/bash
export GEM_HOME="$HOME/gems"
export PATH="$HOME/gems/bin:$PATH"
cd /home/hyper/CobaltTester || exit
java -jar CobaltTester-1.0-SNAPSHOT.jar
cd /home/hyper/CobaltTester/web || exit
bundle exec jekyll build
cp /home/hyper/CobaltTester/backend/instances.json _site
sudo rm -r /var/www/instances.hyper.lol
sudo mv /home/hyper/CobaltTester/web/_site/ /var/www/instances.hyper.lol
sudo chown -R nginx:nginx /var/www/instances.hyper.lol/