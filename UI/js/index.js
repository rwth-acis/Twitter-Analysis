

// Get 50 newest tweets from MongoDB via sleepymongoose
$(function(){
    $.ajax({
        url: 'http://' + window.serverIP + ':27080/' + window.databaseName + '/tweets/_find?sort=%7B%22id%22%3A-1%7D&limit=50',
        type: 'POST',
        dataType: 'jsonp',
        success: function(response) {
            if (response.results != undefined) {
                var data = response.results;
                //console.log("Mongnose", data);
                // Create new table element with tweets
                table = '<table id="NewTweetsTable">'
                for (var i = 0; i < data.length; i++) {
                    len = data[i].created_at.length;
                    table = table + '<tr style="background: #C5DAF0"> <td style="text-align:left;" colspan="2" >@' + data[i].user
                        + '</td> <td  style="text-align:right;" colspan="2">' + data[i].created_at.substring(4, 16) + "</td> </tr>"
                        + '<tr> <td style="text-align:left;"  colspan="4">' + data[i].text + '</td> </tr>'
                        + '<tr class="tweetStats"> <td style="text-align:left;">Nutrition: ' + data[i].nutrition
                        + ' </td> <td style="text-align:right;"> <text class = "gn-icon-retweet"></text>' + data[i].retweet_count
                        + '</td> <td style="text-align:left;"> <text class = "gn-icon-star"></text>' + data[i].favorite_count;
                    // different colors for positive/negative/neutral sentiments
                    if (data[i].sentiment_score > 0) {
                        table = table + ' </td> <td style="text-align:right; color: green;">Sentiment: ' + data[i].sentiment_score.toFixed(2) + '</td> </tr>';
                    }
                    else {
                        if (data[i].sentiment_score < 0) {
                            table = table + ' </td> <td style="text-align:right; color: red;">Sentiment: ' + data[i].sentiment_score.toFixed(2) + '</td> </tr>';
                        }
                        else {
                            table = table + ' </td> <td style="text-align:right;">Sentiment: 0</td> </tr>';   
                        }
                    }
                    table = table + '<tr> <td style="height:50px;" colspan="4"> </td>'
                    if (i == 0) {
                        lastShownTweetId = data[0].id;
                    }
                }
                $("#NewTweetsDiv").append(table);
            }
            else {
                console.log("no data");
            }
            getDataByTick();
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log(XMLHttpRequest);
            console.log(textStatus);
            console.log(errorThrown);
            getDataByTick();
        }
    });
});

// Check new tweets every 5000 milliseconds
var tweetUpdate = setInterval(function(){getDataByTick()}, 500000);
var lastShownTweetId = 0;
var newTweets;
var nutrition = 0;
var sentiment = 0;

// Check new tweets
function getDataByTick(){
    $.ajax({
        url: 'http://' + window.serverIP + ':27080/' + window.databaseName + '/tweets/_find?sort=%7B%22id%22%3A-1%7D&limit=50',
        type: 'POST',
        dataType: 'jsonp',
        success: function(response) {
            if (response.results != undefined) {
                var data = response.results;
                newTweets = data;
                j = 0;
                newer = true;
                // Count the nubmer of new tweets and put it to j
                for (var i = 0; i < data.length; i++) {
                    if (data[i].id != lastShownTweetId && newer) {
                        j++;
                    }
                    else {
                        newer = false;
                    }
                }
                if (j != 0) {
                    $("#tweetUpdate").show();
                    $("#tweetUpdate").html('<p class="newTweets" id="pUpdate"> Show new tweets (' + j + ')</p>');
                }
            }
            else {
                console.log("no data");
            }
            
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('error', errorThrown);
        }
    });
    
    $.ajax({
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/event/_find',
        type: 'GET',
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
            console.log("EVENT DATA", data);
            nutrition = data.rows[0].nutrition_day;
            sentiment = data.rows[0].sentiment_day;
            
            // Put data to energy div
            if (data.rows[0].energy != undefined) {
                if (data.rows[0].energy_grow == true) {
                    $("#pEnergy").addClass("valuePositive");
                    //$("#pEnergy").append('<text class = "gn-icon gn-icon-up"></text>');                   
                }
                else {
                    $("#pEnergy").addClass("valueNegative");
                }
            }
            else {
                 $("#pEnergy").addClass("value");
            }
            energy = data.rows[0].energy;
            if (energy == undefined) {
                energy = 0;
            }
            $("#pEnergy").text(numberWithCommas(energy.toFixed(0)));
            
            // Put data to tweets div
            if (data.rows[0].tweets_sum != undefined) {
                 $("#pTweets").addClass("value");
            }
            $("#pTweets").text(numberWithCommas(data.rows[0].tweets_sum.toFixed(0)));
            
            // Put data to favorites div
            if (data.rows[0].tweets_sum != undefined) {
                 $("#pFavorites").addClass("value");
            }
            favSum = data.rows[0].favorites_sum;
            if (favSum == undefined) {
                favSum = 0;
            }
            $("#pFavorites").text(numberWithCommas(favSum.toFixed(0)));
            
            // Put data to retweets div
            if (data.rows[0].tweets_sum != undefined) {
                 $("#pRetweets").addClass("value");
            }
            retw_sum = data.rows[0].retweets_sum;
            if (retw_sum == undefined) {
                retw_sum = 0;
            }
            $("#pRetweets").text(numberWithCommas(retw_sum.toFixed(0)));
            
            // Put data to sentiment div
            $("#pSenPositive").text(numberWithCommas(data.rows[0].sentiment_positive_amount));
            $("#pSenNeutral").text(numberWithCommas(data.rows[0].sentiment_neutral_amount));
            $("#pSenNegative").text(numberWithCommas(data.rows[0].sentiment_negative_amount));
            if (data.rows[0].sentiment_positive_amount != undefined) {
                avg = data.rows[0].sentiment_positive_sum / data.rows[0].sentiment_positive_amount;                
                $("#pSenPositiveAvg").text(numberWithCommas(avg.toFixed(2)));
            }
            else {
                $("#pSenPositiveAvg").text("0");
            }
            if (data.rows[0].sentiment_neutral_amount != undefined) {
                avg = data.rows[0].sentiment_neutral_sum / data.rows[0].sentiment_neutral_amount;                
                $("#pSenNeutralAvg").text(numberWithCommas(avg.toFixed(2)));
            }
            else {
                $("#pSenNeutralAvg").text("0");
            }
            if (data.rows[0].sentiment_negative_amount != undefined) {
                avg = data.rows[0].sentiment_negative_sum / data.rows[0].sentiment_negative_amount;                
                $("#pSenNegativeAvg").text(numberWithCommas(avg.toFixed(2)));
            }
            else {
                $("#pSenNegativeAvg").text("0");
            }
            
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('error', errorThrown);
        }
    });
}

function numberWithCommas(x) {
    if (x != undefined) {
        return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
    }
    else
        return 0;
}

// Draw dynamical graph for Nutrition value
$(function drawNutrition() {
    var n = 60,
        duration = 2000,
        now = new Date(Date.now() - duration),
        count = 0,
        data = d3.range(n).map(function() { return 0; });
    var margin = {top: 5, right: 20, bottom: 20, left: 40};
    var divHeight = document.getElementById('NutritionGraphDiv').offsetHeight;
    // Append svg into div element
    var svg = d3.select("div.nutrition").append("svg")
        .attr("width", "100%")
        .attr("height", divHeight - 60)
        .attr("id", "svgNutrition")
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    svgWidth = $("#svgNutrition").outerWidth();
    svgHeight = $("#svgNutrition").outerHeight();
    width = svgWidth - margin.left - margin.right,
    height = svgHeight - margin.top - margin.bottom;
    var x = d3.time.scale()
        .domain([now - (n - 2) * duration, now - duration])
        .range([0, width]);
    var y = d3.scale.linear()
        .domain([0, 500])
        .range([height, 0]);
    var line = d3.svg.line()
        .interpolate("basis")
        .x(function(d, i) { return x(now - (n - 1 - i) * duration); })
        .y(function(d, i) { return y(d); });
        
    svg.append("defs").append("clipPath")
        .attr("id", "clip")
      .append("rect")
        .attr("width", width.toFixed(0))
        .attr("height", height.toFixed(0));
        
    var axis = svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(x.axis = d3.svg.axis().scale(x).orient("bottom"));
    var yaxis = svg.append("g")
        .attr("class", "y axis")
        .call(d3.svg.axis().scale(y).orient("left"));
    var path = svg.append("g")
        .attr("clip-path", "url(#clip)")
      .append("path")
        .datum(data)
        .attr("class", "line")
        .style('stroke-width', 3)
        .style('stroke', "#3E508C");
    tick();

    // function that redraws the graph and makes it dynamical
    function tick() {
        data.push(nutrition);
        now = new Date();
        x.domain([now - (n - 2) * duration, now - duration]);
        ydomain = d3.min(data) - 10;
        if (ydomain < 0) {
            ydomain = 0;
        }
        y.domain([ydomain, d3.max(data) + 10]);
        svg.select(".line")
            .attr("d", line)
            .attr("transform", null);
        yaxis.transition()
            .duration(duration)
            .ease("linear")
            .call(d3.svg.axis().scale(y).orient("left"));
        // rescale the y axis
        axis.transition()
            .duration(duration)
            .ease("linear")
            .call(x.axis);
        // slide the line left
        path.transition()
            .duration(duration)
            .ease("linear")
            .attr("transform", "translate(" + x(now - (n - 1) * duration) + ")")
            .each("end", tick);
        data.shift();
    }
});

// Draw dynamical graph for Sentiment value
$(function drawSentiment() {
    var n = 60,
        duration = 2000,
        now = new Date(Date.now() - duration),
        count = 0,
        data = d3.range(n).map(function() { return 0; });
    var margin = {top: 5, right: 20, bottom: 20, left: 40};
    var divHeight = document.getElementById('SentimentGraphDiv').offsetHeight;
    // Append svg for sentiment graph
    var svg = d3.select("div.sentiment").append("svg")
        .attr("width", "100%")
        .attr("height", divHeight - 60)
        .attr("id", "svgSentiment")
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
        svgWidth = $("#svgNutrition").outerWidth();
        svgHeight = $("#svgNutrition").outerHeight();
        width = svgWidth - margin.left - margin.right,
        height = svgHeight - margin.top - margin.bottom;
    var x = d3.time.scale()
        .domain([now - (n - 2) * duration, now - duration])
        .range([0, width]);
    var y = d3.scale.linear()
        .domain([0, 500])
        .range([height, 0]);
    var line = d3.svg.line()
        .interpolate("basis")
        .x(function(d, i) { return x(now - (n - 1 - i) * duration); })
        .y(function(d, i) { return y(d); });
    svg.append("defs").append("clipPath")
        .attr("id", "clip")
      .append("rect")
        .attr("width", width)
        .attr("height", height);
    var axis = svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(x.axis = d3.svg.axis().scale(x).orient("bottom"));
    var yaxis = svg.append("g")
        .attr("class", "y axis")
        .call(d3.svg.axis().scale(y).orient("left"));
    var path = svg.append("g")
        .attr("clip-path", "url(#clip)")
      .append("path")
        .datum(data)
        .attr("class", "line")
        
        .style('stroke-width', 3)
        .style('stroke', "#3E508C");;
    tick();

    // function that animates the graph
    function tick() {
        data.push(sentiment);
        now = new Date();
        x.domain([now - (n - 2) * duration, now - duration]);
        ydomain = d3.min(data) - 10;
        
        y.domain([ydomain, d3.max(data) + 10]);
        svg.select(".line")
            .attr("d", line)
            .attr("transform", null);
        yaxis.transition()
            .duration(duration)
            .ease("linear")
            .call(d3.svg.axis().scale(y).orient("left"));
        // rescale the y axis
        axis.transition()
            .duration(duration)
            .ease("linear")
            .call(x.axis);
        // slide the line left
        path.transition()
            .duration(duration)
            .ease("linear")
            .attr("transform", "translate(" + x(now - (n - 1) * duration) + ")")
            
            .each("end", tick);
        data.shift();
    }
});

// get index of the node or -1 if there is no such node
function checkExists(inArr, name)
{
    for (i = 0; i < inArr.length; i++) {
        if (inArr[i].name == name) {
            return i;
        }
    }
    return -1;
}

// Click event for "show new tweets" button (click on tweetUpdate div)
function showTweets(){
    table = '<table id="NewTweetsTable">'
    if (newTweets != undefined) {
        for (var i = 0; i < newTweets.length; i++) {
            len = newTweets[i].created_at.length;
            table = table + '<tr style="background: #C5DAF0"> <td style="text-align:left;" colspan="2" >@' + newTweets[i].user
                    + '</td> <td  style="text-align:right;" colspan="2">' + newTweets[i].created_at.substring(4, 16) + "</td> </tr>"
                    + '<tr> <td style="text-align:left;"  colspan="4">' + newTweets[i].text + '</td> </tr>'
                    + '<tr class="tweetStats"> <td style="text-align:left;">Nutrition: ' + newTweets[i].nutrition
                    + ' </td> <td style="text-align:right;"> <text class = "gn-icon-retweet"></text>' + newTweets[i].retweet_count
                    + '</td> <td style="text-align:left;"> <text class = "gn-icon-star"></text>' + newTweets[i].favorite_count;
                    // Different colors for positive/negarive/neurtral sentiment
                    if (newTweets[i].sentiment_score > 0) {
                        table = table + ' </td> <td style="text-align:right; color: green;">Sentiment: ' + newTweets[i].sentiment_score.toFixed(2) + '</td> </tr>';
                    }
                    else {
                        if (newTweets[i].sentiment_score < 0) {
                            table = table + ' </td> <td style="text-align:right; color: red;">Sentiment: ' + newTweets[i].sentiment_score.toFixed(2) + '</td> </tr>';
                        }
                        else {
                            table = table + ' </td> <td style="text-align:right;">Sentiment: 0</td> </tr>';   
                        }
                    }
                    
                    table = table + '<tr> <td style="height:50px;" colspan="4"> </td>'
            if (i == 0) {
                lastShownTweetId = newTweets[0].id;
            }
            var el = document.getElementById( 'NewTweetsTable' );
            if (el != undefined)
                el.parentNode.removeChild(el);
            $("#NewTweetsDiv").append(table);
            $("#tweetUpdate").hide();
        }
    }
};

// Show tip
function showTip(elementId) {    
    var text;
    switch(elementId.id) {
    case "newTweets_tip":
        text = "<h3 style='text-align: center;'>New Tweets</h3><h4 style='margin: 5px;'> Description: </h4><p>Event related tweets appear in real-time</p>";
        break;
    case "energy_tip":
        text = "<h3 style='text-align: center;'>Energy</h3><h4 style='margin: 5px;'> Description: </h4><p>Energy shows the popularity of the event. When it is red the " +
        "popularity is less than yesterday (it decreases), when green - more than yesterday (it increases).</p>";
        break;
    case "tweets_tip":
        text = "<h3 style='text-align: center;'>Nubmer of Tweets</h3><h4 style='margin: 5px;'> Description: </h4><p>Number of tweets related to the event</p>";
        break;
    case "sentiment_tip":
        text = "<h3 style='text-align: center;'>Sentiment</h3><h4 style='margin: 5px;'> Description: </h4><p>Number of tweets that are positive, negative and neutral " +
        "and average value of sentiment for the corresponding sentiment types</p>";
        break;
    case "retweets_tip":
        text = "<h3 style='text-align: center;'>Number of Retweets</h3><h4 style='margin: 5px;'> Description: </h4><p>Number of retweets for tweets related to the event</p>";
        break;
    case "favorites_tip":
        text = "<h3 style='text-align: center;'>Number of Favorites</h3><h4 style='margin: 5px;'> Description: </h4><p>Number of favorites for tweets related to the event</p>";
        break;
    case "nutritionNow_tip":
        text = "<h3 style='text-align: center;'>Nutrition Real-time</h3><h4 style='margin: 5px;'> Description: </h4><p>Change of the nutrition value in real-time</p>" +
        "<h4>Tip:</h4> <p>When a new tweet appears you may see the change of nutrition</p>";
        break;
    case "sentimentNow_tip":
        text = "<h3 style='text-align: center;'>Sentiment Real-time</h3><h4 style='margin: 5px;'> Description: </h4><p>Change of summing up sentiment value in real-time. " +
        "Sentiment of all sentiment types (positive, negative and neutral) are summed up in one value. Change of the value is shown on the graph</p>" +
        "<h4>Tip:</h4> <p>When a new tweet appears you may see the change of the sentiment value</p>"
        break;
    case "other":
        text = "Other";
    default:
        text="";
    }
    $("#messageToolTipDiv").html(text);
    $("#messageToolTipDiv").css("marginLeft", mouseX - 250 + "px")
      .css("marginTop", mouseY - 60 + "px")
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