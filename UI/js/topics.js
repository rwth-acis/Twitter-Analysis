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

var topicDictionary = [];

$(function (){
    $.ajax({
        // get data from "aggregated" collection
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/topics/',
        type: 'GET',                                                                                           
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success:function(response) {
            if (response.rows != undefined) {
                var data = response.rows;
                for (var i = 0; i < data.length; i++) {                    
                    // Create topics dictionary
                    var jsonData = {};
                    jsonData.nodes = [];
                    jsonData.links = [];
                    nodes = [];
                    links = [];
                    var label;
                    // initial node creation
                    if (checkExists(nodes, data[i].term) == -1) {
                        label = data[i].label;
                        weight = data[i].label_weight;
                        distribution = data[i].distribution;
                        if (distribution == undefined) {
                            distribution = 0;
                        }
                        jsonData.distribution = distribution;
                        if (weight == undefined) {
                            weight = 0;
                        }
                        if (label != undefined) {
                            nodes.push(JSON.parse('{"name":"' + label + '", "occur":' + weight + '}'));
                            jsonData.label = label;
                        }
                    }                    
                    // links and other nodes creation
                    for (var j = 0; j < data[i].words.length; j++) {
                        name = data[i].words[j].word;
                        weight = data[i].words[j].weight;
                        if (weight == undefined) {
                            weight = 0;
                        }
                        // add node
                        if (name != undefined) {
                            nodes.push(JSON.parse('{"name":"' + name + '", "occur":' + weight + '}'));
                        }                        
                        var nodeIndex = checkExists(nodes, data[i].words[j].word);
                        // add link from initial node to other node
                        if (nodeIndex != -1) {
                           links.push(JSON.parse('{"source": 0, "target":' + nodeIndex + '}'));
                        }
                    }
                    jsonData.nodes = nodes;
                    jsonData.links = links;
                    topicDictionary.push(jsonData);
                    
                }
                topicDictionary.sort(function(a,b) { return parseFloat(b.distribution) - parseFloat(a.distribution) } );
                // create topics list as table
                table = '<table id="TopicsTable" style="margin: 0px; padding:0px; border:0px;">'                
                for (var i = 0; i < topicDictionary.length; i++) {
                    table = table + '<tr> <td  style="border: 1px solid #5f6f81;">' + topicDictionary[i].label + "</td> </tr>"
                }                
                table = table + '</table>';
                var el = document.getElementById( 'TopicsTable' );
                if (el != undefined)
                    el.parentNode.removeChild(el);
                $("#TopicsList").append(table);
                console.log(topicDictionary);
                // add onclick event to rows of the table
                addListenerToTable();
                // pick first topic
                $('#TopicsTable tr:first-child').addClass('highlighted');
                drawTopicInfo(0);
                drawTopicGraph(0);
            }
            else {
                console.log("no data");
            }
        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            console.log('error', errorThrown);
        }
    });
});

function addListenerToTable() {
    var table = document.getElementById("TopicsTable");
    var rows = table.rows;
    for (var i = 0; i < rows.length; i++) {
        rows[i].onclick = (function() {
            var topicIndex = i; 
            return function() {
                console.log(topicDictionary[topicIndex]);
                var selected = $(this).hasClass("highlighted");
                $("#TopicsTable tr").removeClass("highlighted");
                if(!selected)
                    $(this).addClass("highlighted");
                // draw topic info and graph
                drawTopicInfo(topicIndex);
                drawTopicGraph(topicIndex);
            }    
        })();
    }
}

// draw topic info
function drawTopicInfo(topicIndex) {
    $("#pTopic").html('Topic: ' + topicDictionary[topicIndex].label);
    $("#pDistribution").html('Distribution: ' + topicDictionary[topicIndex].distribution.toFixed(5));
}

// draw topic graph
function drawTopicGraph(topicIndex) {
    console.log("drawTopicGraph");
    
    var jsonData = {};
    jsonData.nodes = topicDictionary[topicIndex].nodes;
    jsonData.links = topicDictionary[topicIndex].links;
    
    if ($("#topicsSvg")[0] != undefined) {
        
        var el = document.getElementById('topicsSvg');
            if (el != undefined)
                el.parentNode.removeChild(el);
    }  
    
    var margin = {top: 20, right: 20, bottom: 30, left: 50};
        divHeight = $("#TopicGraph").outerHeight();
        svgGraph = d3.select("#TopicGraph").append("svg")
            .attr("width", "100%")
            .attr("height", divHeight)
            .attr("id", "topicsSvg")
          .append("g");
        var svgWidth = $("#topicsSvg").outerWidth();
            svgHeight = $("#topicsSvg").outerHeight();
        width = svgWidth;
        height = svgHeight;
        color = d3.scale.category20();
        graph = jsonData;
        drag = false;
        r = 6;
    
        force = d3.layout.force()
                .gravity(0)
                .linkStrength(0.5)
                .charge(-100)
                .linkDistance(80)
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
            .style("stroke", "#9EAEE8");
  
    // Create the groups under svg
    var gnodes = svgGraph.selectAll('g.gnode')
        .data(jsonData.nodes)
        .enter()
        .append('g')
        .classed('gnode', true)
        .call(force.drag)
        .on('mouseover', function(d) {    if (!drag){   $("#messageToolTipDiv").css("left", event.pageX - 250 + "px");
                                                        $("#messageToolTipDiv").css("top", event.pageY - 10 + "px");
                                                        $("#messageToolTipDiv").html("<p>Term: "+d.name+"<p>Weight: "+d.occur);
                                                        $("#messageToolTipDiv").css("display","block"); }     })
        .on("mousedown", function(d) {drag = true;} )
        .on("mouseup", function(d) {drag = false;} )
        .on('mouseout', function(d) {  $("#messageToolTipDiv").css("display","none"); });

    // Add one circle in each group
    var node = gnodes.append("circle")
      .attr("class", "node")
      .attr("z-index",1)
      .attr("r", function(d) { var size = Math.log(d.occur); size = 5*size; if (size < 5) size = 5; if (size > 20) size = 20; return size; })
      .style("fill", function(d) { return color(d.occur); })
      .style("stroke", "#34495e");

    // Append the labels to each group
    var labels = gnodes.append("text")
        .attr("class", "label")
        .style("font-size", function(d) { var size = Math.log(d.occur); size = 3*size; if (size < 14) size = 14; if (size > 25) size = 25; return size; })
        .style("fill", "#F8F8FF")
        .attr('cx', function(d) { return d.x  + 5;})
        .text(function(d) { return d.name; });
        
    force.on("tick", function() {
        // lock root node in center
        graph.nodes[0].x = (width / 2) - 20;
        graph.nodes[0].y = height / 2;
        link.attr("x1", function(d) { return d.source.x; })
            .attr("y1", function(d) { return d.source.y; })
            .attr("x2", function(d) { return d.target.x; })
            .attr("y2", function(d) { return d.target.y; });
        gnodes.attr("transform", function(d) {
            return 'translate(' + [Math.max(r, Math.min(width - r, d.x)), d.y = Math.max(r, Math.min(height - r, d.y))] + ')';
        });
    });
}

// Request event name term
$(function (){
    $.ajax({
        // get data from "aggregated" collection
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/term_network/',
        type: 'GET',                                                                                           
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success:function(response) {
            var jsonData = {};
            jsonData.nodes = [];
            jsonData.links = [];
            if (response.rows != undefined) {
                var data = response.rows;
                nodes = [];
                links = [];
                console.log("TERMS DATA", data);
                for (var i = 0; i < data.length; i++) {
                    // nodes creation
                    if (checkExists(nodes, data[i].term) == -1) {
                        term = data[i].term;
                        occurred = data[i].occurrence;
                        if (occurred == undefined) {
                            occurred = 0;
                        }
                        if (term != undefined) {
                            nodes.push(JSON.parse('{"name":"' + term + '", "occur":' + occurred + '}'));
                        }
                    }
                    // links creation
                    var nodeIndex = checkExists(nodes, data[i].term);
                    
                    if (data[i].parent != null) {
                       var sourceIndex = checkExists(nodes, data[i].parent);
                       power = data[i].link_power;
                       if (power == undefined) {
                            power = 0;
                       }
                       links.push(JSON.parse('{"source":' + sourceIndex + ',"target":' + nodeIndex + ',"value":' + power + '}'));
                    }
                }
                jsonData.nodes = nodes;
                jsonData.links = links;
                
                if (jsonData != undefined) {
                    drawData(jsonData);
                }
                else {
                console.log("no data");
                }                
            }
            else {
                console.log("no data");
            }
        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            console.log('error', errorThrown);
        }
    });
});

// Draw terms graph
function drawData(jsonData) {
    
    var margin = {top: 20, right: 20, bottom: 30, left: 50};
    divHeight = $("#TermsGraphDiv").outerHeight();
    var svgGraph = d3.select("#TermsGraphDiv").append("svg")
        .attr("width", "100%")
        .attr("height", divHeight)
        .attr("id", "termsSvg")
      .append("g");
    var svgWidth = $("#termsSvg").outerWidth();
        svgHeight = $("#termsSvg").outerHeight();
    var width = svgWidth,
        height = svgHeight;
    var color = d3.scale.category20();
    var graph = jsonData;
    var drag = false;
    var r = 6;
    
    var force = d3.layout.force()
            .gravity(0)
            .linkStrength(1)
            .charge(-100)
            .linkDistance(70)
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
            .style("stroke", "#9EAEE8");
  
    // Create the groups under svg
    var gnodes = svgGraph.selectAll('g.gnode')
        .data(jsonData.nodes)
        .enter()
        .append('g')
        .classed('gnode', true)
        .call(force.drag)
        .on('mouseover', function(d) {    if (!drag){   $("#messageToolTipDiv").html("<p>Term: "+d.name+"<p>Occurred: "+d.occur);
                                                        $("#messageToolTipDiv").css("left", event.pageX - 250 + "px");
                                                        $("#messageToolTipDiv").css("top", event.pageY - 10 + "px");
                                                        $("#messageToolTipDiv").css("opacity", 1);
                                                        $("#messageToolTipDiv").css("display","block"); }     })
        .on("mousedown", function(d) {drag = true;} )
        .on("mouseup", function(d) {drag = false;} )
        .on('mouseout', function(d) {  $("#messageToolTipDiv").css("display","none"); });

    // Add one circle in each group
    var node = gnodes.append("circle")
      .attr("class", "node")
      .attr("z-index",1)
      .attr("r", function(d) { var size = Math.log(d.occur); size = 2*size; if (size < 5) size = 5; if (size > 20) size = 20; return size; })
      .style("fill", function(d) { return color(d.occur); })
      .style("stroke", "#34495e");

    // Append the labels to each group
    var labels = gnodes.append("text")
        .attr("class", "label")
        .style("font-size", function(d) { var size = Math.log(d.occur); size = 3*size; if (size < 14) size = 14; if (size > 25) size = 25; return size; })
        .style("fill", "#F8F8FF")
        .attr('cx', function(d) { return d.x  + 5;})
        .text(function(d) { return d.name; });
        
    force.on("tick", function() {
        // lock root node in center
        graph.nodes[0].x = (width / 2) - 20;
        graph.nodes[0].y = height / 2;
        link.attr("x1", function(d) { return d.source.x; })
            .attr("y1", function(d) { return d.source.y; })
            .attr("x2", function(d) { return d.target.x; })
            .attr("y2", function(d) { return d.target.y; });
        gnodes.attr("transform", function(d) {
            return 'translate(' + [Math.max(r, Math.min(width - r, d.x)), d.y = Math.max(r, Math.min(height - r, d.y))] + ')';
        });
    });
}


// Show tip
function showTip(elementId) {    
    var text;
    switch(elementId.id) {
    case "topics_tip":
        text = "<h3 style='text-align: center;'>Topics</h3><h4 style='margin: 5px;'> Description: </h4><p> Topics that were discussed during the event via Twitter</p>" +
        "<h4>Tip:</h4> <p>Click on a table line with a topic name on the left to get keywords that are related to the chosen topic</p>";
        break;
    case "keyword_tip":
        text = "<h3 style='text-align: center;'>Keyword correlation </h3><h4 style='margin: 5px;'> Description: </h4><p> Keyword graph that demonstrates the relationships between keywords in tweets</p>" +
        "<h4>Tip:</h4> <p>Drag the nodes to read all the sub-keywords of all nodes</p>";
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