// Get data - daily statistics
$(function(){
    $.ajax({
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/daily_stats/',
        type: 'GET',
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
            if (data.rows.length > 0) {
                console.log("json loaded successfully.", data);
            drawEnergy(data.rows);
            drawSentiment(data.rows);
            drawTweets(data.rows);
            }
            else {
                console.log("Loaded 0 rows");
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('error', errorThrown);
        }
    });
});

// Draw energy graph
function drawEnergy(data) {
    var margin = {top: 20, right: 20, bottom: 30, left: 50};
    var parseDate = d3.time.format("%Y-%m-%dT%H:%M:%S.%L%Z").parse;
    bisectDate = d3.bisector(function(d) { return d.date.$date; }).left;
    divHeight = $("#EnergyDiv").outerHeight();
    var svg = d3.select("#EnergyDiv").append("svg")
        .attr("width", "100%")
        .attr("height", divHeight - 50)
        .attr("id", "energySvg")
      .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    var    svgWidth = $("#energySvg").outerWidth();
           svgHeight = $("#energySvg").outerHeight();
    var width = svgWidth - margin.left - margin.right,
        height = svgHeight - margin.top - margin.bottom;
    var x = d3.time.scale()
        .range([0, width]);
    var y = d3.scale.linear()
        .range([height, 0]);
    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom");
    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left");
    var line = d3.svg.line()
        .interpolate("basis")
        .x(function(d) { return x(d.date.$date); })
        .y(function(d) { return y(d.energy); });
    data.forEach(function(d) {
        d.date.$date = parseDate(d.date.$date);
        d.energy = +d.energy;
    });
    x.domain(d3.extent(data, function(d) { return d.date.$date; }));
    y.domain(d3.extent(data, function(d) { return d.energy; }));
    svg.append("g")
          .attr("class", "x axis")
          .attr("transform", "translate(0," + height + ")")
          .attr("font-size", "10px")
          .call(xAxis);
    svg.append("g")
        .attr("class", "y axis")
        .attr("font-size", "10px")
        .call(yAxis)
      .append("text")
        .attr("transform", "rotate(-90)")
        .attr("y", 6)
        .attr("dy", ".71em")
        .style("text-anchor", "end")
        .text("Energy");
    svg.append("path")
        .datum(data)
        .attr("class", "line")
        .attr("d", line)
        .transition()
        .duration(2000)
        .attrTween('d', pathTween);
    function pathTween() {
        var interpolate = d3.scale.quantile().domain([0,1]).range(d3.range(1, data.length + 1));
        return function(t) {
            return line(data.slice(0, interpolate(t)));
        }
    }
}

// Draw sentiment graph
function drawSentiment(data) {
    var margin = {top: 20, right: 20, bottom: 30, left: 50};
    var parseDate = d3.time.format("%Y-%m-%dT%H:%M:%S.%L%Z").parse;
    bisectDate = d3.bisector(function(d) { return d.date.$date; }).left;
    divHeight = $("#SentimentDiv").outerHeight();
    var svg = d3.select("#SentimentDiv").append("svg")
        .attr("width", "100%")
        .attr("height", divHeight - 50)
        .attr("id", "sentimentSvg")
      .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    var svgWidth = $("#sentimentSvg").outerWidth();
        svgHeight = $("#sentimentSvg").outerHeight();
    var width = svgWidth - margin.left - margin.right,
        height = svgHeight - margin.top - margin.bottom;
    var x = d3.time.scale()
        .range([0, width]);
    var y = d3.scale.linear()
        .range([height, 0]);
    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom");
    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left");
    var line = d3.svg.line()
    .interpolate("basis")
        .x(function(d) { return x(d.date.$date); })
        .y(function(d) { return y(d.sentiment); });
    x.domain(d3.extent(data, function(d) { return d.date.$date; }));
    y.domain(d3.extent(data, function(d) { return d.sentiment; }));
    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .attr("font-size", "10px")
        .call(xAxis);
    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis)
        .attr("font-size", "10px")
      .append("text")
        .attr("transform", "rotate(-90)")
        .attr("y", 6)
        .attr("dy", ".71em")
        .style("text-anchor", "end")
        .text("Sentiment");
    svg.append("path")
        .datum(data)
        .attr("class", "line")
        .transition()
        .duration(2000)
        .attrTween('d', pathTween);
    function pathTween() {
        var interpolate = d3.scale.quantile().domain([0,1]).range(d3.range(1, data.length + 1));
        return function(t) {
            return line(data.slice(0, interpolate(t)));
        }
    }
}    

// Draw number of tweets, retweets and favorites    
function drawTweets(data) {
  var margin = {top: 20, right: 20, bottom: 30, left: 80};
    var parseDate = d3.time.format("%Y-%m-%dT%H:%M:%S.%L%Z").parse;
    bisectDate = d3.bisector(function(d) { return d.date.$date; }).left;
    divHeight = $("#TweetsDiv").outerHeight();
    var svg = d3.select("#TweetsDiv").append("svg")
        .attr("width", "100%")
        .attr("height", divHeight - 50)
        .attr("id", "tweetsSvg")
      .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    var svgWidth = $("#tweetsSvg").outerWidth();
        svgHeight = $("#tweetsSvg").outerHeight();
    var width = svgWidth - margin.left - margin.right,
        height = svgHeight - margin.top - margin.bottom;
    var x = d3.time.scale()
        .range([0, width]);
    var y = d3.scale.linear()
        .range([height, 0]);
    var color = d3.scale.category10();
    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom");
    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left");
    var line = d3.svg.line()
        .interpolate("basis")
        .x(function(d) { return x(d.date); })
        .y(function(d) { return y(d.vals); });
    color.domain(["tweets_count", "retweets_count", "favorites_count"]);
    var parameters = color.domain().map(function(name) {
        return {
            name: name,
            values: data.map(function(d) {
                return {date: d.date.$date, vals: d[name]};
            })
        };
  });
    x.domain(d3.extent(data, function(d) { return d.date.$date; }));
    y.domain([ 0, d3.max(parameters, function(c) { return d3.max(c.values, function(v) { return v.vals; }); }) ]);
    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);
    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis)
      .append("text")
        .attr("transform", "rotate(-90)")
        .attr("y", 6)
        .attr("dy", ".71em")
        .style("text-anchor", "end")
        .text("Occurrance");
    var parameter = svg.selectAll(".parameter")
        .data(parameters)
        .enter().append("g")
        .attr("class", "city");
    var path = parameter.append("path")
        .attr("class", "line")
        .attr("d", function(d) { return line(d.values); })
        .attr("data-legend",function(d) { if (d.name == "retweets_count") return "Number of Retweets"; if (d.name == "tweets_count") return "Number of Tweets"; if (d.name == "favorites_count") return "Number of Favorites"; return null; })
        .style("stroke", function(d) { return color(d.name); })
        .transition()
        .duration(3000)
        .ease("linear")
        .attrTween('d', pathTween);
    function pathTween(d) {
        var interpolate = d3.scale.quantile().domain([0,1]).range(d3.range(1, data.length + 1));
        return function(t) {
            return line(d.values.slice(0, interpolate(t)));
        }
    }
    parameter.append("text")
        .datum(function(d) { return {name: d.name, value: d.values[d.values.length - 1]}; })
        .attr("transform", function(d) { return "translate(" + x(d.value.date) + "," + y(d.value.vals) + ")"; })
        .attr("x", 3)
        .attr("dy", ".35em");
    legend = svg.append("g")
        .attr("class","legend")
        .attr("transform","translate(" + (svgWidth - 230) + ",0)")
        .style("font-size","14px")
        .call(d3.legend);
}

// Tweets' languages
$(function(){
    $.ajax({
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/tweet_languages/',
        type: 'GET',
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
            if (data.rows.length > 0) {
                console.log("json loaded successfully.", data);
                drawLanguages(data.rows);
            }
            else {
                console.log("Loaded 0 rows");
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('error', errorThrown);
        }
    });
});

//Draw pie for languages
function drawLanguages(data) {    
    var margin = {top: 20, right: 20, bottom: 30, left: 50};
    divHeight = $("#LanguagesDiv").outerHeight();
    var svg = d3.select("#LanguagesDiv").append("svg")
        .attr("width", "100%")
        .attr("height", divHeight - 50)
        .attr("id", "languagesSvg")
        .append("g");
    var svgWidth = $("#languagesSvg").outerWidth();
        svgHeight = $("#languagesSvg").outerHeight();
    var width = svgWidth - margin.left - margin.right,
        height = svgHeight - margin.top - margin.bottom;
    svg.attr("transform", "translate(" + width / 1.6 + "," + height / 1.45 + ")");
    var radius = Math.min(width, height) / 1.7;
    var color = d3.scale.category20()
    var arc = d3.svg.arc()
        .outerRadius(radius - 10)
        .innerRadius(radius - 50);
    var pie = d3.layout.pie()
        .sort(null)
        .value(function(d) { return d.count; });
    data.forEach(function(d) { d.population = +d.population; });
    var g = svg.selectAll(".arc")
        .data(pie(data))
      .enter().append("g")
        .attr("class", "arc")
        .on("mousemove",function(d){
            var mouseVal = d3.mouse(this);
            $("#messageToolTipDiv").css("display","none");
            lang = getLanguageName(d.data._id);
            if (lang == undefined)
                lang = d.data._id;
            $("#messageToolTipDiv").html('<p>'+ lang + '</p><p>'+d.data.count + ' tweets</p>')
            .css("left", (d3.event.pageX+12) + "px")
            .css("top", (d3.event.pageY-10) + "px")
            .css("opacity", 1)
            .css("display","block");
        })
        .on("mouseout",function(){$("#messageToolTipDiv").html(" ").css("display","none");});
    g.append("path")
        .style("fill", function(d) { return color(d.data._id); })
        .transition().duration(500)
        .attrTween('d', function(d) {
            console.log("test", d);
            var i = d3.interpolate(d.startAngle+0.1, d.endAngle);
            return function(t) {
                d.endAngle = i(t);
                return arc(d);
            }
        });
    labelr = radius + 2;
    var total= d3.sum(data, function(d){ return d.count; });
    g.append("text")
        .attr("transform", function(d) {
            var c = arc.centroid(d),
            x = c[0],
            y = c[1],
            // pythagorean theorem for hypotenuse
            h = Math.sqrt(x*x + y*y);
            return "translate(" + (x/h * labelr) +  ',' + (y/h * labelr) +  ")"; 
        })
        .attr("dy", ".35em")
        .attr("text-anchor", function(d) {
            return (d.endAngle + d.startAngle)/2 > Math.PI ? "end" : "start";
        })
        .attr("font-size", 10)
        .text(function(d, i) { return d.data._id; });
    g.append("text")
        .attr("transform", function(d) { return "translate(" + arc.centroid(d) + ")"; })
        .attr("dy", ".35em")
        .style("text-anchor", "middle")
        .attr("font-size", 8)
        .text(function(d, i) { return (d3.round(100* d.value / total, 1) + "% "); }); }

// Get tweet sources data
$(function(){
    $.ajax({
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/tweet_sources/',
        type: 'GET',
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
            if (data.rows.length > 0) {
                console.log("json loaded successfully.", data);
                drawSources(data.rows);
            }
            else {
                console.log("Loaded 0 rows");
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('error', errorThrown);
        }
    });
});

// Draw pie chart for tweets' sources
function drawSources(data) {
    var margin = {top: 20, right: 20, bottom: 30, left: 50};
    divHeight = $("#SourcesDiv").outerHeight();
    var svg = d3.select("#SourcesDiv").append("svg")
        .attr("width", "100%")
        .attr("height", divHeight - 50)
        .attr("id", "sourcesSvg")
      .append("g");
    var svgWidth = $("#sourcesSvg").outerWidth();
        svgHeight = $("#sourcesSvg").outerHeight();
    var width = svgWidth - margin.left - margin.right,
        height = svgHeight - margin.top - margin.bottom;
    svg.attr("transform", "translate(" + width / 1.6 + "," + height / 1.45 + ")");
    var radius = Math.min(width, height) / 1.7;
    var color = d3.scale.category20c();
    var arc = d3.svg.arc()
        .outerRadius(radius - 10)
        .innerRadius(radius - 50);
    var pie = d3.layout.pie()
        .sort(null)
        .value(function(d) { return d.count; });
    data.forEach(function(d) {
        res = null;
        if (d._id == "web") {
            res = "Twitter Website"
        }
        else {
            if (d._id == "Other") {
                res = "Other"
                url = "";
            }
            else {
            var match = />(.*?)</i.exec(d._id);
            res = match[1];
            match = /\"(.*?)" rel=/i.exec(d._id);
            url = match[1];
            }
        }
        d._id = res;
        d.url = url;
    });
    var g = svg.selectAll(".arc")
        .data(pie(data))
      .enter().append("g")
        .attr("class", "arc")
        .on("mousemove",function(d){
        	var mouseVal = d3.mouse(this);
        	$("#messageToolTipDiv").css("display","none");
        	$("#messageToolTipDiv").html("<p>"+d.data._id+"</p><p>"+d.data.count + ' tweets</p>')
            .css("left", (d3.event.pageX+12) + "px")
            .css("top", (d3.event.pageY-10) + "px")
            .css("opacity", 1)
            .css("display","block");
        })
        .on("mouseout",function(){$("#messageToolTipDiv").html(" ").css("display","none");})
        .on("click",function(d){if (d.data.url.length > 0) {window.open(d.data.url);}});
    g.append("path")
        .style("fill", function(d) { return color(d.data._id); })
        .transition().duration(500)
        .attrTween('d', function(d) {
                console.log("test", d);
                var i = d3.interpolate(d.startAngle+0.1, d.endAngle);
                return function(t) {
                    d.endAngle = i(t);
                    return arc(d);
                }
        });
    labelr = radius + 2;
    var total= d3.sum(data, function(d){return d.count;});
    g.append("text")
        .attr("transform", function(d) {
            var c = arc.centroid(d),
            x = c[0],
            y = c[1],
            // pythagorean theorem for hypotenuse
            h = Math.sqrt(x*x + y*y);
            return "translate(" + (x/h * labelr) +  ',' + (y/h * labelr) +  ")"; 
        })
        .attr("dy", ".35em")
        .attr("text-anchor", function(d) {
            return (d.endAngle + d.startAngle)/2 > Math.PI ? "end" : "start";
        })
        .attr("font-size", 10)
        .text(function(d, i) {return d.data._id; });
    g.append("text")
        .attr("transform", function(d) { return "translate(" + arc.centroid(d) + ")"; })
        .attr("dy", ".35em")
        .style("text-anchor", "middle")
        .attr("font-size", 8)
        .text(function(d, i) {
            return (d3.round(100* d.value / total, 1) + "% "); }); 
}

// Get tweets' content statistics
$(function(){
    $.ajax({
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/tweet_content/',
        type: 'GET',
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
            if (data.rows.length > 0) {
                console.log("json loaded successfully.", data);
                drawContent(data.rows);
            }
            else {
                console.log("Loaded 0 rows");
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('error', errorThrown);
        }
    });
});

// Draw tweets' content
function drawContent(data) { 
    var margin = {top: 20, right: 20, bottom: 30, left: 50};
    divHeight = $("#ContentDiv").outerHeight();
    var svg = d3.select("#ContentDiv").append("svg")
        .attr("width", "100%")
        .attr("height", divHeight - 50)
        .attr("id", "contentSvg")
      .append("g");
    var svgWidth = $("#contentSvg").outerWidth();
        svgHeight = $("#contentSvg").outerHeight();
    var width = svgWidth - margin.left - margin.right,
        height = svgHeight - margin.top - margin.bottom;
    svg.attr("transform", "translate(" + width / 1.6 + "," + height / 1.45 + ")");
    var radius = Math.min(width, height) / 1.7;
    var color = d3.scale.category20();
    var arc = d3.svg.arc()
        .outerRadius(radius - 10)
        .innerRadius(radius - 50);
    var pie = d3.layout.pie()
        .sort(null)
        .value(function(d) { return d.count; });        
    data.forEach(function(d) {
        d.population = +d.population;
    });
    var g = svg.selectAll(".arc")
        .data(pie(data))
      .enter().append("g")
        .attr("class", "arc")
        .on("mousemove",function(d){
        	var mouseVal = d3.mouse(this);
        	$("#messageToolTipDiv").css("display","none");
        	$("#messageToolTipDiv").html("<p>"+d.data._id+"</p><p>"+d.data.count + ' tweets</p>')
            .css("left", (d3.event.pageX+12) + "px")
            .css("top", (d3.event.pageY-10) + "px")
            .css("opacity", 1)
            .css("display","block");
            $("#messageToolTipDiv").addClass("tooltip");
        })
        .on("mouseout",function(){$("#messageToolTipDiv").html(" ").css("display","none");});
    g.append("path")
        //.attr("d", arc)
        .style("fill", function(d) { return color(d.data._id); })
        .transition().duration(500)
        .attrTween('d', function(d) {
            console.log("test", d);
            var i = d3.interpolate(d.startAngle+0.1, d.endAngle);
            return function(t) {
                d.endAngle = i(t);
                return arc(d);
            }
        });
    labelr = radius + 2;
    var total= d3.sum(data, function(d){return d.count;});
    g.append("text")
        .attr("transform", function(d) {
            var c = arc.centroid(d),
            x = c[0],
            y = c[1],
            // pythagorean theorem for hypotenuse
            h = Math.sqrt(x*x + y*y);
            return "translate(" + (x/h * labelr) +  ',' + (y/h * labelr) +  ")"; 
        })
        .attr("dy", ".35em")
        .attr("text-anchor", function(d) {
            return (d.endAngle + d.startAngle)/2 > Math.PI ? "end" : "start";
        })
        .attr("font-size", 10)
        .text(function(d, i) { return d.data._id; });
    g.append("text")
        .attr("transform", function(d) { return "translate(" + arc.centroid(d) + ")"; })
        .attr("dy", ".35em")
        .style("text-anchor", "middle")
        .attr("font-size", 8)
        .text(function(d, i) {
            return (d3.round(100* d.value / total, 1) + "% "); }); 
}

// Show tip
function showTip(elementId) {    
    var text;
    switch(elementId.id) {
    case "languages_tip":
        text = "<h3 style='text-align: center;'>Tweets' languages </h3><h4 style='margin: 5px;'> Description: </h4><p> Tweet language is a language of a tweet</p>" +
        "<h4>Tip:</h4> <p>Move the mouse to the part of a pie to see the full language name</p>";
        break;
    case "sources_tip":
        text = "<h3 style='text-align: center;'>Tweets' sources </h3><h4 style='margin: 5px;'> Description: </h4><p> Tweet source is a client that was used to write a tweet </p>" +
        "<h4>Example:</h4><p>Twitter Web Client is a twitter.com website</p><h4> Tip:</h4> <p>Click on bar to open a client in a new tab of your browser</p>";
        break;
    case "tweets_tip":
        text = "<h3 style='text-align: center;'>Tweets' content </h3><h4 style='margin: 5px;'> Description: </h4><p> Tweet content distribution shows what was shared by tweets: only" +
        "text, text and video, text and picture, text and link </p>";
        break;
    case "retweets_tip":
        text = "<h3 style='text-align: center;'>Time line </h3><h4 style='margin: 5px;'> Description: </h4><p> The graph shows the quantity of written tweets and the quantity of " +
        "retweet and favorite activities in time</p>";
        break;
    case "energy_tip":
        text = "<h3 style='text-align: center;'>Energy </h3><h4 style='margin: 5px;'> Description: </h4><p> Energy distribution in time. " +
        "Shows the level of people interest to the event. </p>";
        break;
    case "sentiment_tip":
        text = "<h3 style='text-align: center;'>Sentiment </h3><h4 style='margin: 5px;'> Description: </h4><p> Sentiment distribution in time </p>";
        break;
    case "other":
        text = "Other";
    default:
        text="";
    }
    $("#messageToolTipDiv").html(text);
    $("#messageToolTipDiv").css("left", mouseX - 250 + "px")
      .css("top", mouseY - 10 + "px")
      .css("opacity", 1)
      .css("display","block")
      .css("text-align", "justify");
}

// Close tip
function noTip() {
    $("#messageToolTipDiv").css("display","none");
    $("#messageToolTipDiv").css("text-align", "center");
}

// track mouse position
var mouseX, mouseY;
$(this).mousemove(function (e) {
    mouseX = e.pageX;
    mouseY = e.pageY;
});