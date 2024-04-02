---
layout: home
permalink: /api/
title: "hyper's cobalt instance API"
description: "an api to access the instance list"
---
# hyper's cobalt instance API
This is an "API" to access the list of instances. You don't need to scrap the page itself.

To access it, you can use <code>https://instances.hyper.lol/instances.json</code> to see the instances.

This returns a JSON array with all instances. In each instance, you have some information, like so:
```json
{
    "cors": 1,
    "commit": "520eb9b",
    "name": "us4",
    "startTime": 1711126855368,
    "api": "co.wuk.sh",
    "version": "7.12.1",
    "branch": "current",
    "frontEnd": "cobalt.tools",
    "status": true
},
```
The status simply returns true/false if the API returns something back. If it's unable to reach it, it will return false. Due note: not all instances will support all services.

Please set a proper user agent when making requests..