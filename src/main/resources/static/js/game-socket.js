const url = `http://${window.location.hostname}:${window.location.port}`;
let stompClient;

function connectToSocket(gameId) {

    let socket = new SockJS(url + "/game");
    stompClient = Stomp.over(socket);
    stompClient.heartbeat.outgoing = 10000;
    stompClient.heartbeat.incoming = 10000;
    stompClient.connect({}, function () {
        stompClient.subscribe("/topic/game-progress/" + gameId, function (response) {
            let data = response.body;
            if (gameOn) {
                getAiPlayer(data);
                submitForm(data);
            }
        })
    })
}

window.onload = function () {
    connectToSocket(gameToken);
}

const terminationEvent = 'onpagehide' in self ? 'pagehide' : 'unload';
window.addEventListener(terminationEvent, (event) => {
    if (event.persisted === false) {
        stompClient.onclose = function () { };
        stompClient.close();
    }
});