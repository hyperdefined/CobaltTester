---
permalink: /api/
title: "API Usage"
description: "an api to access the instance list"
---
This is a simple API to access the list of instances. You don't need to scrap the page itself.

To access it, you can call <code>https://instances.hyper.lol/instances.json</code>. This JSON file is created when the script runs. The script runs at the top of every hour and completes within 20 minutes.

Please set a proper user agent when making requests! I block common user agents (like `curl`, `python-requests`, `Go-http-client` etc) due to many bots hitting this server. I want to know what project or who is using this API!

### Example
When calling the API, it will return a JSON array that contains all instances. Here is a snippet of what information an instance has:
<details>
<summary>Show Example</summary>
{% highlight json %}
{
  "trust": "safe",
  "api_online": true,
  "cors": 1,
  "commit": "05ba1f0",
  "services": {
    "youtube": true,
    "facebook": false,
    "rutube": true,
    "tumblr": true,
    "bilibili": false,
    "pinterest": true,
    "instagram": true,
    "soundcloud": true,
    "youtube_music": true,
    "odnoklassniki": true,
    "dailymotion": true,
    "snapchat": true,
    "twitter": true,
    "loom": true,
    "vimeo": true,
    "streamable": true,
    "vk": true,
    "tiktok": true,
    "reddit": true,
    "twitch_clips": true,
    "youtube_shorts": true,
    "vine": true
  },
  "version": "7.15",
  "branch": "current",
  "score": 90.9090909090909,
  "protocol": "https",
  "name": "kityune",
  "startTime": 1724088311229,
  "api": "api.cobalt.tools",
  "frontEnd": "cobalt.tools"
},
{% endhighlight %}
</details>
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
* `protocol`: What protocol the instance uses (http/https).
* `frontEnd`: The frontend URL of the instance.
* `trust`: If the instance is considered "trusted" or not.

### Services
In the services JSON, each service is listed. If it's able to download media for it, it's set to `true`. If it failed, it's set to `false`.