const forms = document.getElementById("user-card").querySelectorAll("form");
const aiCardLogoBg = document.getElementById("ai-card-logo-bg");
const aiCardLogo = document.getElementById("ai-card-logo");
const aiCardName = document.getElementById("ai-card-name");
const aiCardFlag = document.getElementById("ai-card-flag");
let gameOn = true;

function submitWithDelay(event) {
    event.preventDefault();
    forms.forEach(form => {
        form.lastElementChild.setAttribute("disabled", "true");
    });
    let form = event.target.closest("form");
    let statSelected = form.getAttribute("class");
    let row = form.closest("tr");
    row.classList.remove("initial-row");
    row.classList.add("blinking-row");
    setTimeout(function () {
        getAiPlayer(statSelected);
        submitForm(statSelected);
    }, 1500);
}

function getAiPlayer(statSelected) {
    const elements = document.getElementsByClassName(statSelected);
    for (const element of elements) {
        let row = element.closest("tr");
        row.classList.remove("static-row");
        row.classList.remove("blinking-row");
        row.classList.add("pressed-row");
    }
    const xhr = new XMLHttpRequest();
    xhr.open("POST", "getAiPlayer", true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onload = function () {
        if (xhr.status === 200) {
            let responseArr = JSON.parse(xhr.responseText);
            let aiPlayerCountry = responseArr["aiCountryShortName"];
            let aiPlayer = responseArr["aiPlayer"];

            getLogoBgColor(aiPlayerCountry, aiCardLogoBg, aiCardLogo);
            getNameBgColor(aiPlayerCountry, aiCardName);
            getFlag(aiPlayerCountry, aiCardFlag);
            document.getElementById("ai-card-name-text").innerText = aiPlayer["name"];
            document.getElementById("ai-card-player-image").src = aiPlayer["imageUrl"];
            document.getElementById("ai-card-country").innerText = aiPlayer["country"];
            document.getElementById("ai-card-matches").value = aiPlayer["matches"];
            document.getElementById("ai-card-runs").value = aiPlayer["runs"];
            document.getElementById("ai-card-nos").value = aiPlayer["notOuts"];
            document.getElementById("ai-card-highest").value = aiPlayer["highestScoreStar"];
            document.getElementById("ai-card-bat-avg").value = aiPlayer["batAvg"];
            document.getElementById("ai-card-bat-sr").value = aiPlayer["batStrikeRate"];
            document.getElementById("ai-card-hundreds").value = aiPlayer["hundreds"];
            document.getElementById("ai-card-fifties").value = aiPlayer["fifties"];
            document.getElementById("ai-card-overs").value = aiPlayer["overs"];
            document.getElementById("ai-card-wickets").value = aiPlayer["wickets"];
            document.getElementById("ai-card-bowl-avg").value = aiPlayer["bowlAvg"];
            document.getElementById("ai-card-bowl-er").value = aiPlayer["bowlEconRate"];
            document.getElementById("ai-card-bowl-bbi").value = aiPlayer["bowlBbi"];
            document.getElementById("ai-card-catches").value = aiPlayer["catches"];

            document.getElementById("card-back").classList.add("no-visibility");
            document.getElementById("ai-card-logo-bg").classList.remove("no-visibility");
            document.getElementById("ai-card-name").classList.remove("no-visibility");
            document.getElementById("ai-card-table").classList.remove("no-visibility");

        } else {
            console.error("Network error");
        }
    };
    xhr.onerror = function () {
        console.error("Network error");
    };
    xhr.send(JSON.stringify({
        "gameToken": gameToken,
        "statSelected": statSelected
    }));
}

function submitForm(statSelected) {
    let form = document.getElementById("user-card")
        .getElementsByClassName(statSelected).item(0).closest('form');
    setTimeout(function () {
        form.submit();
    }, 6000);
    gameOn = false;
}

function takeAiTurn() {
    let randomForm = forms[Math.floor(Math.random() * forms.length)];
    let row = randomForm.closest("tr");
    row.classList.remove("initial-row");
    row.classList.add("blinking-row");
    let statSelected = randomForm.getAttribute("class");
    setTimeout(function () {
        const elements =
            document.getElementsByClassName(statSelected);
        getAiPlayer(statSelected);
        for (const element of elements) {
            let row = element.closest('tr');
            row.classList.remove('static-row');
            row.classList.remove("blinking-row");
            row.classList.add('pressed-row');
        }
    }, 3500)
    setTimeout(function () {
        console.log(randomForm);
        randomForm.submit();
    }, 6000);

}

forms.forEach(form => {
    form.addEventListener("click", submitWithDelay);
});
