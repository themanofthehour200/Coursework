function pageLoad() {


    startUp();

    //Associated array of currency types to their signs
    let signs = {"EUR": "&#8364;", "GDP": "&#163;", "USD": "&#36;"};
    let rates = {"GDP":1,"EUR" : 1.18, "USD": 1.31};


    let accountsHTML = `<table>` +
        '<tr>' +
        '<th>ID</th>' +
        '<th>Account Name</th>' +
        '<th>Balance</th>' +
        '<th>Currency</th>' +
        '<th class="last">Options</th>' +
        '</tr>';

    fetch('/Accounts/viewAll/'+ Cookies.get("UserID"), {method: 'get'}
    ).then(response => response.json()
    ).then(accounts => {

        for (let account of accounts) {

            let currency = (account.Currency).toString();

            accountsHTML += `<tr>` +
                `<td>${account.AccountID}</td>` +
                `<td>${account.AccountName}</td>` +
                /*Works out the value of the balance with the correct currency*/
                `<td>`+signs[account.Currency] + `${(account.Balance /100*rates[account.Currency]).toFixed(2)} </td>` +
                `<td>${account.Currency}</td>` +
                `<td class="last">` +
                `<button class='editButton' data-id='${account.AccountID}'>Edit</button>` +
                `<button class='deleteButton' data-id='${account.AccountID}'>Delete</button>` +
                `</td>` +
                `</tr>`;
        }

        accountsHTML += '</table>';

        document.getElementById("listDiv").innerHTML = accountsHTML;

        let editButtons = document.getElementsByClassName("editButton");
        for (let button of editButtons) {
            button.addEventListener("click", editAccount);
        }

        let deleteButtons = document.getElementsByClassName("deleteButton");
        for (let button of deleteButtons) {
            button.addEventListener("click", deleteAccount);
        }

    });

    document.getElementById("saveButton").addEventListener("click", saveChanges);
    document.getElementById("cancelButton").addEventListener("click", cancelChanges);


}

function editAccount(){
    alert("edit fruit");
}

function deleteAccount(){
    alert("delete fruit");
}

function saveChanges(){
    alert("Changes to account made");
}

function cancelChanges(){
    alert("changes cancelled");
}


function logout() {
    console.log("log out");

    let formData = new FormData();
    formData.append("userID", Cookies.get("UserID"));

    fetch("/Users/logout", {method: 'post', body:formData}
    ).then(response => response.json()
    ).then(responseData => {
        if (responseData.hasOwnProperty('error')) {

            alert(responseData.error);

        } else {
            console.log("User logged out");
            Cookies.remove("username");
            Cookies.remove("token");
            window.location.href = '/client/index.html';

        }
    });
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

