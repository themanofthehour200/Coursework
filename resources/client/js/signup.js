function pageLoad() {

    const displayNames = ["Forename", "Surname", "Date of Birth", "Email", "Phone Number (optional)", "Password", "Confirm Password"];
    const inputNames = ["firstName", "surname", "dateOfBirth", "email", "phoneNumber", "password",'passDuplicate']

    let htmlElements = "";
    for (let i = 0; i < 7; i++) {
        htmlElements += '<div class="signupDiv">' +
            '<label for=names[i]>' + displayNames[i] + ':' + '</label>' +
            '<input type="text" name='+ inputNames[i] + 'id='+ inputNames[i] + ' class="signupInput">' +
            '</div>';
    }
    let container = document.getElementById("container");
    container.innerHTML = htmlElements;

    document.getElementById("signupButton").addEventListener("click", signup);

    function signup(event){
        console.log("Sign up function");

        event.preventDefault(); //cancels the event if it is cancelable, meaning that the default action that belongs to the event will not occur.

        const form = document.getElementById("signupForm");
        const formData = new FormData(form);

        fetch("/Users/new", {method: 'post', body: formData}
        ).then(response => response.json()
        ).then(responseData => {
            if (responseData.hasOwnProperty('error')) {
                alert(responseData.error);
            } else {
                alert("User account created!\n Returning to log in page")
                window.location.href = '/client/index.html';
            }
        });
    }
}
