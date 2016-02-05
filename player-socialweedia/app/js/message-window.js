$rv.messageWindow = function (message) {
    message = encodeURIComponent(message);
    chrome.app.window.create("message-screen/index.html?message=" + message,
        {state: "fullscreen"}, function () {
        });
};
