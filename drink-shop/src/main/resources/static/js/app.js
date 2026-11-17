/* =====================================================================
   DRINK SHOP — tương tác giao diện (vanilla JS, không phụ thuộc thư viện)
   ===================================================================== */
(function () {
    "use strict";

    /* ---------- menu mobile ---------- */
    var topbar = document.querySelector(".topbar");
    var navToggle = document.querySelector(".nav-toggle");
    if (topbar && navToggle) {
        navToggle.addEventListener("click", function () {
            topbar.classList.toggle("open");
        });
        document.querySelectorAll(".nav .nav-link").forEach(function (link) {
            link.addEventListener("click", function () {
                topbar.classList.remove("open");
            });
        });
    }

    /* ---------- bóng đổ khi cuộn ---------- */
    function onScroll() {
        if (!topbar) return;
        topbar.classList.toggle("scrolled", window.scrollY > 8);
    }
    window.addEventListener("scroll", onScroll, { passive: true });
    onScroll();

    /* ---------- nút tăng / giảm số lượng ---------- */
    document.addEventListener("click", function (event) {
        var button = event.target.closest("[data-step]");
        if (!button) return;
        var box = button.closest(".qty, .qty-mini");
        if (!box) return;
        var input = box.querySelector("input[type=number]");
        if (!input) return;
        event.preventDefault();
        var step = parseInt(button.getAttribute("data-step"), 10) || 1;
        var min = parseInt(input.getAttribute("min") || "1", 10);
        var max = parseInt(input.getAttribute("max") || "50", 10);
        var value = (parseInt(input.value, 10) || min) + step;
        value = Math.max(min, Math.min(max, value));
        input.value = value;
        input.dispatchEvent(new Event("change", { bubbles: true }));
    });

    /* ---------- form số lượng tự gửi khi đổi ---------- */
    document.querySelectorAll("form[data-auto-submit] input[type=number]").forEach(function (input) {
        input.addEventListener("change", function () {
            if (input.form) input.form.submit();
        });
    });

    /* ---------- xác nhận trước khi xoá ---------- */
    document.querySelectorAll("form[data-confirm]").forEach(function (form) {
        form.addEventListener("submit", function (event) {
            if (!window.confirm(form.getAttribute("data-confirm"))) {
                event.preventDefault();
            }
        });
    });

    /* ---------- hiện khối phụ thuộc hình thức nhận hàng ---------- */
    var fulfillmentInputs = document.querySelectorAll('input[name="fulfillment"]');
    if (fulfillmentInputs.length) {
        var syncFulfillment = function () {
            var checked = document.querySelector('input[name="fulfillment"]:checked');
            var value = checked ? checked.value : "";
            document.querySelectorAll("[data-when]").forEach(function (box) {
                box.style.display = box.getAttribute("data-when") === value ? "" : "none";
            });
        };
        fulfillmentInputs.forEach(function (input) {
            input.addEventListener("change", syncFulfillment);
        });
        syncFulfillment();
    }

    /* ---------- ảnh lỗi thì dùng ảnh mặc định ---------- */
    document.querySelectorAll("img[data-fallback]").forEach(function (img) {
        img.addEventListener("error", function () {
            var fallback = img.getAttribute("data-fallback");
            if (fallback && img.src.indexOf(fallback) === -1) {
                img.src = fallback;
            }
        });
    });
})();
