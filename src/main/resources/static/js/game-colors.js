function getNameBgColor(playerCountry, userCardName) {
    userCardName.classList.add("name-bg-" + playerCountry.toLowerCase());
}

function getFlag(playerCountry, userCardFlag) {
    switch (playerCountry) {
        case "AUS":
            userCardFlag.classList.add("fi-au");
            break;
        case "IND":
            userCardFlag.classList.add("fi-in");
            break;
        case "PAK":
            userCardFlag.classList.add("fi-pk");
            break;
        case "ENG":
            userCardFlag.classList.add("fi-gb-eng");
            break;
        case "NZL":
            userCardFlag.classList.add("fi-nz");
            break;
        case "RSA":
            userCardFlag.classList.add("fi-za");
            break;
        case "SRL":
            userCardFlag.classList.add("fi-lk");
            break;
        case "WIN":
            userCardFlag.classList.add("fi-wi");
            break;
        case "BAN":
            userCardFlag.classList.add("fi-bd");
            break;
        case "ZIM":
            userCardFlag.classList.add("fi-zw");
            break;
        case "AFG":
            userCardFlag.classList.add("fi-af");
            break;
        case "KEN":
            userCardFlag.classList.add("fi-ke");
            break;
        case "NED":
            userCardFlag.classList.add("fi-nl");
            break;
        case "IRE":
            userCardFlag.classList.add("fi-ie");
            break;
        case "SCO":
            userCardFlag.classList.add("fi-gb-sct");
            break;
        case "OMA":
            userCardFlag.classList.add("fi-om");
            break;
        case "NAM":
            userCardFlag.classList.add("fi-na");
            break;
        case "USA":
            userCardFlag.classList.add("fi-us");
            break;
        case "NEP":
            userCardFlag.classList.add("fi-np");
            break;
        case "CAN":
            userCardFlag.classList.add("fi-ca");
            break;
        case "HKG":
            userCardFlag.classList.add("fi-hk");
            break;
        case "UAE":
            userCardFlag.classList.add("fi-ae");
            break;
        case "BER":
            userCardFlag.classList.add("fi-bm");
            break;
        case "PNG":
            userCardFlag.classList.add("fi-pg");
            break;
        case "JER":
            userCardFlag.classList.add("fi-je");
            break;
    }
}

function getLogoBgColor(playerCountry, userCardLogoBg, userCardLogo) {
    userCardLogoBg.classList.add("logo-" + playerCountry.toLowerCase());
    let img = document.createElement("img");
    img.alt = "";
    img.id = "background-image-" + playerCountry.toLowerCase();
    switch (playerCountry) {
        case "AUS":
            img.src = "https://upload.wikimedia.org/wikipedia/en/3/3f/Cricket_Australia.png";
            userCardLogo.appendChild(img);
            break;
        case "IND":
            img.src = "https://upload.wikimedia.org/wikipedia/en/8/8d/Cricket_India_Crest.svg";
            userCardLogo.appendChild(img);
            break;
        case "PAK":
            img.src = "https://upload.wikimedia.org/wikipedia/en/7/7c/PakistancricketBoard-logo.svg";
            userCardLogo.appendChild(img);
            break;
        case "ENG":
            img.src = "https://upload.wikimedia.org/wikipedia/en/5/51/England_and_Wales_Cricket_Board.svg";
            userCardLogo.appendChild(img);
            break;
        case "NZL":
            img.src = "https://upload.wikimedia.org/wikipedia/en/5/55/NZCricket.png";
            userCardLogo.appendChild(img);
            break;
        case "RSA":
            img.src = "https://upload.wikimedia.org/wikipedia/en/5/5a/Cricket_South_Africa.svg";
            userCardLogo.appendChild(img);
            break;
        case "SRL":
            img.src = "https://upload.wikimedia.org/wikipedia/en/e/e4/Logo_of_Sri_Lanka_Cricket.png";
            userCardLogo.appendChild(img);
            break;
        case "WIN":
            img.src = "https://upload.wikimedia.org/wikipedia/en/9/9b/Cricket_West_Indies_Logo_2017.png";
            userCardLogo.appendChild(img);
            break;
        case "BAN":
            img.src = "https://upload.wikimedia.org/wikipedia/en/5/5c/Bangladesh_Cricket_Board_Logo.svg";
            userCardLogo.appendChild(img);
            break;
        case "ZIM":
            img.src = "https://upload.wikimedia.org/wikipedia/en/4/48/Zimbabwe_Cricket_%28logo%29.svg";
            userCardLogo.appendChild(img);
            break;
        case "AFG":
            img.src = "https://upload.wikimedia.org/wikipedia/en/4/47/Afghanistan_Cricket_Board_logo.png";
            userCardLogo.appendChild(img);
            break;
        case "KEN":
            img.src = "https://www.kenyacricket.com/assets/img/logo.png";
            userCardLogo.appendChild(img);
            break;
        case "NED":
            img.src = "https://upload.wikimedia.org/wikipedia/en/8/86/Logo_of_cricket_Netherlands.png";
            userCardLogo.appendChild(img);
            break;
        case "IRE":
            img.src = "https://upload.wikimedia.org/wikipedia/en/8/8e/Cricket_Ireland_logo.svg";
            userCardLogo.appendChild(img);
            break;
        case "SCO":
            img.src = "https://upload.wikimedia.org/wikipedia/en/9/96/CricketScotlandLogo.svg";
            userCardLogo.appendChild(img);
            break;
        case "OMA":
            img.src = "https://upload.wikimedia.org/wikipedia/en/6/63/Logo_of_Oman_Cricket.png";
            userCardLogo.appendChild(img);
            break;
        case "NAM":
            img.src = "https://upload.wikimedia.org/wikipedia/en/2/29/Logo_of_Namibia_Cricket_2021.png";
            userCardLogo.appendChild(img);
            break;
        case "USA":
            img.src = "https://upload.wikimedia.org/wikipedia/en/7/7e/USA_Cricket_logo.svg";
            userCardLogo.appendChild(img);
            break;
        case "NEP":
            img.src = "https://upload.wikimedia.org/wikipedia/en/6/6d/Cricket_Association_of_Nepal_logo.svg";
            userCardLogo.appendChild(img);
            break;
        case "CAN":
            img.src = "https://upload.wikimedia.org/wikipedia/en/7/74/CricketCanada.png";
            userCardLogo.appendChild(img);
            break;
        case "HKG":
            img.src = "https://upload.wikimedia.org/wikipedia/en/3/30/Cricket_Hong_Kong_logo.svg";
            userCardLogo.appendChild(img);
            break;
        case "UAE":
            img.src = "https://upload.wikimedia.org/wikipedia/en/c/c0/Emirates_Cricket_Board_logo.svg";
            userCardLogo.appendChild(img);
            break;
        case "BER":
            img.src = "https://upload.wikimedia.org/wikipedia/en/6/68/Bermuda_Cricket_Board_%28logo%29.png";
            userCardLogo.appendChild(img);
            break;
        case "PNG":
            img.src = "https://upload.wikimedia.org/wikipedia/en/c/c6/Cricket_PNG_logo.png";
            userCardLogo.appendChild(img);
            break;
        case "JER":
            img.src = "https://upload.wikimedia.org/wikipedia/en/9/9a/Jersey_Cricket_Board_logo.png";
            userCardLogo.appendChild(img);
            break;
    }
}