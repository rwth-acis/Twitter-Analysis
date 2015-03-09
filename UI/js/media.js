var height;
var videoData, videoShown,
    picsData, picsShown,
    linksData, linksShown;

// if no picture
function imgError(image) {
    image.onerror = "";
    image.src = "images/no_image.jpg";
    return true;
}

// Get pics data    
$(function(){
    height = $(document).height();
    $.ajax({
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/popular_pics/',
        type: 'GET',
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
            console.log("json loaded successfully.", data);
            if (data.rows.length > 0) {
                picsData = data.rows;
                picsShown = 10;
                table = '<table id="PicsTable">'
                for (var i = 0; i < 10; i++) {
                    len = data.rows[i].created_at.length;
                    table = table + '<tr> <td style="text-align:left;" colspan="2" >@' + data.rows[i].user
                        + '</td> <td style="text-align:right;">' + data.rows[i].created_at.substring(4, 11) + data.rows[i].created_at.substring(len-4, len)
                        + '</td> </tr><tr> <td style="text-align:left;"  colspan="3">' + data.rows[i].text
                        + '</td> </tr><tr> <td style="text-align:center;"  colspan="3"> <img width=100% src="' + data.rows[i].entities.media_url + '" onerror="imgError(this);"'
                        + ' /></td> </tr><tr class="tweetStats"> <td style="text-align:left;"> <text class = "gn-icon-retweet"></text>' + data.rows[i].retweet_count
                        + '</td> <td style="text-align:center;"> <text class = "gn-icon-star"></text>' + data.rows[i].favorite_count
                        + ' </td> <td style="text-align:right;">Nutrition: ' + data.rows[i].nutrition
                        + ' </tr><tr> <td style="height:50px;"  colspan="3">   </td>';
                }
                table = table + '</table>';
                $("#PicsTable").html(table);
                if (picsShown < data.rows.length) {
                    $('#MorePicsDiv').show();
                }
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('error', errorThrown);
        }
    });
});

// Get links data
$(function(){
    $.ajax({
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/popular_links/',
        type: 'GET',
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
            console.log("json loaded successfully.", data);
            if (data.rows.length > 0) {
                linksData = data.rows;
                linksShown = 10;
                table = '<table id="LinksTable">'
                for (var i = 0; i < 10; i++) {
                    len = data.rows[i].created_at.length;
                    table = table + '<tr> <td style="text-align:left;" colspan="2" >@' + data.rows[i].user
                        + '</td> <td  style="text-align:right;">' + data.rows[i].created_at.substring(4, 11) + data.rows[i].created_at.substring(len-4, len)
                        + '</td> </tr><tr> <td style="text-align:left;"  colspan="3">' + data.rows[i].text
                        + '</td> </tr><tr> <td style="text-align:center; height:70px;"  colspan="3"><a class="classUserInfo" href="' + data.rows[i]._id
                        + '" target="_blank">' + data.rows[i].url
                        + ' <text class = "gn-icon gn-icon-link"></text></a></td> </tr><tr class="tweetStats"> <td style="text-align:left;"> <text class = "gn-icon-retweet"></text>' + data.rows[i].retweet_count
                        + '</td> <td style="text-align:center;"> <text class = "gn-icon-star"></text>' + data.rows[i].favorite_count
                        + ' </td> <td style="text-align:right;">Nutrition: ' + data.rows[i].nutrition
                        + ' </tr><tr> <td style="height:50px;"  colspan="3">   </td>';
                }
                table = table + '</table>';
                $("#LinksTable").html(table);
                if (linksShown < data.rows.length) {
                    $('#MoreLinksDiv').show();
                }
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('error', errorThrown);
        }
    });
});

// Get data about the most popular video
$(function(){
    $.ajax({
        url: 'http://' + window.serverIP + ':28017/' + window.databaseName + '/popular_video/',
        type: 'GET',
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
            videoData = data.rows;
            videoShown = 10;
            console.log("json loaded successfully.", data);
            if (data.rows.length > 0) {
                videoData = data.rows;
                table = '<table id="VideoTable">'
                for (var i = 0; i < 10; i++) {
                    var videoId;
                    var regExp = /^.*(youtu.be\/|v\/|embed\/|watch\?|youtube.com\/user\/[^#]*#([^\/]*?\/)*)\??v?=?([^#\&\?]*).*/;
                    var match = data.rows[i]._id.match(regExp);
                    if (match && match[3] && match[3].length == 11){
                        videoId = match[3];
                        len = data.rows[i].created_at.length;
                    table = table + '<tr> <td style="text-align:left;" colspan="2" >@' + data.rows[i].user
                        + '</td> <td  style="text-align:right;">' + data.rows[i].created_at.substring(4, 11) + data.rows[i].created_at.substring(len-4, len)
                        + '</td> </tr><tr> <td style="text-align:left;"  colspan="3">' + data.rows[i].text
                        + '</td> </tr><tr> <td style="text-align:center;"  colspan="3"> <iframe style="width:100%; height:300px;" src="http://www.youtube.com/embed/' + videoId
                        + '" frameborder="0" allowfullscreen></iframe></td> </tr><tr class="tweetStats"> <td style="text-align:left;"> <text class = "gn-icon-retweet"></text>' + data.rows[i].retweet_count
                        + '</td> <td style="text-align:center;"> <text class = "gn-icon-star"></text>' + data.rows[i].favorite_count
                        + ' </td> <td style="text-align:right;">Nutrition: ' + data.rows[i].nutrition
                        + ' </tr><tr> <td style="height:50px;"  colspan="3">   </td>';
                    } else {
                        console.log("Garbage");
                    }
                }
                table = table + '</table>';
                $("#VideoTable").html(table);
                if (videoShown < data.rows.length) {
                    $('#MoreVideoDiv').show();
                }
            }    
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('error', errorThrown);
        }
    });
});


// Show video
function showVideos() {
    console.log(videoShown, videoData.length);
    var table = '';
    for (var i = videoShown; (i < videoShown + 10) && (i < videoData.length); i++) {
        console.log(i);
        var videoId;
        var regExp = /^.*(youtu.be\/|v\/|embed\/|watch\?|youtube.com\/user\/[^#]*#([^\/]*?\/)*)\??v?=?([^#\&\?]*).*/;
        var match = videoData[i]._id.match(regExp);
        if (match && match[3] && match[3].length == 11){
            videoId = match[3];
            len = videoData[i].created_at.length;
            table = table + '<tr> <td style="text-align:left;" colspan="2" >@' + videoData[i].user
                + '</td> <td  style="text-align:right;">' + videoData[i].created_at.substring(4, 11) + videoData[i].created_at.substring(len-4, len)
                + '</td> </tr><tr> <td style="text-align:left;" colspan="3">' + videoData[i].text
                + '</td> </tr><tr> <td style="text-align:center;" colspan="3"> <iframe style="width:100%; height:300px;" src="http://www.youtube.com/embed/' + videoId
                + '" frameborder="0" allowfullscreen></iframe></td> </tr><tr class="tweetStats"> <td style="text-align:left;"> <text class = "gn-icon-retweet"></text>' + videoData[i].retweet_count
                + '</td> <td style="text-align:center;"> <text class = "gn-icon-star"></text>' + videoData[i].favorite_count
                + ' </td> <td style="text-align:right;">Nutrition: ' + videoData[i].nutrition
                + ' </tr><tr> <td style="height:50px;" colspan="3"></td>';
        } else {
            console.log("Garbage");
        }
    }
    
    if (table.length > 0) {
        $('#VideoTable').append(table);
    }
    videoShown = videoShown + 10;
    if (videoShown >= videoData.length) {
        $("#MoreVideoDiv").hide();
    }
}

// Show most popular pictures
function showPics() {
    console.log(picsShown, picsData.length);
    var table = '';
    for (var i = picsShown; (i < picsShown + 10) && (i < picsData.length); i++) {
        len = picsData[i].created_at.length;
        table = table + '<tr> <td style="text-align:left;" colspan="2" >@' + picsData[i].user
            + '</td> <td style="text-align:right;">' + picsData[i].created_at.substring(4, 11) + picsData[i].created_at.substring(len-4, len)
            + '</td> </tr><tr> <td style="text-align:left;"  colspan="3">' + picsData[i].text
            + '</td> </tr><tr> <td style="text-align:center;"  colspan="3"> <img width=100% src="' + picsData[i].entities.media_url  + '" onerror="imgError(this);"'
            + '/></td> </tr><tr class="tweetStats"> <td style="text-align:left;"> <text class = "gn-icon-retweet"></text>' + picsData[i].retweet_count
            + '</td> <td style="text-align:center;"> <text class = "gn-icon-star"></text>' + picsData[i].favorite_count
            + ' </td> <td style="text-align:right;">Nutrition: ' + picsData[i].nutrition
            + ' </tr><tr> <td style="height:50px;" colspan="3"></td>';
    }
    if (table.length > 0) {
        $('#PicsTable').append(table);
    }
    picsShown = picsShown + 10;
    if (picsShown >= picsData.length) {
        $("#MorePicsDiv").hide();
    }
}

// Show most popular links
function showLinks() {
    
    console.log(linksShown, linksData.length);
    var table = '';
    for (var i = linksShown; (i < linksShown + 10) && (i < linksData.length); i++) {
        vlen = linksData[i].created_at.length;
        table = table + '<tr> <td style="text-align:left;" colspan="2" >@' + linksData[i].user
            + '</td> <td  style="text-align:right;">' + linksData[i].created_at.substring(4, 11) + linksData[i].created_at.substring(len-4, len)
            + '</td> </tr><tr> <td style="text-align:left;"  colspan="3">' + linksData[i].text
            + '</td> </tr><tr> <td style="text-align:center; height:70px;"  colspan="3"><a class="classUserInfo" href="' + linksData[i]._id
            + '" target="_blank">' + linksData[i].url
            + ' <text class = "gn-icon gn-icon-link"></text></a></td> </tr><tr class="tweetStats"> <td style="text-align:left;"> <text class = "gn-icon-retweet"></text>' + linksData[i].retweet_count
            + '</td> <td style="text-align:center;"> <text class = "gn-icon-star"></text>' + linksData[i].favorite_count
            + ' </td> <td style="text-align:right;">Nutrition: ' + linksData[i].nutrition
            + ' </tr><tr> <td style="height:50px;" colspan="3"></td>';
    }
    if (table.length > 0) {
        $('#LinksTable').append(table);
    }
    linksShown = linksShown + 10;
    if (linksShown >= linksData.length) {
        $('#MoreLinksDiv').hide();
    }
}

// Show tip
function showTip(elementId) {    
    var text;
    switch(elementId.id) {
    case "pics_tip":
        text = "<h3 style='text-align: center;'>Most popular pictures</h3><h4 style='margin: 5px;'> Description: </h4><p> Tweets with most number of retweets and favorites that contain pictures</p>" +
        "<h4>Tip:</h4> <p>You may compare the popularity of tweets with different media content</p>";
        break;
    case "links_tip":
        text = "<h3 style='text-align: center;'>Most popular links</h3><h4 style='margin: 5px;'> Description: </h4><p> Tweets with most number of retweets and favorites that contain links to other resources (blog posts, websites, etc.)</p>" +
        "<h4>Tip:</h4> <p>You may compare the popularity of tweets with different media content</p>";
        break;
    case "video_tip":
        text = "<h3 style='text-align: center;'>Most popular video</h3><h4 style='margin: 5px;'> Description: </h4><p> Tweets with most number of retweets and favorites that contain video</p>" +
        "<h4>Tip:</h4> <p>You may compare the popularity of tweets with different media content</p>";
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