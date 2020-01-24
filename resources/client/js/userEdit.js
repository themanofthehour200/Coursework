function pageLoad() {

    document.getElementById("editButton").addEventListener("click", edit);
    document.getElementById("backButton").addEventListener("click", back);

    startUp();

}

//This is used to edit the validate and edit the users details in the database
function edit(event) {
    event.preventDefault(); //cancels the event if it is cancelable, meaning that the default action that belongs to the event will not occur.

    const form = document.getElementById("editForm");
    const formData = new FormData(form);
    formData.set("userID", Cookies.get("UserID"));

//If the users 'Password' and 'Confirm Password' don't match
    if (formData.get("password") !== formData.get("passDuplicate")) {
        alert("Passwords don't match");
    }
    //If the user hasn't entered a date of birth
    else if(formData.get("dateOfBirth")===null || formData.get("dateOfBirth")==='') {
        alert("You haven't entered a date of birth");
    }else {
        fetch("/Users/edit", {method: 'post', body: formData}
        ).then(response => response.json()
        ).then(responseData => {
            //If there's an error then the appropriate error message is displayed.
            if (responseData.hasOwnProperty('error')) {
                switch(responseData.error) {
                    case "Invalid names":
                        alert("Invalid first name or surname entered, they cannot include numbers or special characters");
                        break;
                    case "Invalid email":
                        alert("Email entered is in an invalid format, please check it again");
                        break;
                    case "Invalid password":
                        alert("Password entered must be between 8 and 16 characters and include:\nLower case " +
                            "letters\nUpper case letters\nAt least one number\nAt least one special character");
                        break;
                    default:
                        alert(responseData.error);
                        break;
                }
                //If there's no errors then the user is edited
            } else {
                alert("User edited\nReturning to accounts page");
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