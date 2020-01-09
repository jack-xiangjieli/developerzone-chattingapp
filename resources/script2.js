
var sendButton = document.getElementById("sendButton");

roomname = localStorage.getItem("roomname");
username = localStorage.getItem("username");

var socket = new WebSocket("ws://" + location.host);
socket.onopen = function() {
	console.log("client sends to server: join " + roomname);
	socket.send("join" + " " + roomname);
}

sendButton.onclick = function() {
	var messageForm = document.getElementById("message");
	var message = messageForm.value;
	console.log("client sends to server: " + username + " " + message);
	socket.send(username + " " + message);
	messageForm.value = "";
};

	
socket.onmessage = function(e) {
	var messageContainer = document.getElementById("messageContainer");

	var message = document.createElement("p");
    message.setAttribute("id", "Message");

    var header = document.createElement("h5");
    header.setAttribute("class", "mt-0");

    var mediaBody = document.createElement("div");
    mediaBody.setAttribute("class", "media-body");

    var image = document.createElement("img");
    image.setAttribute("class", "mr-3");
    image.setAttribute("src", "https://cdn.dribbble.com/users/239755/screenshots/2266928/bender-icon-dribbble.png");
    image.setAttribute("width", "50");
    image.setAttribute("height", "50");

	var media = document.createElement("div");
	media.setAttribute("class", "media");

	var obj = JSON.parse(e.data);
	console.log("server sends back the message: " + obj.user + " " + obj.message);
	header.innerHTML = obj.user;
	message.innerHTML = obj.message;

	messageContainer.appendChild(media);
	media.appendChild(image);
	media.appendChild(mediaBody);
	mediaBody.appendChild(header);
	mediaBody.appendChild(message);
}


