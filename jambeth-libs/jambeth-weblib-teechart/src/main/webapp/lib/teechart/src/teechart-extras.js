/*
 TeeChart(tm) for JavaScript(tm)
 @fileOverview TeeChart for JavaScript(tm)
 v1.5 January 2013
 Copyright(c) 2012-2013 by Steema Software SL. All Rights Reserved.
 http://www.steema.com

 Licensed with commercial and non-commercial attributes,
 specifically: http://www.steema.com/licensing/html5

 JavaScript is a trademark of Oracle Corporation.
*/
var Tee=Tee||{};
(function(){function r(b,a){return"rgba( "+b[0]+", "+b[1]+", "+b[2]+", "+a+" )"}function n(b,a,d){for(var e=0;e<b.axes.items.length;e++)3<e&&(b.axes.items[e].labels.format.font.setSize(11),b.axes.items[e].format.stroke.fill=d,b.axes.items[e].labels.format.font.fill=a,b.axes.items[e].title.format.font.fill=a,b.axes.items[e].title.format.font.setSize(20),b.axes.items[e].grid.visible=!1,b.axes.items[e].grid.format.stroke.size=0.6,b.axes.items[e].grid.format.stroke.fill="silver")}function s(b){b.title.transparent=!0;
b.walls.visible=!1;b.footer.transparent=!0;b.panel.format.shadow.visible=!1;b.panel.format.stroke.fill="";b.panel.format.round.x=0;b.panel.format.round.y=0;b.panel.format.gradient.visible=!1;b.panel.format.fill="white";k(b,"seaWash");if(0<b.series.items.length)for(var a=0;a<b.series.items.length;a++)b.series.items[a].format.fill=b.palette.get(a),null!=b.series.items[a].pointer&&(b.series.items[a].pointer.format.fill=b.palette.get(a),b.series.items[a].pointer.format.stroke.fill="white");b.axes.left.format.stroke.fill=
"rgba(39,79,105,0.8)";b.axes.bottom.format.stroke.fill="rgba(39,79,105,0.8)";b.axes.left.format.stroke.fill="rgba(0,0,0,0.0)";b.axes.left.labels.format.font.setSize(14);b.axes.bottom.labels.format.font.setSize(14);b.axes.left.labels.format.font.fill="rgba(124,124,144,0.9)";b.axes.bottom.labels.format.font.fill="rgba(124,124,144,0.9)";b.axes.left.title.format.font.fill="rgba(124,124,144,0.9)";b.axes.left.title.format.font.setSize(20);b.axes.bottom.title.format.font.fill="rgba(124,124,144,0.9)";b.axes.bottom.title.format.font.setSize(20);
b.axes.left.grid.visible=!0;b.axes.bottom.grid.visible=!1;b.axes.left.grid.format.stroke.size=0.6;b.axes.bottom.grid.format.stroke.size=0.6;b.axes.left.grid.format.stroke.fill="silver";b.axes.bottom.grid.format.stroke.fill="silver";0<b.axes.items.length&&n(b,"rgba(124,124,144,0.9)","rgba(39,79,105,0.8)");b.legend.transparent=!0;b.legend.format.font.setSize(14);b.legend.format.font.fill="rgba(124,124,144,0.9)";b.title.format.shadow.visible=!1;b.title.format.font.style="18px Arial";b.title.format.font.style=
"bold 18px Arial";b.title.format.font.fill="rgba(124,124,144,0.9)";b.title.format.font.shadow.visible=!1}function k(b,a){var d="#4466a3 #f39c35 #f14c14 #4e97a8 #2b406b #1d7b63 #b3080e #f2c05d #5db79e #707070 #f3ea8d #b4b4b4".split(" ");"castaway"==a?d="#4466a3 #E8D0A9 #B7AFA3 #C1DAD6 #F5FAFA #ACD1E9 #6D929B".split(" "):"classic"==a?d="#0000FF #00FF00 #00FFFF #FF0000 #FF00FF #FFFF00 #000080 #008000 #008080 #800000 #808000 #808080".split(" "):"cool"==a?d="rgba(43,64,107,1.0) rgba(59,84,140,1.0) rgba(68,102,163,1.0) rgba(78,151,168,1.0) rgba(93,183,158,1.0) rgba(65,160,138,1.0) rgba(43,146,125,1.0) rgba(29,123,99)".split(" "):
"excel"==a?d="#FF9999 #663399 #CCFFFF #FFFFCC #660066 #8080FF #CC6600 #FFCCCC #800000 #FF00FF #00FFFF #FFFF00 #800080 #000080 #808000 #FF0000 #FFCC00 #FFFFCC #CCFFCC #00FFFF #FFCC99 #CC99FF".split(" "):"grayscale"==a?d="#F0F0F0 #E0E0E0 #D0D0D0 #C0C0C0 #B0B0B0 #A0A0A0 #909090 #808080 #707070 #606060 #505050 #404040 #303030 #202020 #101010".split(" "):"macOS"==a?d="#FFFFFF #FCF305 #FF6402 #DD0806 #F20884 #4600A5 #0000D4 #02ABEA #1FB714 #006411 #562C05 #90713A #C0C0C0 #808080 #404040 #000000".split(" "):
"modern"==a?d="#FF9966 #FF6666 #99CCFF #669966 #CCCC99 #9966CC #CC6666 #FFCC99 #9966FF #CCCCCC #66FFCC #6699FF #996699 #CCCCFF".split(" "):"onBlack"==a?d=["rgba(200,230,90,1.0)","rgba(90,150,220,1.0)","rgba(230,90,40,1.0)","rgba(230,160,15)"]:"opera"!=a&&("pastels"==a?d="#CCFFFF #FFFFCC #CCCCFF #00CCCC #CCCCCC #009999 #999999 #DDCCCC #FFCC66 #CCCCFF #FF9999 #FFFF99 #99CCFF #CCFFCC".split(" "):"rainbow"==a?d="#FF0000 #FF7F00 #FFFF00 #00FF00 #0000FF #6600FF #8B00FF".split(" "):"redRiver"==a?d="#DC5C05 #FFC519 #6EC5B8 #FF9000 #978B7D #C7BAA7".split(" "):
"rust"==a?d="#CBFFFA #7F3D17 #7F5E17 #22287F #DD1E2F #EBB035 #06A2CB #218559 #D0C6B1 #B67721 #68819E #747E80 #D5E1DD #F7F3E8 #F2583E #77BED2".split(" "):"seaWash"==a?d="#DC5C05 #FFAC00 #6EC5B8 #E8D0A9 #978B7D #C7BAA7 #C1DAD6 #FFC99F #ACD1E9 #6D929B #D3E397 #FFF5C3".split(" "):"solid"==a?d="#0000FF #FF0000 #00FF00 #FFCC00 #404040 #FFFF00 #FF00C0 #FFFFFF".split(" "):"teechart"==a?d="rgba(255,0,0,1.0) rgba(0,128,0,1.0) rgba(255,255,0,1.0) rgba(0,0,255,1.0) rgba(255,255,255,1.0) rgba(128,128,128,1.0) rgba(255,0,255,1.0) rgba(0,128,128,1.0) rgba(0,0,128,1.0) rgba(128,0,0,1.0) rgba(0,255,0,1.0) rgba(128,128,0,1.0) rgba(128,0,128,1.0) rgba(192,192,192,1.0) rgba(0,255,255,1.0) rgba(0,0,0,1.0) rgba(173,255,47,1.0) rgba(135,206,235,1.0) rgba(255,228,196,1.0) rgba(75,0,130,1.0)".split(" "):
"warm"==a?d="rgba(243,234,141,1.0) rgba(242,192,93,1.0) rgba(243,156,53,1.0) rgba(245,129,28,1.0) rgba(243,107,21,1.0) rgba(241,76,20,1.0) rgba(230,24,10,1.0) rgba(179,8,14)".split(" "):"web"==a?d="#FFA500 #0000CE #00CE00 #FFFF40 #40FFFF #FF40FF #FF4000 #8080A5 #808040".split(" "):"rainbowWide"==a?d="#990000 #C30000 #EE0000 #FF1A00 #FF4600 #FF7300 #FF9F00 #FFCB00 #FFF700 #E3F408 #C3E711 #A3DA1B #83CD25 #63C02E #42B338 #22A642 #029A4B #0C876A #1A758A #2863AA #3650CB #443EEB #612AFF #9615FF #CC00FF".split(" "):
"windowsVista"==a?d="#001FD2 #E00201 #1E6602 #E8CD7E #AFABAC #A4D0D9 #3D3B3C #95DD31 #9E0001 #DCF774 #45FDFD #D18E74 #A0D891 #D57A65 #9695D9".split(" "):"windowsxp"==a?d="rgba(130,155,254,1.0) rgba(252,209,36,1.0) rgba(124,188,13,1.0) rgba(253,133,47,1.0) rgba(253,254,252,1.0) rgba(226,78,33,1.0) rgba(41,56,214,1.0) rgba(183,148,0,1.0) rgba(90,134,0,1.0) rgba(210,70,0,1.0) rgba(211,229,250,1.0) rgba(216,216,216,1.0) rgba(95,113,123,1.0)".split(" "):"victorian"==a&&(d="#5DA5A1 #C45331 #E79609 #F6E84A #B1A2A7 #C9A784 #8C7951 #D8CDB7 #086553 #F7D87B #016484".split(" ")));
b.paletteName=a;b.palette.colors=d;if(0<b.series.items.length)for(d=0;d<b.series.items.length;d++)b.series.items[d].format.fill=b.palette.get(d),null!=b.series.items[d].pointer&&(b.series.items[d].pointer.format.fill=b.palette.get(d));b.draw()}function t(){try{return new XMLHttpRequest}catch(b){}try{return new ActiveXObject("Msxml2.XMLHTTP")}catch(a){}try{return new ActiveXObject("Microsoft.XMLHTTP")}catch(d){}throw Error("Could not create HTTP request object.");}Tee.Chart.prototype.drawReflection=
function(){var b=this.ctx,a=this.bounds.height;b.scale(1,-1);b.translate(0,2*-a);this.ondraw=null;this.draw();b.translate(0,2*a);b.scale(1,-1);var d=this.canvas.height-a,e=b.createLinearGradient(0,a,0,a+d),c=this.reflectionColor;e.addColorStop(0,r(c,0.5));e.addColorStop(1,r(c,1));b.fillStyle=e;b.beginPath();b.shadowColor="transparent";b.rect(0,a,this.bounds.width,d);b.fill();this.ondraw=this.drawReflection};Tee.drawSpline=function(b,a,d,e,c){function g(a,b,c,h,f,d,e){var g=Math.sqrt((c-a)*(c-a)+(h-
b)*(h-b)),g=e*g/(g+Math.sqrt((f-c)*(f-c)+(d-h)*(d-h)));e-=g;return[c+g*(a-f),h+g*(b-d),c-e*(a-f),h-e*(b-d)]}var f=[],h=a.length;if(c){e&&b.moveTo(a[0],a[1]);a.push(a[0],a[1],a[2],a[3]);a.unshift(a[h-1]);a.unshift(a[h-1]);for(c=0;c<h;c+=2)f=f.concat(g(a[c],a[c+1],a[c+2],a[c+3],a[c+4],a[c+5],d));f=f.concat(f[0],f[1]);for(c=2;c<h+2;c+=2)b.bezierCurveTo(f[2*c-2],f[2*c-1],f[2*c],f[2*c+1],a[c+2],a[c+3])}else{for(c=0;c<h-4;c+=2)f=f.concat(g(a[c],a[c+1],a[c+2],a[c+3],a[c+4],a[c+5],d));e&&b.moveTo(a[0],a[1]);
b.quadraticCurveTo(f[0],f[1],a[2],a[3]);for(c=2;c<h-5;c+=2)b.bezierCurveTo(f[2*c-2],f[2*c-1],f[2*c],f[2*c+1],a[c+2],a[c+3]);b.quadraticCurveTo(f[2*h-10],f[2*h-9],a[h-2],a[h-1])}};Tee.Chart.prototype.applyTheme=function(b){if(!b||""==b)this.applyTheme("default");else if("default"==b){this.title.format.font.style="18px Verdana";this.walls.visible=!0;this.panel.format.shadow.visible=!1;this.panel.format.round.x=8;this.panel.format.round.y=8;this.panel.format.gradient.visible=!0;this.panel.format.gradient.colors=
["rgba(224,224,224,1.0)","white"];this.panel.format.gradient.direction="diagonalup";this.panel.format.stroke.fill="rgba(204,204,204,1.0)";this.panel.format.stroke.size=1;k(this,"opera");if(0<this.series.items.length)for(var a=0;a<this.series.items.length;a++)this.series.items[a].format.fill=this.palette.get(a),null!=this.series.items[a].pointer&&(this.series.items[a].pointer.format.fill=this.palette.get(a),this.series.items[a].pointer.format.stroke.fill="white");this.axes.left.labels.format.font.setSize(11);
this.axes.bottom.labels.format.font.setSize(11);this.axes.left.format.stroke.fill="rgba(39,79,105,0.8)";this.axes.bottom.format.stroke.fill="rgba(39,79,105,0.8)";this.axes.left.labels.format.font.fill="rgba(0,0,0,1)";this.axes.bottom.labels.format.font.fill="rgba(0,0,0,1)";this.axes.left.title.format.font.fill="rgba(0,0,0,1)";this.axes.left.title.format.font.setSize(20);this.axes.bottom.title.format.font.fill="rgba(0,0,0,1)";this.axes.bottom.title.format.font.setSize(20);this.axes.left.grid.visible=
!0;this.axes.bottom.grid.visible=!1;this.axes.left.grid.format.stroke.size=0.6;this.axes.bottom.grid.format.stroke.size=0.6;this.axes.left.grid.format.stroke.fill="silver";this.axes.bottom.grid.format.stroke.fill="silver";this.axes.left.grid.visible=!0;this.axes.top.grid.visible=!0;this.axes.right.grid.visible=!0;this.axes.bottom.grid.visible=!0;0<this.axes.items.length&&n(this,"rgba(0,0,0,1)","rgba(39,79,105,0.8)");this.legend.transparent=!1;this.legend.format.fill="white";this.legend.format.font.setSize(11);
this.legend.format.font.fill="rgba(0,0,0,1)";this.legend.fontColor=!1;this.title.format.font.fill="rgba(0,0,0,1)";this.walls.visible=!1}else if("minimal"==b)s(this);else if("excel"==b){if(s(this),k(this,"excel"),this.axes.left.grid.format.stroke.fill="rgba(0,0,0,0.9)",this.axes.bottom.grid.format.stroke.fill="rgba(0,0,0,0.9)",0<this.series.items.length)for(a=0;a<this.series.items.length;a++)this.series.items[a].format.fill=this.palette.get(a),null!=this.series.items[a].pointer&&(this.series.items[a].pointer.format.fill=
this.palette.get(a),this.series.items[a].pointer.format.stroke.fill="white")}else if("dark"==b){k(this,"onBlack");this.title.transparent=!0;this.legend.transparent=!0;this.footer.transparent=!0;this.panel.format.shadow.visible=!1;this.panel.format.stroke.fill="";this.panel.format.round.x=0;this.panel.format.round.y=0;this.panel.format.gradient.colors=["rgba(0,0,0,1)","rgba(0,0,0,1)"];this.panel.format.gradient.visible=!0;if(0<this.series.items.length)for(a=0;a<this.series.items.length;a++)this.series.items[a].format.fill=
this.palette.get(a),null!=this.series.items[a].pointer&&(this.series.items[a].pointer.format.fill=this.palette.get(a),this.series.items[a].pointer.format.stroke.fill="rgba(82,82,82,1)");this.axes.left.format.stroke.fill="rgba(224,224,224,0.6)";this.axes.bottom.format.stroke.fill="rgba(224,224,224,0.6)";this.axes.left.labels.format.font.setSize(14);this.axes.bottom.labels.format.font.setSize(14);this.axes.left.labels.format.font.fill="rgba(224,224,224,0.6)";this.axes.bottom.labels.format.font.fill=
"rgba(224,224,224,0.6)";this.axes.left.title.format.font.fill="rgba(224,224,224,0.6)";this.axes.left.title.format.font.setSize(20);this.axes.bottom.title.format.font.fill="rgba(224,224,224,0.6)";this.axes.bottom.title.format.font.setSize(20);this.axes.bottom.grid.visible=!1;this.axes.left.grid.visible=!0;this.axes.left.grid.format.stroke.fill="silver";this.axes.bottom.grid.format.stroke.fill="silver";0<this.axes.items.length&&n(this,"rgba(224,224,224,0.6)","rgba(39,79,105,0.8)");this.walls.visible=
!1;this.legend.transparent=!0;this.legend.format.font.setSize(14);this.legend.format.font.fill="rgba(224,224,224,0.6)";this.title.format.shadow.visible=!1;this.title.format.font.style="18px Arial";this.title.format.font.style="bold 18px Arial";this.title.format.font.fill="rgba(224,224,224,0.6)";this.title.format.font.shadow.visible=!1}else if("twilight"==b){this.title.format.font.style="18px Verdana";this.walls.visible=!0;this.panel.format.shadow.visible=!1;this.panel.format.round.x=8;this.panel.format.round.y=
8;this.panel.format.gradient.visible=!0;this.panel.format.gradient.colors=["rgba(99,99,99,1.0)","rgba(19,19,19,1.0)"];this.panel.format.gradient.direction="topbottom";this.panel.format.stroke.fill="rgba(204,204,204,1.0)";this.panel.format.stroke.size=1;k(this,"redRiver");if(0<this.series.items.length)for(a=0;a<this.series.items.length;a++)this.series.items[a].format.fill=this.palette.get(a),null!=this.series.items[a].pointer&&(this.series.items[a].pointer.format.fill=this.palette.get(a),this.series.items[a].pointer.format.stroke.fill=
"rgba(82,82,82,1)");this.axes.left.format.stroke.fill="rgba(224,224,224,0.6)";this.axes.bottom.format.stroke.fill="rgba(224,224,224,0.6)";this.axes.left.labels.format.font.setSize(11);this.axes.bottom.labels.format.font.setSize(11);this.axes.left.labels.format.font.fill="rgba(224,224,224,0.6)";this.axes.bottom.labels.format.font.fill="rgba(224,224,224,0.6)";this.axes.left.title.format.font.fill="rgba(224,224,224,0.6)";this.axes.left.title.format.font.setSize(20);this.axes.bottom.title.format.font.fill=
"rgba(224,224,224,0.6)";this.axes.bottom.title.format.font.setSize(20);this.axes.bottom.grid.visible=!1;this.axes.left.grid.visible=!0;this.axes.left.grid.format.stroke.fill="silver";this.axes.bottom.grid.format.stroke.fill="silver";0<this.axes.items.length&&n(this,"rgba(224,224,224,0.6)","rgba(39,79,105,0.8)");this.legend.transparent=!0;this.legend.format.font.setSize(14);this.legend.format.font.fill="rgba(224,224,224,0.6)";this.legend.format.fill="rgba(0,0,0,0.1)";this.title.format.shadow.visible=
!1;this.title.format.font.style="18px Arial";this.title.format.font.style="bold 18px Arial";this.title.format.font.fill="rgba(224,224,224,0.6)";this.title.format.font.shadow.visible=!1;this.walls.visible=!1}else if("daybreak"==b){this.title.format.font.style="18px Verdana";this.walls.visible=!0;this.panel.format.shadow.visible=!1;this.panel.format.round.x=8;this.panel.format.round.y=8;this.panel.format.gradient.visible=!0;this.panel.format.gradient.colors=["rgba(201,204,242,1.0)","rgba(255,252,255,1.0)",
"rgba(21,21,23,1.0)"];this.panel.format.gradient.direction="topbottom";this.panel.format.stroke.fill="rgba(204,204,204,1.0)";this.panel.format.stroke.size=1;k(this,"redRiver");if(0<this.series.items.length)for(a=0;a<this.series.items.length;a++)this.series.items[a].format.fill=this.palette.get(a),null!=this.series.items[a].pointer&&(this.series.items[a].pointer.format.fill=this.palette.get(a),this.series.items[a].pointer.format.stroke.fill="rgba(82,82,82,1)");this.axes.left.format.stroke.fill="rgba(14,14,54,0.6)";
this.axes.bottom.format.stroke.fill="rgba(224,224,224,0.6)";this.axes.left.labels.format.font.setSize(11);this.axes.bottom.labels.format.font.setSize(11);this.axes.left.labels.format.font.fill="rgba(14,14,54,0.6)";this.axes.bottom.labels.format.font.fill="rgba(224,224,224,0.6)";this.axes.left.title.format.font.fill="rgba(14,14,54,0.6)";this.axes.left.title.format.font.setSize(20);this.axes.bottom.title.format.font.fill="rgba(224,224,224,0.6)";this.axes.bottom.title.format.font.setSize(20);this.axes.bottom.grid.visible=
!1;this.axes.left.grid.visible=!0;this.axes.left.grid.format.stroke.fill="silver";this.axes.bottom.grid.format.stroke.fill="silver";0<this.axes.items.length&&n(this,"rgba(224,224,224,0.6)","rgba(39,79,105,0.8)");this.legend.transparent=!0;this.legend.format.font.setSize(14);this.legend.format.font.fill="silver";this.title.format.shadow.visible=!1;this.title.format.font.style="18px Arial";this.title.format.font.style="bold 18px Arial";this.title.format.font.fill="rgba(14,14,54,0.6)";this.title.format.font.shadow.visible=
!1;this.walls.visible=!1}this.themeName=b;this.draw()};Tee.Chart.prototype.applyPalette=function(b){k(this,b)};Tee.doHttpRequest=function(b,a,d,e){var c=t();c&&(c.onreadystatechange=function(){4==c.readyState&&(200===c.status||0===c.status?d(b,c.responseText):e&&e(c.status,c.statusText))},c.open("GET",a,!0),c.send(null))};Tee.Slider=function(b,a){function d(a,b){return b.x>=a.x&&b.x<=a.x+a.width&&b.y>=a.y&&b.y<=a.y+a.height}Tee.Tool.call(this,b);var e=this.thumb=new Tee.Format(b);e.round={x:4,y:4};
e.stroke.size=0.5;e.gradient.visible=!0;e.gradient.direction="bottomtop";e.shadow.visible=!0;e=this.back=new Tee.Format(b);e.fill="white";e.gradient.visible=!0;e.stroke.fill="darkgrey";e.round={x:4,y:4};e=this.grip=new Tee.Format(b);e.round={x:4,y:4};e.stroke.fill="rgb(20,20,20,1.0)";this.gripSize=3;var c=this.bounds={x:10,y:10,width:200,height:20};this.transparent=!1;this.margin=16;this.min=0;this.max=100;this.position="undefined"==typeof a?50:a;this.useRange=!1;this.thumbSize=8;this.horizontal=
!0;this.cursor="pointer";this.delta=0;this.thumbRect=function(a){var b=this.max-this.min,b=0<b?(this.position-this.min)/b:0;this.horizontal?(a.width=this.thumbSize,a.x=c.x+b*c.width-0.5*a.width,a.y=c.y,a.height=c.height):(a.height=this.thumbSize,a.y=c.y+b*c.height-0.5*a.height,a.x=c.x,a.width=c.width)};var g={};this.gripRect=function(a){if(this.horizontal){var b=0.2*a.height;return{x:a.x-this.gripSize,y:a.y+0.5*a.height-b,width:2*this.gripSize,height:2*b}}b=0.2*a.width;return{x:a.x+0.5*a.width-b,
y:a.y-this.gripSize,width:2*b,height:2*this.gripSize}};this.draw=function(){var a=this.horizontal?c.height:c.width,b=a*this.margin*0.01;this.transparent||(this.horizontal?this.back.rectangle(c.x,c.y+b,c.width,a-2*b):this.back.rectangle(c.x+b,c.y,a-2*b,c.height));if(this.onDrawThumb)this.onDrawThumb(this);this.thumbRect(g);this.invertThumb?(a=this.thumb,this.horizontal?(a.rectangle(c.x,c.y+b,g.x,c.height-2*b),a.rectangle(c.x+g.x+g.width,c.y+b,c.width,c.height-2*b)):(a.rectangle(c.x+b,c.y,c.width-2*
b,g.y),a.rectangle(c.x+b,c.y+g.y+g.height,c.width-2*b,c.height))):this.thumb.rectangle(g);this.useRange&&this.horizontal&&(b=this.gripRect(g),this.grip.rectangle(b),b.x+=g.width,this.grip.rectangle(b))};this.clickAt=function(a){a=this.min+Math.max(0,(a+this.delta-(this.horizontal?c.x:c.y))*(this.max-this.min)/(this.horizontal?c.width:c.height));a>this.max&&(a=this.max);if(this.onChanging){var b=this.onChanging(this,a);"undefined"!==typeof b&&(a=b)}a<this.min?a=this.min:a>this.max&&(a=this.max);this.chart.newCursor=
this.cursor;this.position!=a&&(this.position=a,this.chart.draw())};this.resized=function(){if(this.onChanging)this.onChanging(this,this.position);this.chart.draw();this.chart.newCursor="col-resize"};this.mousemove=function(a){var b=this.horizontal?c.width:c.height,f=this.horizontal?a.x:a.y,e=this.max-this.min;this.thumbRect(g);this.resizeBegin&&f<g.x+g.width?(a=this.thumbSize,f=g.x-f,this.thumbSize+=f,this.position-=f*e/b*0.5,this.position<this.min&&(this.position=this.min,this.thumbSize=a),this.resized()):
this.resizeEnd&&f>g.x?(a=g.x+g.width-f,this.thumbSize-=a,this.position-=a*e/b*0.5,this.resized()):this.dragging?this.clickAt(f):(b=!1,this.useRange&&(e=this.gripRect(g),b=d(e,a),b||(e.x+=g.width,b=d(e,a))),b?this.chart.newCursor="col-resize":d(g,a)&&(this.chart.newCursor=this.cursor))};var f={x:0,y:0};this.mousedown=function(a){this.thumbRect(g);this.chart.calcMouse(a,f);a=this.gripRect(g);this.resizeBegin=this.useRange&&d(a,f);a.x+=g.width;this.resizeEnd=this.useRange&&!this.resizeBegin&&d(a,f);
this.dragging=!this.resizeBegin&&!this.resizeEnd&&d(g,f);this.resizeBegin||this.resizeEnd||(this.dragging?this.delta=this.horizontal?g.x+0.5*g.width-f.x:g.y+0.5*g.height-f.y:d(c,f)&&(a=this.horizontal?0.5*g.width:0.5*g.height,this.delta=-a,this.clickAt(a+(this.horizontal?f.x:f.y))));return this.dragging||this.resizeBegin||this.resizeEnd};this.clicked=function(){var a=this.dragging||this.resizeBegin||this.resizeEnd;this.resizeBegin=this.resizeEnd=this.dragging=!1;this.delta=0;return a};this.mouseout=
function(){this.resizeBegin=this.resizeEnd=this.dragging=!1}};Tee.Slider.prototype=new Tee.Tool;Tee.Scroller=function(b,a){Tee.Chart.call(this,b);this.target=a;this.aspect.clip=!1;this.panel.transparent=!0;this.title.visible=!1;var d=this.scroller=new Tee.Slider(this);d.useRange=!0;d.thumbSize=100;var e=d.thumb;e.shadow.height=0;e.transparency=0.6;e.stroke.fill="black";e.shadow.visible=!1;d.horizontal=!0;var c=d.bounds;c.x=0;c.y=0;c.width=this.bounds.width;c.height=this.bounds.height;d.margin=0;d.lock=
!1;this.tools.add(d);var g=this;a.ondraw=function(){d.lock||g.draw()};a.onscroll=function(){var a=this.axes.bottom,b=this.series,c=b.minXValue(),b=b.maxXValue(),d=a.maximum-a.minimum;a.minimum<c&&(a.minimum=c,a.maximum=a.minimum+d);a.maximum>b&&(a.maximum=b,a.minimum=a.maximum-d)};this.useRange=function(a){d.useRange=a;this.draw()};this.invertThumb=function(a){d.invertThumb=a;this.draw()};d.onChanging=function(b,c){var d=b.thumbSize*(b.max-b.min)/b.bounds.width*0.5,e=a.series,k=e.minXValue(),e=e.maxXValue();
c-d<k?c=k+d:c+d>e&&(c=e-d);a.axes.bottom.setMinMax(c-d,c+d);this.lock=!0;a.draw();this.lock=!1;if(g.onChanging)g.onChanging(g,c-d,c+d);return c};this.setBounds=function(a,b,d,e){this.bounds.x=a;this.bounds.y=b;this.bounds.width=d;this.bounds.height=e;c.x=a;c.y=b;c.width=d;c.height=e};d.onDrawThumb=function(b){function c(a,b){var d={mi:a.minimum,ma:a.maximum,sp:a.startPos,ep:a.endPos};e(a,b);return d}function e(a,b){a.minimum=b.mi;a.maximum=b.ma;a.startPos=b.sp;a.endPos=b.ep;a.scale=(b.ep-b.sp)/(b.ma-
b.mi)}var k=a.chartRect,n=a.ctx;a.chartRect=d.bounds;a.ctx=d.chart.ctx;var m=d.bounds,l=a.series,p;b.min=l.minXValue();b.max=l.maxXValue();p=c(a.axes.bottom,{sp:m.x,ep:m.x+m.width,mi:b.min,ma:b.max});var m=c(a.axes.left,{sp:m.y,ep:m.y+m.height,mi:l.minYValue(),ma:l.maxYValue()}),l=0.5*(p.mi+p.ma),q=p.ma-p.mi;if(b.position!=l){b.thumbSize=q*b.bounds.width/(b.max-b.min);q*=0.5;if(g.onChanging)g.onChanging(g,l-q,l+q);b.position=l}a.series.each(function(a){a.visible&&a.useAxes&&a.draw()});e(a.axes.bottom,
p);e(a.axes.left,m);a.chartRect=k;a.ctx=n}};Tee.Scroller.prototype=new Tee.Chart;Tee.SliderControl=function(b){b=new Tee.Chart(b);b.panel.transparent=!0;b.title.visible=!1;var a=new Tee.Slider(b);a.bounds.x=a.thumbSize+1;a.bounds.width=b.canvas.width-2*a.thumbSize-2;a.bounds.y=0.5*(b.canvas.height-a.bounds.height);b.tools.add(a);return a};Tee.CheckBox=function(b,a,d){Tee.Annotation.call(this,b);this.transparent=!0;this.text=a;this.checked=d||!0;this.margins.left=10;this.cursor="pointer";this.check=
new Tee.Format(b);this.check.fill="white";this.draw=function(){Tee.Annotation.prototype.draw.call(this);var a=this.chart.ctx,b=this.position.x+2,d=0.6*this.bounds.height,f=this.position.y+0.4*(this.bounds.height-d);this.check.rectangle(b,f,d,d);this.checked&&(a.beginPath(),a.moveTo(b+3,f+5),a.lineTo(b+4,f+8),a.lineTo(b+7,f+2),this.check.stroke.prepare(),a.stroke())};this.onclick=function(){this.checked=!this.checked;if(this.onchange)this.onchange(this);return!0}};Tee.CheckBox.prototype=new Tee.Annotation}).call(this);