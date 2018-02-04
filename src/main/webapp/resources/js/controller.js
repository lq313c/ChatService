"use strict";

var Chat = {};
Chat.socket = null;

var ChatConsole = {};


Chat.connect = (function (host) {
    if ('WebSocket' in window) {
        Chat.socket = new WebSocket(host);
    } else if ('MozWebSocket' in window) {
        Chat.socket = new MozWebSocket(host);
    } else {
        ChatConsole.log('Error: WebSocket is not supported by this browser.');
        return;
    }

    Chat.socket.onopen = function () {
        ChatConsole.log('Info: WebSocket connection opened.');
        document.getElementById('chatInput').onkeydown = function (event) {
            if (event.keyCode == 13) {
                Chat.sendMessage();
            }
        };
    };

    Chat.socket.onclose = function () {
        document.getElementById('chatInput').onkeydown = null;
        ChatConsole.log('Info: WebSocket closed.');
    };

    Chat.socket.onmessage = function (message) {
        ChatConsole.log(message.data);
    };
});

Chat.initialize = function () {
    if ( window.location.host === "" ) {
        // Local testing
        Chat.connect('ws://' + 'localhost:8080' + '/ChatService/websocket/chat');
    } else {
        // Deployment endpoint
        Chat.connect('ws://' + window.location.host + '/ChatService/websocket/chat');
    }
};

Chat.sendMessage = (function () {
    var message = document.getElementById('chatInput').value;
    if (message != '') {
        Chat.socket.send(message);
        document.getElementById('chatInput').value = '';
    }
});


ChatConsole.log = (function (message) {
    var textArea = document.getElementById('textArea');
    textArea.value += message + "\n";
    textArea.scrollTop = textArea.scrollHeight;
});

Chat.initialize();