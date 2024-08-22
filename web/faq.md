---
permalink: /faq/
title: "FAQ"
description: "questions and answers for the instance list."
---
### General
<details>
<summary>What is cobalt?</summary>
cobalt is an open-source media downloader. It supports a wide range of social media websites. No ads, tracking, or paywalls. It was created by <a href="https://github.com/imputnet/">imput</a>.
</details>

<details>
<summary>What is an "instance"?</summary>
An instance is simply another "copy" of cobalt. Because cobalt is open source, anyone can start up their own instance. Each entry on the tracker is an instance of cobalt.
</details>

<details>
<summary>What is this tracker used for?</summary>
This site is used to track these instances. It uses a score system to determine which community instance (and official one) are the best. It allows users to use other instances if the official one goes offline or has issues.
<br><br>
The official instance sees <i>a lot</i> of traffic, so some services may be blocked. Using other instances until the official is fixed is the idea.
</details>

<details>
<summary>What is the difference between official and community instances?</summary>
Official instance is the main cobalt instance by the developers. This instance is <code>cobalt.tools</code>, and the API is <code>api.cobalt.tools</code>. All others on this list are community hosted and might have their own quirks.
</details>

<details>
<summary>What is the difference between API and frontend?</summary>
The frontend is the web app you see when you visit a cobalt instance. The API is another module that handles any download requests sent by the frontend. It does the processing and handling. When you enter a URL and download it, the frontend sends a request to the API, and it returns the media back.
<br><br>
If you're a regular user, you want to use the frontend. If you want to use the API for your own reasons, you then would use the API of an instance.
</details>

### Instance List
<details>
<summary>How do I read the instance list?</summary>
There's a few ways to see the instances, by the master list or by service.

<ul>
<li><a href="{{ site.url }}/instances/">Master list</a>: see all cobalt instances.</li>
<li><a href="{{ site.url }}/service/">By service</a>: see what services work on what instances.</li>
</ul>

When viewing each list, there are 3 categories: official, domain, and no domain.
<ul>
<li>Official - the main official cobalt instance by the developers.</li>
<li>Domain - instances that have a domain.</li>
<li>No domain - instances that do not have a domain, and just use an IP to connect. These are not secure.</li>
</ul>
</details>

<details>
<summary>What does each column mean on the master list?</summary>
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
</ul>
</details>

<details>
<summary>Why are some different colors on the list?</summary>
Each color simply refers to what trust level the instance is. There is a key at the top of the page of instances and on the instance page itself. A trust level helps users know which instances are "good" or "bad."
<br><br>
Safe instances are generally better to use, as they don't contain malicious code, tracking, or break the cobalt license itself. These are selected by the owner of this site based on the community.
</details>

<details>
<summary>How did you find these instances?</summary>
I wrote a post about it <a href="https://hyper.lol/blog/2024/05-03-hunting-down-cobalt-instances/">here</a> on my blog. In short, I used "service scanners" to search for specific queries to find them. Some instances were requested to be added.
</details>

<details>
<summary>I want to add/remove my instance!</summary>
If you want to be added/removed, ping @hyperdefined on the <a href="https://discord.gg/pQPt8HBUPu">cobalt discord</a> or create a pull request <a href="https://github.com/hyperdefined/CobaltTester">here</a>.
</details>

<details>
<summary>Do you have an API?</summary>
Yes there is one! Visit the <a href="{{ site.url }}/api">API page</a> for more information.
</details>