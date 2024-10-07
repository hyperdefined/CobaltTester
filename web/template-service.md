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
Community instances that have a domain. These are NOT OFFICIAL! Use the official instance above instead. <strong>Only use these when needed.</strong><br>
<input type="text" id="main-search" placeholder="Search instances..." onkeyup="onFilterChange('main-table', 'main-search', 'main-filter', 'slider-main')">
<select id="main-filter" onchange="onFilterChange('main-table', 'main-search', 'main-filter', 'slider-main')">
    <option value="all">All</option>
    <option value="safe">Safe</option>
    <option value="unknown">Unknown</option>
    <option value="not-trusted">Not Trusted</option>
</select>
<label for="slider-main">Score Filter:</label>
<input type="range" id="slider-main" min="0" max="100" value="0" oninput="onFilterChange('main-table', 'main-search', 'main-filter', 'slider-main')" />
<span id="slider-main-value">0%</span>
<service-table-domain>
</details>
### Community (no domain)
<details>
<summary>Show List</summary>
Community instances that do not have a domain. These are NOT OFFICIAL! Use the official instance above instead. <strong>Only use these when needed.</strong><br>
<input type="text" id="other-search" placeholder="Search instances..." onkeyup="onFilterChange('other-table', 'other-search', 'other-filter', 'slider-other')">
<select id="other-filter" onchange="onFilterChange('other-table', 'other-search', 'other-filter', 'slider-other')">
    <option value="all">All</option>
    <option value="safe">Safe</option>
    <option value="unknown">Unknown</option>
    <option value="not-trusted">Not Trusted</option>
</select>
<label for="slider-other">Score Filter:</label>
<input type="range" id="slider-other" min="0" max="100" value="0" oninput="onFilterChange('other-table', 'other-search', 'other-filter', 'slider-other')" />
<span id="slider-other-value">0%</span>
<service-table-nodomain>
</details>