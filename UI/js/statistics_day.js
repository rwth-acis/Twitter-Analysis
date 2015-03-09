// Get data for the visualization
$(function(){
    $.ajax({
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/hourly_stats_avg/',
        type: 'GET',
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
            if (data.rows.length > 0) {
                console.log("json loaded successfully.", data);
                var sentiment = [],
                    tweets = [],
                    nutrition = [];
                // Reformat the data into the required form for data visualization
                var indexArray = data.rows.map(function(o){return o._id;});
                //Add empty data
                for (var i = 0; i < 24; i++) {
                    var currentidno = indexArray.indexOf(i);
                    var obj_sen = {},
                        obj_tw = {},
                        obj_retw = {},
                        obj_fav = {},
                        obj_nutr = {};
                    obj_sen.key = "Sentiment";
                    obj_sen._id = i;
                    obj_tw.key = "Tweets Count";
                    obj_tw._id = i;
                    obj_retw.key = "Retweets Count";
                    obj_retw._id = i;
                    obj_fav.key = "Favorites Count";
                    obj_fav._id = i;
                    obj_nutr.key = "Nutrition";
                    obj_nutr._id = i;
                    if (currentidno != -1) {
                        obj_sen.value = data.rows[currentidno].sentiment_avg;
                        obj_tw.value = data.rows[currentidno].tweets_count_avg;
                        obj_retw.value = data.rows[currentidno].retweets_count_avg;
                        obj_fav.value = data.rows[currentidno].favorites_count_avg;
                        obj_nutr.value = data.rows[currentidno].nutrition_avg;
                    }
                    else {                        
                        obj_sen.value = 0;                        
                        obj_tw.value = 0;                        
                        obj_retw.value = 0;                        
                        obj_fav.value = 0;
                        obj_nutr.value = 0;                        
                        obj_en.value = 0;                        
                    }
                    sentiment.push(obj_sen);
                    tweets.push(obj_tw);
                    tweets.push(obj_retw);
                    tweets.push(obj_fav);
                    nutrition.push(obj_nutr);
                }
                //Draw visualization
                drawData(nutrition, "NutritionDiv", "nutritionSvg");
                drawData(tweets, "TweetsDiv", "tweetsSvg");
                drawData(sentiment, "SentimentDiv", "sentimentSvg");
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

// Draw graphs
function drawData(data, div, svgId) {
    var margin = {top: 20, right: 20, bottom: 30, left: 50};
    divHeight = $("#" + div).outerHeight();
    // Add svg to div
    var svg = d3.select("#" + div).append("svg")
        .attr("width", "100%")
        .attr("height", divHeight - 50)
        .attr("id", svgId)
        .append("g");  
    var svgWidth = $("#" + svgId).outerWidth();
        svgHeight = $("#" + svgId).outerHeight();
    var width = svgWidth - margin.left - margin.right,
        height = svgHeight - margin.top - margin.bottom;   
    svg.attr("transform", "translate(" + width / 1.8 + "," + height / 1.8 + ")");   
    svg.append("g")
        .attr("class","legend")
        .attr("transform","translate(" + (svgWidth - 20) + ",0)")
        .style("font-size","14px")
        .call(d3.legend);
    // Format a date into the required form   
    var formatDate = d3.time.format("%H"),
        formatHour = function(d) { return formatDate(new Date(2014, 1, 1, d, 0, 0, 0)); };    
    var outerRadius = height / 2 - 10,
        innerRadius = 0;    
    var angle = d3.time.scale()
        .range([0, 2 * Math.PI]);    
    var radius = d3.scale.linear()
        .range([innerRadius, outerRadius]);    
    var z = d3.scale.category20c();    
    var stack = d3.layout.stack()
        .offset("zero")
        .values(function(d) { return d.values; })
        .x(function(d) { return d._id; })
        .y(function(d) { return d.value; });    
    var nest = d3.nest()
        .key(function(d) {return d.key; });    
    var line = d3.svg.line.radial()
        .interpolate("cardinal-closed")
        .angle(function(d) { return angle(d._id); })
        .radius(function(d) { return radius(d.y); });    
    var area = d3.svg.area.radial()
        .interpolate("cardinal-closed")
        .angle(function(d) { return angle(d._id); })
        .innerRadius(function(d) { return radius(0); })
        .outerRadius(function(d) { return radius(d.y); });
    var layers = stack(nest.entries(data));
    min = d3.min(data, function(d) { return d.y; });
    // Min and max radius
    if (min > 0) {
      radius_min = 0;
    }
    else {
      radius_min = min;
    }
    angle.domain([0, d3.max(data, function(d) { return d._id + 1; })]);
    radius.domain([radius_min, d3.max(data, function(d) { return d.y; })]);  
    console.log(layers);        
    svg.selectAll(".layer")
        .data(layers)
      .enter().append("path")
        .attr("class", "layer")
        .attr("d", function(d) { console.log(d.values, area(d.values)); return area(d.values); })
        .style("fill", function(d, i) { return z(i); })
        .attr("data-legend",function(d) { return d.key; })
        .style("stroke", function(d,i) { return z(i); })        
        .style("opacity", 0.8);
    // Draw axis 
    svg.selectAll(".axis")
        .data(d3.range(angle.domain()[1]))
      .enter().append("g")
        .attr("class", "axis")
        .attr("transform", function(d) { return "rotate(" + angle(d) * 180 / Math.PI + ")"; })
      .call(d3.svg.axis()
        .scale(radius.copy().range([-innerRadius, -outerRadius]))
        .orient("left"))
      .append("text")
        .attr("transform", function(d) { return d < 270 && d > 90 ? "rotate(180 " + (radius + 6) + ",0)" : null; })
        .attr("y", -outerRadius - 15)
        .attr("font-size", 10)
        .attr("dy", ".71em")
        .attr("text-anchor", "middle")
        .text(function(d) { return formatHour(d) + ":00"; });
    // Draw legend
    legend = d3.select("#" + svgId).append("g")
    .attr("class","legend")
    .attr("transform","translate(" + (svgWidth - 120) + ",50)")
    .style("font-size","14px")
    .call(d3.legend);
}

// Show tip
function showTip(elementId) {    
    var text;
    switch(elementId.id) {
    case "sentiment_tip":
        text = "<h3 style='text-align: center;'>Sentiment</h3><h4 style='margin: 5px;'> Description: </h4><p> Average sentiment value for each hour of a day</p>";
        break;
    case "tweets_tip":
        text = "<h3 style='text-align: center;'>Tweets, Retweets & Favorites </h3><h4 style='margin: 5px;'> Description: </h4><p> Average number of tweets, " +
        "retweets and favorites for each hour of a day</p>";
        break;
    case "nutrition_tip":
        text = "<h3 style='text-align: center;'>Nutrition </h3><h4 style='margin: 5px;'> Description: </h4><p> Average nutrition value for each hour of a day  </p>";
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