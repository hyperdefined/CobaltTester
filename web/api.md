---
layout: home
permalink: /api/
title: "hyper's cobalt instance API"
description: "an api to access the instance list"
---
# hyper's cobalt instance API
This is a simple API to access the list of instances. You don't need to scrap the page itself.

To access it, you can use <code>https://instances.hyper.lol/instances.json</code>. Please set a proper user agent when making requests.

## Example
This returns a JSON array with all instances. In each instance, you have some information, like so:
```json
{
  "score": 85.71428571428571,
  "api_online": true,
  "protocol": "https",
  "cors": 1,
  "frontend_online": true,
  "commit": "eaf88fe",
  "name": "us3",
  "startTime": 1714180992438,
  "api": "co.wuk.sh",
  "version": "7.12.6",
  "branch": "temp-issues",
  "frontEnd": "cobalt.tools"
}
```

## Status
`frontend_online` and `api_online` are used to determine if parts of the instance is online/offline. `frontend_online` returns true if the frontend returns 200. `api_online` returns true if the API responds correctly to a POST request. This does not check all services for an instance. Some services might not work due to server location or missing cookies for some websites.