// Get user network data from database
$(function upd(){
    $.ajax({
        // get data from "aggregated" collection
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/user_network/',
        type: 'GET',                                                                                           
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success:function(response) {
            //console.log("User network data", response);
            if (response.rows != undefined) {
                var data = response.rows;
                nodes = [];
                links = [];
                // nodes creation
                for (var i = 0; i < data.length; i++) {
                    if (checkExists(nodes, data[i].screen_name) == -1) {
                        name = data[i].screen_name;
                        mentioned = data[i].mentioned;
                        if (mentioned == undefined) {
                            mentioned = 0;
                        }
                        tweets_count = data[i].tweets_count;
                        if (tweets_count == undefined) {
                            tweets_count = 0;
                        }
                        if (name != undefined) {
                            nodes.push(JSON.parse('{"name":"' + name + '", "occur":' + tweets_count + ', "mentioned":' + mentioned + '}'));
                        }
                    }
                }
                // links creation
                for (var i = 0; i < data.length; i++) {
                    var nodeIndex = checkExists(nodes, data[i].screen_name);
                    for (var key in data[i].in_links) {
                        var sourceIndex = checkExists(nodes, key);
                        if (sourceIndex != -1) {
                            if (data[i].in_links.hasOwnProperty(key)) {
                                links.push(JSON.parse('{"source":' + sourceIndex + ',"target":' + nodeIndex + ',"value":' + data[i].in_links[key] + '}'));
                            }
                        }
                    }
                    for (var key in data[i].out_links) {
                        var targetIndex = checkExists(nodes, key);
                        if (targetIndex != -1) {
                            if (data[i].out_links.hasOwnProperty(key)) {
                                links.push(JSON.parse('{"source":' + nodeIndex + ',"target":' + targetIndex + ',"value":' + data[i].out_links[key] + '}'));
                            }
                        }
                    }
                }
                jsonData.nodes = nodes;
                jsonData.links = links;
                drawData(jsonData);
            }
            else {
                console.log("no data");
            }
        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            console.log(xhr);
            console.log(err);
            console.log(msg);
        }
    });
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
   
// Draw user network
function drawData(jsonData) {    
    var margin = {top: 20, right: 20, bottom: 30, left: 50};
    divHeight = $("#UsersGraphDiv").outerHeight();
    var svgGraph = d3.select("#UsersGraphDiv").append("svg")
        .attr("width", "100%")
        .attr("height", divHeight)
        .attr("id", "usersSvg")
      .append("g");
    var svgWidth = $("#usersSvg").outerWidth();
        svgHeight = $("#usersSvg").outerHeight();
    var width = svgWidth,
        height = svgHeight;
    var color = d3.scale.category20();
    var graph = jsonData;
    var drag = false;
    var r = 6;
    var force = d3.layout.force()
        .charge(-200)
        .linkDistance(50)
        .chargeDistance(140)
        .size([width, height]);
    force.nodes(jsonData.nodes)
         .links(jsonData.links)
         .start();
    var link =   svgGraph.selectAll(".link")
        .data(jsonData.links)
        .enter().append("line")
        .attr("class", "link")
        .attr("z-index",1)
        .attr("position", "absolute")
        .style("stroke-width", function (d) {if (d.value != 0) {res = Math.log(d.value);  if (res > 3) return 3; if (res < 1) return 1; return res;} else return 1;})
        .style("stroke", "#E6F0FC");
    var node = svgGraph.selectAll(".node")
        .data(jsonData.nodes)
        .enter().append("circle")
        .attr("class", "node")
        .attr("z-index",1)
        .attr("position", "relative")
        .attr("r", function(d) { var size = Math.log(d.occur); size = 3*size; if (size < 3) size = 3; if (size > 20) size = 20; return size; })
        .style("fill", function(d) { return color(d.occur); })
        .style("stroke", "#34495e")
        .call(force.drag)
        .on('mouseover', function(d) {    if (!drag){   $("#messageToolTipDiv").css({top:d3.mouse(this)[1] - 20,left:d3.mouse(this)[0] + 80});
                                                        $("#messageToolTipDiv").html("<p>User: "+d.name+"<p>Mentioned: "+d.mentioned);
                                                        $("#messageToolTipDiv").css("opacity", 1);
                                                        $("#messageToolTipDiv").show();}     })
        .on("mousedown", function(d) {drag = true;} )
        .on("mouseup", function(d) {drag = false;} )
        .on('mouseout', function(d) {   $("#messageToolTipDiv").hide();    })
        .on('click', function(d) { data = getUser(d.name, d.mentioned);
        }); 
    force.on("tick", function() {
        link.attr("x1", function(d) { return d.source.x; })
            .attr("y1", function(d) { return d.source.y; })
            .attr("x2", function(d) { return d.target.x; })
            .attr("y2", function(d) { return d.target.y; });
        node.attr("cx", function(d) { return d.x = Math.max(r, Math.min(width - r, d.x)); })
            .attr("cy", function(d) { return d.y = Math.max(r, Math.min(height - r, d.y)); });
    });
}

// Get infromation about the clicked on the graph user
function getUser(username, mentioned){
    $.ajax({
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/users/?filter_screen_name=' + username,
        type: 'GET',                                                                                           
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
            console.log("TEST", data);
            var info;
            if (data.rows[0].listed_count != undefined) {
                // create p element with user info
                info = '<p style="text-align: center; font-size:18px;"> ' + '<a class="classUserInfo" href="http://www.twitter.com/' + username
                    + '" target="_blank">' + username + '</a>'
                    + " - "
                if (data.rows[0].url != null) {
                    info = info + '<a class="classUserInfo" href="' + data.rows[0].url + '" target="_blank">' + data.rows[0].name + ' <text class = "gn-icon gn-icon-link"></text></a>  </p>';
                }
                else {
                     info = info + data.rows[0].name;
                }
                if (data.rows[0].description != null) {
                    info = info + '<p style="text-align: center; font-size:14px;">' + data.rows[0].description
                }
                info = info  + "<p>Mentioned: " + mentioned
                + "<p>Number of followers: " + data.rows[0].followers_count
                + "<p>Follows: " + data.rows[0].friends_count
                + "<p>Listed: " + data.rows[0].listed_count
                                + "<p>Total number of tweets: " + data.rows[0].statuses_count
                                   + "<p>Tweets related to the event: " + data.rows[0].tweets_count    
                                  + "<p>Nutrition: " + data.rows[0].nutrition
                                   + "<p>Sentiment: " + (data.rows[0].sentiment_score/data.rows[0].tweets_count).toFixed(2)
                                   + "<p>Language: " + data.rows[0].lang;
                if (data.rows[0].location != null) {
                    info = info + '<p>Location: ' + data.rows[0].location;
                }
                info = info +  "<p>User is created at: "+ data.rows[0].created_at.substring(4, 8) + data.rows[0].created_at.substring(data.rows[0].created_at.length-4, data.rows[0].created_at.length);
            }
            else {
                if (data.rows.length != 0) {
                    info = '<p style="text-align: center; font-size:18px;"> ' + '<a class="classUserInfo" href="http://www.twitter.com/' + username
                        + '" target="_blank">' + username + '</a>' + "<p>Mentioned: " + mentioned + "<p>Tweets related to the event: 0";
                }                
            }
            info = info + '<p id="userInfo_tip" onmouseover="showTip(this)" onmouseout="noTip()"; style="position: absolute; top: 4px; right: 0px; color: #2E73B8;" class = "gn-icon gn-icon-info"></p>'
            $("#UserInfoDiv").html(info);  
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('error', errorThrown);
        }
    });
}

// Get data about most mentioned users
$(function(){
    $.ajax({
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/most_mentioned_users/',
        type: 'GET',
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
            console.log("json loaded successfully", data);
            // Create table element
            table = '<table id="MentionedUsersTable"> <tr style="background: #B7D1EB;"> <td style="text-align: center;">Users </td> <td>Number of Mentions</td> </tr>'
            for (var i = 0; i < data.rows.length; i++) {
                mentioned = data.rows[i].mentioned;
                if (mentioned == undefined) {
                    mentioned = 0;
                }
                table = table + "<tr> <td>" + (i+1) + ". @" + data.rows[i].screen_name + "</td> <td>" + mentioned + "</td> </tr>"
            }
            table = table + '</table>';
            var el = document.getElementById( 'MentionedUsersTable' );
            if (el != undefined)
                el.parentNode.removeChild(el);
            $("#MostMentionedUsersDiv").append(table);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('error', errorThrown);
        }
    });
});

// Get data about most followed users
$(function(){
    $.ajax({
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/most_followed_users/',
        type: 'GET',
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
            console.log("json loaded successfully.", data);
            // Create table element
            table = '<table id="FollowedUsersTable"> <tr style="background: #B7D1EB;"> <td style="text-align: center;">Users </td> <td>Number of followers</td> </tr>'
            for (var i = 0; i < data.rows.length; i++) {
                followers = data.rows[i].followers_count;
                if (followers == undefined) {
                    followers = 0;
                }
                table = table + "<tr> <td>" + (i+1) + ". @" + data.rows[i].screen_name + "</td> <td>" + followers + "</td> </tr>"
            }
            table = table + '</table>';
            var el = document.getElementById( 'FollowedUsersTable' );
            if (el != undefined)
                el.parentNode.removeChild(el);
            $("#MostFollowedUsersDiv").append(table);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('error', errorThrown);
        }
    });
});

// Get users with max nutrition value
$(function(){
    $.ajax({
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/most_auth_users/',
        type: 'GET',
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
            console.log("json loaded successfully.", data);
            // Create table element
            table = '<table id="AuthUsersTable"> <tr style="background: #B7D1EB;"> <td style="text-align: center;">Users </td> <td>Number of nutrition</td> </tr>'
            console.log();
            for (var i = 0; i < data.rows.length; i++) {
                nutrition = data.rows[i].nutrition;
                if (nutrition == undefined) {
                    nutrition = 0;
                }
                table = table + "<tr'> <td>" + (i+1) + ". @" + data.rows[i].screen_name + "</td> <td>" + nutrition + "</td> </tr>"
            }
            table = table + '</table>';
             var el = document.getElementById( 'AuthUsersTable' );
            if (el != undefined)
                el.parentNode.removeChild(el);
            $("#MaxAuthUsersDiv").append(table);            
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('error', errorThrown);
        }
    });
});

// Get user languages info
$(function(){
    $.ajax({
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/user_languages/',
        type: 'GET',
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
            console.log("json loaded successfully.", data);
            drawBar(data.rows, "lang", "Users", "Languages");
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('error', errorThrown);
        }
    });
});

// Get user types info
$(function(){
    $.ajax({
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/user_types/',
        type: 'GET',
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
            console.log("json loaded successfully.", data);
            drawBar(data.rows, "types", "Users", "Types");
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('error', errorThrown);
        }
    });
});


// Draw user languages and user types bars
function drawBar(data, cls, xName, yName) {
    var margin = {top: 40, right: 80, bottom: 30, left: 100};
    var barWidth = document.body.clientWidth * 0.5 - 250;
    var barHeight = 220;
    var formatPercent = d3.format(".0%");
    var x = d3.scale.linear()
            .range([0, barWidth]);
    var y = d3.scale.ordinal()
            .rangeRoundBands([0, barHeight], .1);
    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom");
    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left");
    var tip = d3.tip()
      .attr('class', 'd3-tip')
      .offset([-10, 0])
      .html(function(d) {
        return "<strong>Users:</strong> <span style='color:#00EEFF'>" + d.count + "</span>";
      })
    divName = 'div.' + cls
    var svgBar = d3.select(divName).append("svg")
        .attr("width", "100%")
        .attr("height", "300px")
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    svgBar.call(tip);
    divWidth = document.getElementById("UserTypesDiv").offsetWidth;
    console.log("123", divWidth);
      y.domain(data.map(function(d) { return d._id; }));
      x.domain([0, d3.max(data, function(d) { return d.count; })]);
      svgBar.append("g")
          .attr("class", "x axis")
          .attr("transform", "translate(0," + barHeight + ")")
          .call(xAxis)
          .append("text")
          .attr("x", divWidth - 130)
          .style("text-anchor", "end")
          .text(xName);
      svgBar.append("g")
          .attr("class", "y axis")
          .call(yAxis)
          .append("text")
          .attr("transform", "rotate(-90)")
          .attr("y", 0)
          .style("text-anchor", "end");
      svgBar.selectAll(".bar")
          .data(data)
          .enter().append("rect")
          .attr("class", "bar")
          .attr("x", 0)
          .attr("width", function(d) { return x(d.count); })
          .attr("y", function(d) { return y(d._id); })
          .attr("height", y.rangeBand())
          .on('mouseover', tip.show)
          .on('mouseout', tip.hide);
}

// Bar element 
function bar(d) {
    var bar = svg.insert("g", ".y.axis")
        .attr("class", "enter")
        .attr("transform", "translate(0,5)")
        .selectAll("g")
        .enter().append("g");
    bar.append("text")
        .attr("x", -6)
        .attr("y", barHeight / 2)
        .attr("dy", ".35em")
        .style("text-anchor", "end")
        .text(function(d) { cosnole.log("jikjkl", d); return d._id; });
    bar.append("rect")
        .attr("width", function(d) { return x(d.count); })
        .attr("height", barHeight);
  return bar;
}

// A stateful closure for stacking bars horizontally.
function stack(i) {
  var x0 = 0;
  return function(d) {
    var tx = "translate(" + x0 + "," + barHeight * i * 1.2 + ")";
    x0 += x(d.value);
    return tx;
  };
}

var jsonData = {};
jsonData.nodes = [];
jsonData.links = [];

// Show tip
function showTip(elementId) {    
    var text;
    switch(elementId.id) {
    case "userGrap_tip":
        text = "<h3 style='text-align: center;'>Most Active Users Graph</h3><h4 style='margin: 5px;'> Description: </h4><p> The graph shows users as nodes. "
        + "The links between nodes show that these people mentioned each other in tweets.</p>" +
         "<h4>Important:</h4> <p>The graph shows not all users! The users are the most active users - the users who have written most tweets.</p>" +
        "<h4>Tip:</h4> <p>You may click on node and get more information about each user. If a user has his/her own website - you may find a link at the right part of a titel and also open the website on a new tab</p>";
        break;
    case "userInfo_tip":
        text = "<h3 style='text-align: center;'>User Information</h3><h4 style='margin: 5px;'> Description: </h4><p> User profile information provided in Twitter</p>" +
        "<h4>Tip:</h4> <p>You may click on a username in the title and open a user page on twitter.com on a new tab</p>";
        break;
    case "mention_tip":
        text = "<h3 style='text-align: center;'>Most Mentioned Users</h3><h4 style='margin: 5px;'> Description: </h4><p> Users most frequently mentioned by other users in tweets</p>";
        break;
    case "important_tip":
        text = "<h3 style='text-align: center;'>Important Users</h3><h4 style='margin: 5px;'> Description: </h4><p> Users that most improved the event's energy and nutrition</p>";
        break;
    case "follow_tip":
        text = "<h3 style='text-align: center;'>Most Followed Users</h3><h4 style='margin: 5px;'> Description: </h4><p> Users who have the most number of followers</p>";
        break;
    case "language_tip":
        text = "<h3 style='text-align: center;'>Languages</h3><h4 style='margin: 5px;'> Description: </h4><p> Distribution of users' languages</p>" +
        "<h4>Tip:</h4> <p> You may check the exact value by moving your cursor over a bar</p>";
        break;
    case "type_tip":
        text = "<h3 style='text-align: center;'>User Types</h3><h4 style='margin: 5px;'> Description: </h4><p>Roles of users in an event</p>" +
        "<h5>Broadcasters: Users with more than 100 000 followers</h5>" +
        "<h5>Selebrities: Users that are mentioned more than 100 times</h5>" +
        "<h5>Spreaders: Users that got more retweets then wrote tweets</h5>" +
        "<h5>Active Users: Users that wrote more than 5 tweets about the event</h5>" +
        "<h5>Chatters: Users that replied to other people</h5>";
        break;
    case "other":
        text = "Other";
    default:
        text="";
    }
    $("#messageToolTipDiv").html(text);
    $("#messageToolTipDiv").css("left", mouseX - 250 + "px")
      .css("top", mouseY - 60 + "px")
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