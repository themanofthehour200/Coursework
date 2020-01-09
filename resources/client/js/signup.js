function pageLoad() {

    document.getElementById("signupButton").addEventListener("click", signup);

}

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
