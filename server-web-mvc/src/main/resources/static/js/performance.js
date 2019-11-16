function Diagram(id, seriesNames, yAxisMax) {
    var that = this;

    this.seriesNames = seriesNames;
    this.seriesLength = seriesNames.length;
    this.seriesCopy = new Array(this.seriesLength);
    this.yAxisMax = yAxisMax;

    new Highcharts.chart(id, {
        chart: {
            type: 'line',
            events: {
                load: function () {
                    for (var i = 0; i < that.seriesLength; i++) {
                        that.seriesCopy[i] = this.series[i];
                    }
                }
            }
        },
        colors: ['#0066FF', '#00CCFF'],
        title: {
            text: false
        },
        xAxis: {
            type: 'datetime',
            minRange: 60 * 1000
        },
        yAxis: {
            title: {
                text: false
            },
            max: that.yAxisMax
        },
        legend: {
            enabled: false
        },
        plotOptions: {
            series: {
                threshold: 0,
                marker: {
                    enabled: false
                }
            }
        },
        series: (function () {
            var series = [];
            for (var i = 0; i < that.seriesLength; i++) {
                series.push({
                    name: that.seriesNames[i]
                });
            }
            return series;
        }())
    });

    this.addPoints = function (points) {
        var shift = this.seriesCopy[0].data.length > 60;
        for (var i = 0; i < points.length; i++) {
            this.seriesCopy[i].addPoint(points[i], true, shift, false);
        }
    };
}

var diagram1 = new Diagram('top-left', ['System CPU load', 'Process CPU load'], 100);
var diagram2 = new Diagram('top-right', ['Committed virtual memory size']);
var diagram3 = new Diagram('bottom-left', ['Total physical memory size', 'Free physical memory size']);
var diagram4 = new Diagram('bottom-right', ['Total swap space size', 'Free swap space size']);

var eventSource = new EventSource('http://localhost:8080/sse/mvc/performance');

eventSource.onmessage = function (message) {

    var performance = JSON.parse(message.data);
    var time = performance.time;

    diagram1.addPoints([
        [time, performance.systemCpuLoad * 100],
        [time, performance.processCpuLoad * 100]
    ]);
    diagram2.addPoints([
        [time, performance.committedVirtualMemorySize]
    ]);
    diagram3.addPoints([
        [time, performance.totalPhysicalMemorySize],
        [time, performance.freePhysicalMemorySize]
    ]);
    diagram4.addPoints([
        [time, performance.totalSwapSpaceSize],
        [time, performance.freePhysicalMemorySize]
    ]);

};
