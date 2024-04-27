---
permalink: /api/
title: "API Usage"
description: "an api to access the instance list"
---
This is a simple API to access the list of instances. You don't need to scrap the page itself.

To access it, you can use <code>https://instances.hyper.lol/instances.json</code>. This JSON file is created when the script runs.

Please set a proper user agent when making requests.

### Example
When calling the API, it will return a JSON array that contains all instances. Here is a snippet of what information an instance has:
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

### What are each keys?
This section explains what each key in the JSON mean for an instance.

Some of these keys are pulled directly from the API of an instance. You can view them yourself at `<API URL>/api/serverInfo`.
* `version`: The version the instance is running.
* `commit`: The commit the instance is running.
* `branch`: This the branch the instance is running.
* `name`: The "name" of the instance.
* `api`: The API URL of the instance.
* `cors`: If the instance has CORS enabled/disabled.
* `startTime`: The time the instance started up.
* `score`: The test score of the instance. This is a percentage.
* `api_online`: Whether or not the API is online.
* `frontend_online`: Whether or not the frontend is online.
* `protocol`: What protocol the instance uses (http/https).
* `frontEnd`: The frontend URL of the instance.