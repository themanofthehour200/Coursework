function pageLoad() {

    if (window.location.search === '?logout') {
        document.getElementById('content').innerHTML = '<h1>Logging out, please wait...</h1>';
        logout();
    } else {
        document.getElementById("loginButton").addEventListener("click", login);
    }

    function login(event) {
        //debugger;
        console.log("login function");

        event.preventDefault(); //cancels the event if it is cancelable, meaning that the default action that belongs to the event will not occur.

        const form = document.getElementById("loginForm");
        const formData = new FormData(form);

        fetch("/Users/login", {method: 'post', body: formData}
        ).then(response => response.json()
        ).then(responseData => {
            if (responseData.hasOwnProperty('error')) {
                alert(responseData.error);
            } else {
                Cookies.set("UserID", responseData.UserID);
                Cookies.set("Token", responseData.Token);
                window.location.href = '/client/index.html';
            }
        });
    }

    function logout() {

        fetch("/user/logout", {method: 'post'}
        ).then(response => response.json()
        ).then(responseData => {
            if (responseData.hasOwnProperty('error')) {

                alert(responseData.error);

            } else {

                Cookies.remove("username");
                Cookies.remove("token");

                window.location.href = '/client/index.html';

            }
        });
    }
}

