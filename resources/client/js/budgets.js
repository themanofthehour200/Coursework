function pageLoad() {
    startUp();

}

function startUp(){
    let userID = Cookies.get("UserID");
    let token = Cookies.get("Token");

    console.log("User retrieved");

    let formData = new FormData();
    formData.append("userID", userID);
    formData.append("token", token);


    /*This validates that the client's token corresponds to a logged in user*/
    fetch("/Users/validate", {method: 'post', body: formData}
    ).then(response => response.json()
    ).then(responseData => {
        if (responseData.hasOwnProperty('error')) {
            alert(responseData.error);
            window.location.href = '/client/index.html';
        } else if (!responseData.found) {
            console.log("Invalid log on details");
            window.location.href = '/client/index.html';
        } else {
            console.log("User validated")
        }
    });

    /*This retrieves the details of the user*/
    fetch("/Users/search/"+ userID, {method: 'get'}
    ).then(response => response.json()
    ).then(responseData => {
        if (responseData.hasOwnProperty('error')) {
            alert(responseData.error);
        } else {
            let logInMessage = "You are currently logged in as ";
            logInMessage += "<em>";
            logInMessage += responseData.Firstname + " ";
            logInMessage += responseData.Surname;
            logInMessage += "</em>";
            document.getElementById("logInMessage").innerHTML = logInMessage;
        }
    });
}


function logout() {
    console.log("log out");

    let formData = new FormData();
    formData.append("userID", Cookies.get("UserID"));

    fetch("/Users/logout", {method: 'post', body: formData}
    ).then(response => response.json()
    ).then(responseData => {
        if (responseData.hasOwnProperty('error')) {

            alert(responseData.error);

        } else {
            console.log("User logged out");
            Cookies.remove("UserID");
            Cookies.remove("Token");
            Cookies.remove("FirstName");
            Cookies.remove("Surname");
            window.location.href = '/client/index.html';

        }
    });
}

