// Function to apply all filters
function applyFilters(tableId, searchInputId, dropdownId, sliderId) {
    const input = document.getElementById(searchInputId);
    const dropdown = document.getElementById(dropdownId);
    const slider = document.getElementById(sliderId);

    const filterText = input.value.toLowerCase();
    const filterDropdown = dropdown.value;
    const scoreValue = parseInt(slider.value, 10);

    const valueDisplayId = sliderId + "-value";
    const valueDisplay = document.getElementById(valueDisplayId);
    if (valueDisplay) {
        valueDisplay.textContent = scoreValue + "%";
    }

    const table = document.getElementById(tableId);
    const rows = table.getElementsByTagName("tr");

    for (let i = 1; i < rows.length; i++) {
        const row = rows[i];
        const cells = row.getElementsByTagName("td");
        let shouldDisplay = true;

        // Filter by search input
        if (filterText) {
            shouldDisplay = Array.from(cells).some(cell => {
                const txtValue = cell.textContent || cell.innerText;
                return txtValue.toLowerCase().indexOf(filterText) > -1;
            });
        }

        // Filter by dropdown
        if (filterDropdown !== "all") {
            const rowClass = row.className;
            shouldDisplay = shouldDisplay && (rowClass === filterDropdown);
        }

        // Filter by score
        const scoreCell = cells[7];
        if (scoreCell) {
            const score = parseInt(scoreCell.textContent.replace('%', '').trim(), 10) || 0;
            shouldDisplay = shouldDisplay && (score >= scoreValue);
        }

        row.style.display = shouldDisplay ? "" : "none";
    }
}

function onFilterChange(tableId, searchInputId, dropdownId, sliderId) {
    applyFilters(tableId, searchInputId, dropdownId, sliderId);
}

function convertEmojiToNumber(text) {
    if (text.includes('✅')) return 1;
    if (text.includes('❌')) return 0;
    return text;
}

function sortTable(n, event, searchInputId, dropdownId, sliderId) {
    var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;
    table = event.target.closest("table");
    switching = true;
    dir = "asc";

    while (switching) {
        switching = false;
        rows = table.rows;

        for (i = 1; i < (rows.length - 1); i++) {
            shouldSwitch = false;

            x = rows[i].getElementsByTagName("TD")[n].innerText.toLowerCase();
            y = rows[i + 1].getElementsByTagName("TD")[n].innerText.toLowerCase();

            x = convertEmojiToNumber(x);
            y = convertEmojiToNumber(y);

            let xValue = parseFloat(x.replace('%', '')) || x;
            let yValue = parseFloat(y.replace('%', '')) || y;

            if (dir === "asc") {
                if (xValue > yValue) {
                    shouldSwitch = true;
                    break;
                }
            } else if (dir === "desc") {
                if (xValue < yValue) {
                    shouldSwitch = true;
                    break;
                }
            }
        }

        if (shouldSwitch) {
            rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
            switching = true;
            switchcount++;
        } else {
            if (switchcount === 0 && dir === "asc") {
                dir = "desc";
                switching = true;
            }
        }
    }
    console.log(searchInputId)
    console.log(dropdownId)
    console.log(sliderId)
    applyFilters(table.id, searchInputId, dropdownId, sliderId);
}