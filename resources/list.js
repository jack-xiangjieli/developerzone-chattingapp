var socket = new WebSocket("ws://" + location.host);
socket.onopen = function() {
    console.log("client sends to server: get list ");
    socket.send("get list");
}

socket.onmessage = function(e) {
    var obj = JSON.parse(e.data);
    var roomname = obj.user;
    var roomInfo = obj.message.split(" ");
    var msgNum = roomInfo[0];
    var curOnline = roomInfo[1];
    console.log("server sends back the message: " + roomname + " " + msgNum + " " + curOnline);

    var roomContainer = document.getElementById("roomContainer");

    var cardGrid = document.createElement("div");
    cardGrid.setAttribute("class", "col mb-4");

    var cardHeight = document.createElement("div");
    cardHeight.setAttribute("class", "card h-100");

    var cardBody = document.createElement("div");
    cardBody.setAttribute("class", "card-body");

    var title = document.createElement("h5");
    title.setAttribute("class", "card-title");
    title.innerHTML = roomname;

    var historyMsg = document.createElement("p");
    historyMsg.setAttribute("class", "card-text");
    historyMsg.innerHTML = "Number of History Messages: " + msgNum;

    var curOl = document.createElement("p");
    curOl.setAttribute("class", "card-text");
    curOl.innerHTML = "Currently Online: " + curOnline;

    roomContainer.appendChild(cardGrid);
    cardGrid.appendChild(cardHeight);
    cardHeight.appendChild(cardBody);
    cardBody.appendChild(title);
    cardBody.appendChild(historyMsg);
    cardBody.appendChild(curOl);


}