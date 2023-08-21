const connectBtn = document.getElementById("connect");
const tokenText = document.getElementById("token");
const connectErrorText = document.getElementById("connectError");

connectBtn.addEventListener("click", function(event){
    event.preventDefault();
    let form = event.target.closest("form");
    const token = tokenText.value;
    const xhr = new XMLHttpRequest();
    xhr.open("POST", "isTokenValid", true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onload = function () {
        if (xhr.status === 200) {
            if (xhr.responseText === "INVALID") {
                connectErrorText.textContent = "Invalid session code: Please try again!";
                connectErrorText.style.color = "#df1f1f";
            } else {
                form.submit();
            }
        } else {
            console.error("Network error");
        }
    };
    xhr.onerror = function () {
        console.error("Network error");
    };
    xhr.send(JSON.stringify({ token: token }));
    })