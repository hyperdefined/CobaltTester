function sortTable(n, event) {
    var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;
    table = event.target.closest("table"); // Get the closest table from the header clicked
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
}

function convertEmojiToNumber(text) {
    if (text.includes('✅')) return 1;
    if (text.includes('❌')) return 0;
    return text;
}

function searchTable(searchInputId, tableId) {
    var input = document.getElementById(searchInputId);
    var filter = input.value.toLowerCase();
    var table = document.getElementById(tableId);
    var rows = table.getElementsByTagName("tr");

    for (var i = 1; i < rows.length; i++) {
        var cells = rows[i].getElementsByTagName("td");
        var shouldDisplay = false;

        for (var j = 0; j < cells.length; j++) {
            var cell = cells[j];
            var txtValue = cell.textContent || cell.innerText;

            if (txtValue.toLowerCase().indexOf(filter) > -1) {
                shouldDisplay = true;
                break;
            }
        }
        rows[i].style.display = shouldDisplay ? "" : "none";
    }
}