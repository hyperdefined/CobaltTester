---
permalink: "/instances/"
title: "Instances"
description: "the master list of community cobalt instances. currently tracking <instance-count> instances."
layout: banner-included
---
This is the master list of instances. Currently tracking <instance-count> instances.

Last updated (UTC): <time>

### Official Instance
This is the official cobalt instance.
<main-table-official>
### Community (with domain)
Community instances that have a domain.
<details>
<summary>Show List</summary>
<input type="text" id="main-search" placeholder="Search instances..." onkeyup="searchTable('main-search', 'main-table')">
<main-table-domain>
</details>
### Community (no domain)
Community instances that do not have a domain.
<details>
<summary>Show List</summary>
<input type="text" id="other-search" placeholder="Search instances..." onkeyup="searchTable('other-search', 'other-table')">
<main-table-nodomain>
</details>
