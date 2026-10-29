/**
 * bill-form.js
 * - Autocomplete with keyboard navigation
 * - Live amount calculation
 * - A new row appears automatically when Qty is typed in the LAST row
 * - Deleting a row that has data asks for confirmation (rows without buyer and qty don't)
 * - Tab from Rate jumps to the Buyer of the next row
 * - Rows are checked before saving (date, buyer, type, qty, rate)
 * - Date, type and rate are carried over from the previous row
 */
document.addEventListener('DOMContentLoaded', function () {

    // =========================================================
    // HELPERS
    // =========================================================
    function getLastFilledDate() {
        var lastDate = '';
        document.querySelectorAll('#itemsBody tr').forEach(function (row) {
            var d = row.querySelector('input[name*=".itemDate"]');
            if (d && d.value.trim() !== '') lastDate = d.value.trim();
        });
        return lastDate;
    }

    function getLastFilledType() {
        var lastType = '';
        document.querySelectorAll('#itemsBody tr').forEach(function (row) {
            var t = row.querySelector('input[name*=".materialType"]');
            if (t && t.value.trim() !== '') lastType = t.value.trim();
        });
        return lastType;
    }

    // =========================================================
    // AUTOCOMPLETE
    // =========================================================
    function attachAutocomplete(input) {
        var url = input.dataset.url;
        if (!url) return;

        var wrapper = input.closest('.position-relative') || input.parentElement;
        var list = wrapper.querySelector('.ac-list');
        if (!list) {
            list = document.createElement('ul');
            list.className = 'ac-list d-none list-unstyled mb-0';
            wrapper.appendChild(list);
        }

        // Store active index directly on the input element
        input._acIndex = -1;
        var timer;

        function getItems() {
            return list.querySelectorAll('li');
        }

        function clearHighlight() {
            getItems().forEach(function (li) {
                li.style.background = '';
                li.style.color      = '';
                li.style.fontWeight = '';
            });
        }

        function highlightItem(index) {
            var items = getItems();
            if (!items.length) return;
            // clamp
            if (index < 0)             index = 0;
            if (index >= items.length) index = items.length - 1;
            input._acIndex = index;
            clearHighlight();
            items[input._acIndex].style.background = 'var(--tint)';
            items[input._acIndex].style.color      = 'var(--red)';
            items[input._acIndex].style.fontWeight = '600';
        }

        function selectCurrent() {
            var items = getItems();
            if (input._acIndex >= 0 && input._acIndex < items.length) {
                input.value    = items[input._acIndex].textContent;
                input._acIndex = -1;
                list.innerHTML = '';
                list.classList.add('d-none');
                // move focus to Type field in same row
                var row       = input.closest('tr');
                var typeField = row ? row.querySelector('input[name*=".materialType"]') : null;
                if (typeField) {
                    setTimeout(function () { typeField.focus(); }, 0);
                }
                return true;
            }
            return false;
        }

        function isOpen() {
            return !list.classList.contains('d-none') && getItems().length > 0;
        }

        // ── KEY DOWN - use capture phase to intercept Enter before form ──
        input.addEventListener('keydown', function (e) {
            if (e.key === 'ArrowDown') {
                if (!isOpen()) return;
                e.preventDefault();
                e.stopPropagation();
                highlightItem(input._acIndex + 1);
                return;
            }

            if (e.key === 'ArrowUp') {
                if (!isOpen()) return;
                e.preventDefault();
                e.stopPropagation();
                highlightItem(input._acIndex - 1);
                return;
            }

            if (e.key === 'Enter') {
                if (isOpen()) {
                    // Always stop Enter when list is open
                    e.preventDefault();
                    e.stopPropagation();
                    if (input._acIndex >= 0) {
                        selectCurrent();
                    } else {
                        // Nothing highlighted - highlight first item
                        highlightItem(0);
                    }
                    return;
                }
                // List is closed - let Enter pass through normally
            }

            if (e.key === 'Escape') {
                list.classList.add('d-none');
                input._acIndex = -1;
                return;
            }

            if (e.key === 'Tab') {
                if (isOpen() && input._acIndex >= 0) {
                    e.preventDefault();
                    selectCurrent();
                } else {
                    list.classList.add('d-none');
                    input._acIndex = -1;
                }
            }
        }, true); // <<< TRUE = capture phase, fires BEFORE form submit handler

        // ── INPUT - fetch suggestions ─────────────────────────
        input.addEventListener('input', function () {
            clearTimeout(timer);
            input._acIndex = -1;
            var q = this.value.trim();
            if (q.length < 1) {
                list.innerHTML = '';
                list.classList.add('d-none');
                return;
            }
            timer = setTimeout(function () {
                fetch(url + '?q=' + encodeURIComponent(q))
                    .then(function (r) { return r.json(); })
                    .then(function (data) {
                        list.innerHTML = '';
                        input._acIndex = -1;
                        if (!data.length) {
                            list.classList.add('d-none');
                            return;
                        }
                        data.forEach(function (name) {
                            var li = document.createElement('li');
                            li.textContent = name;
                            li.addEventListener('mousedown', function (e) {
                                e.preventDefault();
                                input.value    = name;
                                input._acIndex = -1;
                                list.innerHTML = '';
                                list.classList.add('d-none');
                                var row       = input.closest('tr');
                                var typeField = row ? row.querySelector('input[name*=".materialType"]') : null;
                                if (typeField) setTimeout(function () { typeField.focus(); }, 0);
                            });
                            list.appendChild(li);
                        });
                        list.classList.remove('d-none');
                    })
                    .catch(function () {
                        list.classList.add('d-none');
                    });
            }, 200);
        });

        input.addEventListener('blur', function () {
            setTimeout(function () {
                list.classList.add('d-none');
                input._acIndex = -1;
            }, 220);
        });

        input.addEventListener('focus', function () {
            if (getItems().length) list.classList.remove('d-none');
        });
    }

    // Attach to all existing inputs on page load
    document.querySelectorAll('.ac-input').forEach(attachAutocomplete);

    // =========================================================
    // ROW HELPERS
    // =========================================================
    var tbody = document.getElementById('itemsBody');
    var form  = document.getElementById('billForm');

    function val(row, selector) {
        var el = row.querySelector(selector);
        return el ? el.value.trim() : '';
    }

    /** A row "has data" when a buyer OR a qty has been typed (date/type/rate are auto-filled). */
    function rowHasData(row) {
        return val(row, 'input[name*=".buyerName"]') !== '' || val(row, '.qty-input') !== '';
    }

    function focusBuyer(row) {
        var b = row ? row.querySelector('input[name*=".buyerName"]') : null;
        if (b) b.focus();
    }

    function renumber() {
        Array.prototype.forEach.call(tbody.children, function (row, idx) {
            row.querySelectorAll('[name]').forEach(function (el) {
                el.name = el.name.replace(/items\[\d+\]/, 'items[' + idx + ']');
            });
        });
    }

    function getLastFilledRate() {
        var lastRate = '';
        document.querySelectorAll('#itemsBody tr').forEach(function (row) {
            var r = row.querySelector('.rate-input');
            if (r && r.value.trim() !== '') lastRate = r.value.trim();
        });
        return lastRate;
    }

    // =========================================================
    // AMOUNT CALCULATION
    // =========================================================
    function calcRow(row) {
        var qtyEl  = row.querySelector('.qty-input');
        var rateEl = row.querySelector('.rate-input');
        var amtEl  = row.querySelector('.amount-cell');
        var qty    = parseFloat(qtyEl  ? qtyEl.value  : 0) || 0;
        var rate   = parseFloat(rateEl ? rateEl.value : 0) || 0;
        if (amtEl) amtEl.value = (qty * rate).toFixed(2);
        updateTotal();
    }

    function updateTotal() {
        var total = 0;
        document.querySelectorAll('#itemsBody .amount-cell').forEach(function (el) {
            total += parseFloat(el.value) || 0;
        });
        var gt = document.getElementById('grandTotal');
        if (gt) gt.value = total.toFixed(2);
    }

    function attachCalc(row) {
        var qty  = row.querySelector('.qty-input');
        var rate = row.querySelector('.rate-input');
        if (qty)  qty.addEventListener('input', function () { calcRow(row); });
        if (rate) rate.addEventListener('input', function () { calcRow(row); });
        calcRow(row);
    }

    document.querySelectorAll('#itemsBody tr').forEach(attachCalc);

    // =========================================================
    // ADD ROW  (button, and automatically when Qty is typed in the last row)
    // =========================================================
    function addRow(focus) {
        var i        = tbody.children.length;
        var autoDate = getLastFilledDate();
        var autoType = getLastFilledType();
        var autoRate = getLastFilledRate();

        if (!autoDate) {
            var now  = new Date();
            var dd   = String(now.getDate()).padStart(2, '0');
            var mm   = String(now.getMonth() + 1).padStart(2, '0');
            var yyyy = now.getFullYear();
            autoDate = dd + '-' + mm + '-' + yyyy;
        }

        var tr = document.createElement('tr');
        tr.innerHTML =
            '<td>' +
            '  <input type="text" name="items[' + i + '].itemDate"' +
            '         class="form-control form-control-sm"' +
            '         placeholder="DD-MM-YYYY" autocomplete="off"/>' +
            '</td>' +
            '<td class="position-relative">' +
            '  <input type="text" name="items[' + i + '].buyerName"' +
            '         class="form-control form-control-sm ac-input"' +
            '         data-url="/api/buyers/suggest"' +
            '         placeholder="Buyer name..." autocomplete="off"/>' +
            '  <ul class="ac-list d-none list-unstyled mb-0"></ul>' +
            '</td>' +
            '<td>' +
            '  <input type="text" name="items[' + i + '].materialType"' +
            '         class="form-control form-control-sm"' +
            '         placeholder="Wheat, Rice..."/>' +
            '</td>' +
            '<td>' +
            '  <input type="number" name="items[' + i + '].qty"' +
            '         class="form-control form-control-sm qty-input"' +
            '         min="1" placeholder="0"/>' +
            '</td>' +
            '<td>' +
            '  <input type="number" step="0.01" name="items[' + i + '].rate"' +
            '         class="form-control form-control-sm rate-input"' +
            '         min="0" placeholder="0.00"/>' +
            '</td>' +
            '<td>' +
            '  <input type="text"' +
            '         class="form-control form-control-sm amount-cell fw-semibold"' +
            '         readonly tabindex="-1" placeholder="0.00"/>' +
            '</td>' +
            '<td class="text-center">' +
            '  <button type="button" tabindex="-1"' +
            '          class="btn btn-sm btn-outline-danger remove-row"' +
            '          title="Remove row">X</button>' +
            '</td>';

        // values are set through the DOM (not the HTML string) so quotes can never break the row
        tr.querySelector('input[name$=".itemDate"]').value     = autoDate;
        tr.querySelector('input[name$=".materialType"]').value = autoType;
        tr.querySelector('.rate-input').value                  = autoRate;

        tbody.appendChild(tr);
        attachAutocomplete(tr.querySelector('.ac-input'));
        attachCalc(tr);

        if (focus) focusBuyer(tr);
        return tr;
    }

    document.getElementById('addRow').addEventListener('click', function () {
        addRow(true);
    });

    // typing a Qty in the LAST row adds the next row below (blank rows are skipped on save)
    tbody.addEventListener('input', function (e) {
        if (!e.target.classList.contains('qty-input')) return;
        var row = e.target.closest('tr');
        if (row === tbody.lastElementChild && e.target.value.trim() !== '') {
            addRow(false);
        }
    });

    // Date / Type / Rate follow the row above until a row gets a buyer or qty.
    // (e.g. Qty is typed first, the new row appears, then Rate is typed in the row above)
    tbody.addEventListener('input', function (e) {
        var t = e.target;
        var m = t.name ? /\.(itemDate|materialType|rate)$/.exec(t.name) : null;
        if (!m) return;
        for (var next = t.closest('tr').nextElementSibling;
             next && !rowHasData(next);
             next = next.nextElementSibling) {
            var el = next.querySelector('[name$=".' + m[1] + '"]');
            if (el) {
                el.value = t.value;
                if (m[1] === 'rate') calcRow(next);
            }
        }
    });

    // Tab from Rate -> Buyer of the next row (Date, Type and Rate are already filled)
    tbody.addEventListener('keydown', function (e) {
        if (e.key !== 'Tab' || e.shiftKey) return;
        if (!e.target.classList.contains('rate-input')) return;
        var next = e.target.closest('tr').nextElementSibling;
        var buyer = next ? next.querySelector('input[name*=".buyerName"]') : null;
        if (buyer) {
            e.preventDefault();
            buyer.focus();
        }
    });

    // =========================================================
    // DELETE ROW  (asks first when the row has a buyer or qty)
    // =========================================================
    var modalEl     = document.getElementById('deleteRowModal');
    var deleteModal = (modalEl && window.bootstrap) ? new window.bootstrap.Modal(modalEl) : null;
    var pendingRow  = null;
    var focusAfter  = null;

    function describeRow(row) {
        var n = Array.prototype.indexOf.call(tbody.children, row) + 1;
        var parts = [];
        var buyer = val(row, 'input[name*=".buyerName"]');
        var type  = val(row, 'input[name*=".materialType"]');
        var qty   = val(row, '.qty-input');
        var amt   = parseFloat(val(row, '.amount-cell'));
        if (buyer) parts.push(buyer);
        if (type)  parts.push(type);
        if (qty)   parts.push(qty + ' bags');
        if (amt > 0) parts.push('\u20B9 ' + amt.toFixed(2));
        return 'Row ' + n + ':  ' + parts.join('  \u2022  ');
    }

    /** Removes the row; never leaves the table empty. Returns the row to focus next. */
    function removeRow(row) {
        var idx = Array.prototype.indexOf.call(tbody.children, row);
        row.remove();
        if (tbody.children.length === 0) addRow(false);
        renumber();
        updateTotal();
        return tbody.children[Math.min(idx, tbody.children.length - 1)];
    }

    tbody.addEventListener('click', function (e) {
        var btn = e.target.closest('.remove-row');
        if (!btn) return;
        var row = btn.closest('tr');

        if (!rowHasData(row)) {                     // nothing typed -> delete straight away
            focusBuyer(removeRow(row));
            return;
        }
        if (!deleteModal) {                         // pop-up library missing -> plain confirm
            if (window.confirm('Delete this row?\n' + describeRow(row))) focusBuyer(removeRow(row));
            return;
        }
        pendingRow = row;
        document.getElementById('deleteRowSummary').textContent = describeRow(row);
        deleteModal.show();
    });

    if (modalEl) {
        // Cancel is focused by default, so a stray Enter never deletes anything
        modalEl.addEventListener('shown.bs.modal', function () {
            document.getElementById('deleteRowCancel').focus();
        });
        modalEl.addEventListener('hidden.bs.modal', function () {
            pendingRow = null;
            if (focusAfter) { focusBuyer(focusAfter); focusAfter = null; }
        });
        document.getElementById('deleteRowConfirm').addEventListener('click', function () {
            var row = pendingRow;
            pendingRow = null;
            if (row) focusAfter = removeRow(row);
            deleteModal.hide();
        });
    }

    // =========================================================
    // CHECK THE FORM BEFORE SAVING
    // =========================================================
    function validDate(str) {
        var m = /^(\d{2})-(\d{2})-(\d{4})$/.exec(str);
        if (!m) return false;
        var d = parseInt(m[1], 10), mo = parseInt(m[2], 10), y = parseInt(m[3], 10);
        var dt = new Date(y, mo - 1, d);
        return dt.getFullYear() === y && dt.getMonth() === mo - 1 && dt.getDate() === d;
    }

    function clearInvalid() {
        form.querySelectorAll('.is-invalid').forEach(function (el) { el.classList.remove('is-invalid'); });
        var box = document.getElementById('clientErrors');
        if (box) { box.classList.add('d-none'); box.innerHTML = ''; }
    }

    // typing in a field clears its red outline
    form.addEventListener('input', function (e) { e.target.classList.remove('is-invalid'); });

    form.addEventListener('submit', function (e) {
        clearInvalid();
        var problems = [];      // { text, field }

        function add(text, field) { problems.push({ text: text, field: field }); }

        var billDate = form.querySelector('input[name="billDate"]');
        var seller   = form.querySelector('input[name="sellerName"]');
        if (billDate && !validDate(billDate.value.trim())) add('Bill date must be entered as DD-MM-YYYY', billDate);
        if (seller && seller.value.trim() === '')          add('Please enter the Seller name', seller);

        var used = 0;
        Array.prototype.forEach.call(tbody.children, function (row, idx) {
            if (!rowHasData(row)) return;               // empty rows are skipped on save
            used++;
            var n = idx + 1;
            var date  = row.querySelector('input[name*=".itemDate"]');
            var buyer = row.querySelector('input[name*=".buyerName"]');
            var type  = row.querySelector('input[name*=".materialType"]');
            var qty   = row.querySelector('.qty-input');
            var rate  = row.querySelector('.rate-input');

            var missing = [], fields = [];
            if (date.value.trim() === '')   { missing.push('Date');  fields.push(date); }
            if (buyer.value.trim() === '')  { missing.push('Buyer'); fields.push(buyer); }
            if (type.value.trim() === '')   { missing.push('Type');  fields.push(type); }
            if (qty.value.trim() === '')    { missing.push('Qty');   fields.push(qty); }
            if (rate.value.trim() === '')   { missing.push('Rate');  fields.push(rate); }
            if (missing.length) {
                add('Row ' + n + ': please enter ' + missing.join(', '), fields[0]);
                fields.forEach(function (f) { f.classList.add('is-invalid'); });
            }
            if (date.value.trim() !== '' && !validDate(date.value.trim())) {
                add('Row ' + n + ': Date must be DD-MM-YYYY', date);
            }
            if (qty.value.trim() !== '' && !(parseInt(qty.value, 10) >= 1)) {
                add('Row ' + n + ': Qty must be at least 1', qty);
            }
            if (rate.value.trim() !== '' && parseFloat(rate.value) < 0) {
                add('Row ' + n + ': Rate cannot be negative', rate);
            }
        });

        if (used === 0) add('Add at least one row (buyer and qty)', null);

        if (problems.length === 0) return;              // all good -> let the form submit

        e.preventDefault();
        problems.forEach(function (p) { if (p.field) p.field.classList.add('is-invalid'); });

        var box = document.getElementById('clientErrors');
        if (box) {
            box.innerHTML = '';
            var title = document.createElement('strong');
            title.textContent = 'Please fix the following:';
            box.appendChild(title);
            problems.forEach(function (p) {
                var line = document.createElement('div');
                line.textContent = p.text;
                box.appendChild(line);
            });
            box.classList.remove('d-none');
            box.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        var first = problems.filter(function (p) { return p.field; })[0];
        if (first) first.field.focus();
    });

    updateTotal();
});
