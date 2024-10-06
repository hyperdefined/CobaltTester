---
permalink: "/service/<service-slug>/"
title: "<service>"
description: "service status for <service> on all instances."
layout: banner-included
---
Service status for <service> on all instances.

Last updated (UTC): <time>

### Official Instance
<service-table-official>

### Community (with domain)
<details>
<summary>Show List</summary>
<input type="text" id="main-search" placeholder="Search instances..." onkeyup="searchTable('main-search', 'main-table')">
<service-table-domain>
</details>
### Community (no domain)
<details>
<summary>Show List</summary>
<input type="text" id="other-search" placeholder="Search instances..." onkeyup="searchTable('other-search', 'other-table')">
<service-table-nodomain>
</details>