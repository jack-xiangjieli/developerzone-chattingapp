
var button = document.getElementById("button");

button.addEventListener("click",function() {
	var username = document.getElementById("username").value;
	var roomname = document.getElementById("roomname").value;
    localStorage.setItem("username", username);
    localStorage.setItem("roomname", roomname);
	window.location.assign("chatroom");
});