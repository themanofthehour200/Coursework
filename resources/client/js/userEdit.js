function pageLoad() {

    document.getElementById("editButton").addEventListener("click", edit);
    document.getElementById("backButton").addEventListener("click", back);

    startUp();

}

function edit(event) {
    console.log("Edit function");

    event.preventDefault(); //cancels the event if it is cancelable, meaning that the default action that belongs to the event will not occur.

    const form = document.getElementById("editForm");
    const formData = new FormData(form);
    formData.set("userID",Cookies.get("UserID"));

    if (formData.get("password") !== formData.get("passDuplicate")) {
        alert("Passwords don't match");
    } else {
        fetch("/Users/edit", {method: 'post', body: formData}
        ).then(response => response.json()
        ).then(responseData => {
            if (responseData.hasOwnProperty('error')) {
                alert(responseData.error);
            } else {
                alert("User edited\nReturning to accounts page")
                window.location.href = '/client/accounts.html';
            }
        });
    }
}

function back(){window.location.href = '/client/accounts.html';}

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

    let elemNames =["firstName", "surname", "dateOfBirth", "email", "phoneNumber", "password", "passDuplicate"];
    let respNames=["Firstname", "Surname", "DateOfBirth", "Email", "PhoneNumber", "Password", "Password"];

    /*This retrieves the details of the user*/
    fetch("/Users/search/"+ userID, {method: 'get'}
    ).then(response => response.json()
    ).then(responseData => {
        if (responseData.hasOwnProperty('error')) {
            alert(responseData.error);
        } else {
            for(let i = 0; i<7; i++){
                let changeElement = document.getElementById(elemNames[i]);
                changeElement.value = responseData[respNames[i]];
            }
        }
    });
}