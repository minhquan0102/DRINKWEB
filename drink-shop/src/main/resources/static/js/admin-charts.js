/* =====================================================================
   BIỂU ĐỒ THỐNG KÊ (Chart.js) — dữ liệu do Thymeleaf inline xuống
   window.CHART_REVENUE / CHART_STATUS / CHART_PAYMENT / CHART_TOP
   ===================================================================== */
(function () {
    "use strict";

    if (typeof Chart === "undefined") {
        return;
    }

    Chart.defaults.font.family = '"Segoe UI", system-ui, -apple-system, sans-serif';
    Chart.defaults.font.size = 12;
    Chart.defaults.color = "#726458";
    Chart.defaults.borderColor = "#efe6da";

    var BRAND = "#b4471a";
    var PALETTE = ["#b4471a", "#2f55c4", "#157f4b", "#f0b23f", "#7c5cd6", "#b42318", "#6b7280"];

    function money(value) {
        return (value || 0).toLocaleString("vi-VN") + "đ";
    }

    function shortMoney(value) {
        if (value >= 1000000) {
            return (value / 1000000).toFixed(1).replace(".0", "") + "tr";
        }
        if (value >= 1000) {
            return Math.round(value / 1000) + "k";
        }
        return value;
    }

    function barOptions(horizontal) {
        return {
            responsive: true,
            maintainAspectRatio: false,
            indexAxis: horizontal ? "y" : "x",
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: function (ctx) {
                            return horizontal
                                ? " " + ctx.parsed.x + " ly"
                                : " " + ctx.parsed.y + " ly";
                        }
                    }
                }
            },
            scales: {
                x: { grid: { display: !horizontal }, beginAtZero: true },
                y: { grid: { display: horizontal }, beginAtZero: true }
            }
        };
    }

    function initRevenue() {
        var canvas = document.getElementById("chartRevenue");
        var data = window.CHART_REVENUE;
        if (!canvas || !Array.isArray(data) || data.length === 0) {
            return;
        }
        new Chart(canvas, {
            type: "line",
            data: {
                labels: data.map(function (point) { return point.date; }),
                datasets: [
                    {
                        label: "Online",
                        data: data.map(function (point) { return Number(point.online || 0); }),
                        borderColor: "#2f55c4",
                        backgroundColor: "rgba(47, 85, 196, .13)",
                        fill: true,
                        tension: .35,
                        borderWidth: 2,
                        pointRadius: 0,
                        pointHoverRadius: 4
                    },
                    {
                        label: "Tại quầy",
                        data: data.map(function (point) { return Number(point.atCounter || 0); }),
                        borderColor: BRAND,
                        backgroundColor: "rgba(180, 71, 26, .13)",
                        fill: true,
                        tension: .35,
                        borderWidth: 2,
                        pointRadius: 0,
                        pointHoverRadius: 4
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: { mode: "index", intersect: false },
                plugins: {
                    legend: {
                        position: "bottom",
                        labels: { usePointStyle: true, boxWidth: 8, boxHeight: 8, padding: 14 }
                    },
                    tooltip: {
                        callbacks: {
                            label: function (ctx) {
                                return " " + ctx.dataset.label + ": " + money(ctx.parsed.y);
                            }
                        }
                    }
                },
                scales: {
                    x: { grid: { display: false } },
                    y: {
                        beginAtZero: true,
                        grid: { color: "#f3ece3" },
                        ticks: { callback: function (value) { return shortMoney(value); } }
                    }
                }
            }
        });
    }

    function initDoughnut(canvasId, data, unitLabel) {
        var canvas = document.getElementById(canvasId);
        if (!canvas || !Array.isArray(data)) {
            return;
        }
        // Bỏ các nhóm có giá trị 0 để chú giải gọn hơn
        data = data.filter(function (point) { return Number(point.value || 0) > 0; });
        if (data.length === 0) {
            return;
        }
        new Chart(canvas, {
            type: "doughnut",
            data: {
                labels: data.map(function (point) { return point.label; }),
                datasets: [{
                    data: data.map(function (point) { return Number(point.value || 0); }),
                    backgroundColor: data.map(function (_, index) { return PALETTE[index % PALETTE.length]; }),
                    borderColor: "#ffffff",
                    borderWidth: 2,
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: "62%",
                plugins: {
                    legend: {
                        position: "bottom",
                        labels: { usePointStyle: true, boxWidth: 8, boxHeight: 8, padding: 12 }
                    },
                    tooltip: {
                        callbacks: {
                            label: function (ctx) {
                                var total = ctx.dataset.data.reduce(function (a, b) { return a + b; }, 0);
                                var percent = total ? Math.round((ctx.parsed / total) * 100) : 0;
                                return " " + ctx.label + ": " + ctx.parsed + " " + unitLabel + " (" + percent + "%)";
                            }
                        }
                    }
                }
            }
        });
    }

    function initTopProducts() {
        var canvas = document.getElementById("chartTop");
        var data = window.CHART_TOP;
        if (!canvas || !Array.isArray(data)) {
            return;
        }
        data = data.filter(function (point) { return Number(point.value || 0) > 0; });
        if (data.length === 0) {
            return;
        }
        new Chart(canvas, {
            type: "bar",
            data: {
                labels: data.map(function (point) {
                    var label = point.label || "";
                    return label.length > 22 ? label.substring(0, 21) + "…" : label;
                }),
                datasets: [{
                    data: data.map(function (point) { return Number(point.value || 0); }),
                    backgroundColor: "rgba(180, 71, 26, .82)",
                    borderRadius: 8,
                    barThickness: 18
                }]
            },
            options: barOptions(true)
        });
    }

    function start() {
        initRevenue();
        initDoughnut("chartStatus", window.CHART_STATUS, "đơn");
        initDoughnut("chartPayment", window.CHART_PAYMENT, "giao dịch");
        initTopProducts();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", start);
    } else {
        start();
    }
})();
