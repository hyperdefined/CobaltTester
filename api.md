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
    "cors": 1,
    "frontend_status": true,
    "commit": "825db84",
    "name": "us4",
    "startTime": 1711942963149,
    "api_status": true,
    "api": "co.wuk.sh",
    "version": "7.12.3",
    "branch": "awesome",
    "frontEnd": "cobalt.tools"
  },
```

## Status
`frontend_status` and `api_status` are used to determine if parts of the instance is online/offline. `frontend_status` returns true if the frontend returns 200. `api_status` returns true if the API reponds correctly to a POST request. This does not check all services for an instance. Some services might not work due to server location or missing cookies for some websites.