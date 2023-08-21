const forms = document.getElementById("user-card").querySelectorAll("form");
const aiCardLogoBg = document.getElementById("ai-card-logo-bg");
const aiCardLogo = document.getElementById("ai-card-logo");
const aiCardName = document.getElementById("ai-card-name");
const aiCardFlag = document.getElementById("ai-card-flag");
const button = document.getElementById("clash-button");
const imageContainer = document.getElementById("sprite-container");
const elements = document.getElementsByName("isUserClash");
let imageVisible = false;

function getAiPlayer(form, statSelected) {
    const xhr = new XMLHttpRequest();
    xhr.open("POST", "getAiPlayer", true);
    xhr.setRequestHeader("Content-Type", "application/json");
    setTimeout(function () {
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
                const elements = document.getElementsByClassName(statSelected);
                for (const element of elements) {
                    let row = element.closest('tr');
                    row.classList.remove('static-row');
                    row.classList.remove("blinking-row");
                    row.classList.add('pressed-row');
                }
                setTimeout(function () {
                    form.submit();
                }, 5000);
            } else {
                console.error("Network error");
            }
        };
        xhr.onerror = function () {
            console.error("Network error");
        };
        xhr.send();
    }, 3500);
}

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
    getAiPlayer(form, statSelected);
}

function takeAiTurn() {
    let randomForm = forms[Math.floor(Math.random() * forms.length)];
    let aiClash = (Math.random() * 100) > 65 ? 1 : 0;
    const elements = document.getElementsByName("isAiClash");
    for (const element of elements) {
        element.value = aiClash;
    }
    let row = randomForm.closest("tr");
    let statSelected = randomForm.getAttribute("class");
    setTimeout(function () {
        row.classList.remove("initial-row");
        row.classList.add("blinking-row");
    }, 2500);
    getAiPlayer(randomForm, statSelected);
}

forms.forEach(form => {
    form.addEventListener("click", submitWithDelay);
});

button.addEventListener('click', () => {
    imageVisible = !imageVisible;
    imageContainer.style.visibility = imageVisible ? 'visible' : 'hidden';
    button.querySelector("#clash").textContent = imageVisible ? 'HUSH 💤' : 'CLASH 👊';
    button.querySelector("#clash").style.filter = imageVisible ? 'grayscale(100%)' : 'none';
    button.style.color = imageVisible ? '#7d7d83' : 'ghostwhite';
    button.style.borderStyle = imageVisible ? 'inset' : 'outset';
    button.style.borderColor = imageVisible ? '#e9edf6' : '#bc1b1b';
    button.style.backgroundColor = imageVisible ? 'ghostwhite' : '#e12c2c';
    for (const element of elements) {
        element.value = imageVisible ? '1' : '0';
    }
});
