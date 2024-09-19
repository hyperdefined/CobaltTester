/*
    L     I  BBB    RRRR   EEEE  RRRR   I  N   N   GGGG
    L     I  B  B   R   R  E     R   R  I  NN  N  G
    L     I  BBBB   RRRR   EEE   RRRR   I  N N N  G  GG
    L     I  B   B  R   R  E     R   R  I  N  NN  G    G
    LLLLL I  BBBB   R   R  EEEE  R   R  I  N   N   GGGG 

    LIBRERING is a simple javascript webring script.
    It should be compatible with HTML and XHTML and supports rudimentary configuration options.
    
    Copyright 2023: Lian B. of Libre.Town
    
    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
    You should have received a copy of the GNU Lesser General Public License along with this program. If not, see <https://www.gnu.org/licenses/>. 
*/

// 路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路
// : ADMINISTRATOR SECTION :: This section contains configuration options :
// 路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路

// List of all members in the webring. Add onto this manually whenever you want to add someone new to the ring.
// Please take time to go through here and use the search-and-replace feature of your favorite text editor to change all instances of cobaltWebring to a lower-case or camel-case version of your webring name, as well as change the configuration to your liking.
var cobaltWebring_members = [
    'instances.hyper.lol',
    'hyper.lol',
    'lostdusty.dev.br',
    'canine.tools',
    'nat.envs.sh',
    'spax.zone'
];

// Various config options that should be self-documenting.
// Again, if you're hosting this Librering, please change all the instances of cobaltWebring to your particular webring name in lower case, and insert valid image URLs for the badges and navigation.
var cobaltWebring_ringurl = "https://instances.hyper.lol/webring"; // The URL of the webring itself, for contact and information purposes.
var cobaltWebring_badgeurl = "https://instances.hyper.lol/assets/img/cobalt_wr_home_animated.gif"; // The URL of the main badge of the webring; 88x31 recommended, but any size goes.
var cobaltWebring_prevurl = "https://instances.hyper.lol/assets/img/cobalt_wr_prev.png"; // The URL of the PREVIOUS badge of the webring; in the original design, a quarter of the main badge.
var cobaltWebring_nexturl = "https://instances.hyper.lol/assets/img/cobalt_wr_next.png"; // The URL of the NEXT badge of the webring; in the original design, a quarter of the main badge.
var cobaltWebring_randomurl = "https://instances.hyper.lol/assets/img/cobalt_wr_random.png"; // The URL of the RANDOM badge of the webring; in the original design, a quarter of the main badge.

// 路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路
// : DISPLAY SECTION :: This defines whatever happens on a member's individual site: most notably, inserting a little display. :
// 路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路路

// Before you can use this, please replace all instances of cobaltWebring with your webring name in lower case.
// This will allow you to have multiple webrings on the same site without them conflicting with each other. 
// Please also carefully read through these options to see what you can (and have to) change; the design, the layout and the links.
// For more information and a step-by-step tutorial, see: https://libre.town/creative/development/librering.xhtml

// ... Let's begin.

// For displaying messages of all kinds, as well as the working webring display, we want to keep our little HTML element in mind.
// Remember to insert your webring's name here, just like everywhere, if you host this webring.
var displayElement = document.getElementById("cobaltWebring"); 

// First of all, we want to check whether we are even a member of this particular webring, and if so, at which position.
var currentLocation = window.location.hostname;
var siteIndex = cobaltWebring_members.indexOf(currentLocation);

// If our current location is NOT in the webring, display an error message. The rest of the code only runs if the site has been found in the webring.
if (siteIndex == -1) {
    displayElement.innerHTML =
    "<p>Sorry! Looks like this domain is not in the webring, can't display it.</p>";
} else {
    // This is a readable (but technologically not very sound) way to loop around when you are either the first or last member of the webring.
    var beforeID;
    var afterID;
    if (siteIndex == 0) { beforeID = cobaltWebring_members.length - 1; }
    else { beforeID = siteIndex - 1; }
    if (siteIndex == cobaltWebring_members.length - 1) { afterID = 0; }
    else { afterID = siteIndex + 1; }
    
    // This chooses a random website from a copy of the member list.
    var randomID;
    randomID = Math.floor(Math.random() * cobaltWebring_members.length);

    // Now it is time to get to the meaty stuff. This will replace our little display container with the actual display content: a general badge, next/previous buttons, and a webring info and random link.
    // Remove, swap around or change these components as you see fit.
    displayElement.innerHTML =
    "<a href='https://" + cobaltWebring_members[beforeID] + "'><img alt='Previous' src='" + cobaltWebring_prevurl + "' /></a>" +
    "<a href='" + cobaltWebring_ringurl + "'><img alt='Badge: cobaltWebring webring' src='" + cobaltWebring_badgeurl + "' /></a>" +
    "<a href='https://" + cobaltWebring_members[randomID] + "'><img alt='Random' src='" + cobaltWebring_randomurl + "' /></a>" +
    "<a href='https://" + cobaltWebring_members[afterID] + "'><img alt='Next' src='" + cobaltWebring_nexturl + "' /></a>";
}