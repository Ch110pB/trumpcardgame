const url = `http://${window.location.hostname}:${window.location.port}`;
const form = document.getElementById("connectForm");
let gameOn = true;
let stompClient;

function connectToSocket(gameId) {

    let socket = new SockJS(url + "/game");
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function () {
        stompClient.subscribe("/topic/game-join/" + gameId, function () {
            if (gameOn) {
                connectToGameSession();
            }
        })
    })
}

function connectToGameSession() {
    form.submit();
    gameOn = false;
}

window.onload = function () {
    connectToSocket(gameToken);
}
