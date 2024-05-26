---
permalink: /faq/
title: "FAQ"
description: "questions and answers for the instance list"
---
### General
<details>
<summary>What is cobalt?</summary>
cobalt is an open-source media downloader. It supports a wide range of social media websites. No ads, tracking, or paywalls. It was created by <a href="https://github.com/imputnet/">imput</a>.
</details>

<details>
<summary>What is an "instance"?</summary>
An instance is simply another "copy" of cobalt. Because cobalt is open source, anyone can start up their own instance. However, the official cobalt instance is <code>cobalt.tools</code> and <code>api.cobalt.tools</code>. All others on this list are community created.
</details>

<details>
<summary>Why are multiple instances needed?</summary>
Simple: if the main cobalt instance goes offline, you can still use another instance. Decentralization is good. Servers can be located in different regions to bypass different region limits for media.
</details>

<details>
<summary>What is the difference between API and frontend?</summary>
The frontend is the pretty homescreen you see when you visit a cobalt instance. The API is another module that handles any download requests sent by the frontend. It does the processing and handling. When you enter a URL and download it, the frontend sends a request to the API, and it returns the media back.

If you're a regular user, you probably want to use the frontend.
</details>

### Instance List
<details>
<summary>How do I read the instance list?</summary>
There are 2 primary lists: with and without domains. They simply separate which instances have a domain attached to them.

On each list, it contains these columns:
<ul>
<li>Frontend: The frontend domain of the instance, the one you probably want to use. Not all have frontends.</li>
<li>API: The API domain for the instance. You can read how to use this API <a href="https://github.com/imputnet/cobalt/blob/current/docs/api.md">here</a>.</li>
<li>Version: The version of the instance.</li>
<li>Commit: The commit of the instance.</li>
<li>Branch: The branch of the instance.</li>
<li>Name: The "name" of the instance, which is set by the instance owner.</li>
<li>CORS: If the instance has CORS enabled. (1 = enabled, 0 = false.)</li>
<li>Score: The score result of the instance. The higher the percentage, the more services the instance supports.<ul><li>Not all services work on all instances. Some require special cookies/API keys to be set on their end. Some services also do not work in certain regions where the server is hosted.</li><li>Scores are curved!</li></ul></li>
<li>Status: The status of the instance.</li>
<ul>
<li>Online: Both frontend and API are online.</li>
<li>Partial: Either frontend and API are offline.</li>
<li>Offline: Both frontend and API are offline.</li>
</ul>
</ul>
</details>

<details>
<summary>How did you find these instances?</summary>
I wrote a post about it <a href="https://hyper.lol/post/4">here</a> on my blog. In short, I used "service scanners" to search for specific queries to find them. Some instances were requested to be added.
</details>

<details>
<summary>I want to add/remove my instance!</summary>
If you want to be added/removed, ping @hyperdefined on the <a href="https://discord.gg/pQPt8HBUPu">cobalt discord</a> or create a pull request <a href="https://github.com/hyperdefined/CobaltTester">here</a>.
</details>

<details>
<summary>Do you have an API?</summary>
Yes there is one! Visit the <a href="{{ site.url }}/api">API page</a> for more information.
</details>