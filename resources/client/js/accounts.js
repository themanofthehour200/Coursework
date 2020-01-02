//Associated array of currency types to their signs
let signs = {"EUR": "&#8364;", "GDP": "&#163;", "USD": "&#36;",
            "EUR2":"20AC", "GDP2": "00A3","USD2":"0024"};
let rates = {"GDP":1,"EUR" : 1.18, "USD": 1.31};

function pageLoad() {


    startUp();

    let accountsHTML = `<table>` +
        '<tr>' +
        '<th>ID</th>' +
        '<th>Account Name</th>' +
        '<th>Balance</th>' +
        '<th>Currency</th>' +
        '<th class="last">Options</th>' +
        '</tr>';

    fetch('/Accounts/viewAll/'+ Cookies.get("UserID"), {method: 'post'}
    ).then(response => response.json()
    ).then(accounts => {

        for (let account of accounts) {

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

function editAccount(event){

    const id = event.target.getAttribute("data-id");

    //If no id exists it means that tis function is being called by the 'create new account' button instead
    if (id === null) {

        document.getElementById("editHeading").innerHTML = 'Create New Account:';

        document.getElementById("accountID").value = '';
        document.getElementById("accountName").value = '';
        document.getElementById("balance").value = '';

        document.getElementById("listDiv").style.display = 'none';
        document.getElementById("newButton").style.display = 'none';
        document.getElementById("editDiv").style.display = 'block';

    } else {

        fetch('/Accounts/search/' + id, {method: 'get'}
        ).then(response => response.json()
        ).then(responseData => {

            if (responseData.hasOwnProperty('error')) {
                alert(responseData.error);
            } else {

                document.getElementById("editHeading").innerHTML = 'Editing ' + responseData.AccountName + ':';

                document.getElementById("accountID").value = id;
                document.getElementById("accountName").value = responseData.AccountName;
                document.getElementById("balance").value = String.fromCharCode(parseInt(signs[responseData.Currency+"2"],16)) + (responseData.Balance /100*rates[responseData.Currency]).toFixed(2);
                document.getElementById("balance").disabled = true;
                document.getElementById("currency").value = responseData.Currency;

                document.getElementById("listDiv").style.display = 'none';
                document.getElementById("newButton").style.display = 'none';
                document.getElementById("editDiv").style.display = 'block';

            }

        });

    }
}

function deleteAccount(){

    //This checks if the user has a high enough permission to delete the account


    //Creates a form and gets the users ID and the account ID
    let accountID = event.target.getAttribute("data-id");
    let formData = new FormData();
    formData.append("accountID", accountID);
    formData.append("userID", Cookies.get("UserID"));


    fetch('/Accounts/accessCheck', {method: 'post', body: formData}
    ).then(response => response.json()
    ).then(responseData => {
        console.log("This far");
        if (responseData.hasOwnProperty('error')) {
            alert(responseData.error);
        } else if (responseData.accessLevel !== 3) {
            alert("You do not have a high enough access level on this account to complete this action");
        } else {
            const ok = confirm("Are you sure?");

            if (ok === true) {

                let formData = new FormData();
                formData.append("accountID", accountID);

                fetch('/Accounts/delete', {method: 'post', body: formData}
                ).then(response => response.json()
                ).then(responseData => {
                        if (responseData.hasOwnProperty('error')) {
                            alert(responseData.error);
                        } else {
                            console.log("Account deleted");
                            pageLoad();
                        }
                    }
                );
            }
        }
    });
}


function saveChanges(){
    event.preventDefault();

    //Makes sure that all parts have been filled in
    if (document.getElementById("accountName").value.trim() === '') {
        alert("Please provide an account name.");
        return;
    }

    if (document.getElementById("balance").value.trim() === '') {
        alert("Please provide a balance.");
        return;
    }

    if (document.getElementById("currency").value.trim() === '') {
        alert("Please provide a currency.");
        return;
    }

    const id = document.getElementById("accountID").value;
    const form = document.getElementById("accountForm");
    const formData = new FormData(form);
    //formData.set("balance",document.getElementById("balance").value);
    formData.append("userID", Cookies.get("UserID"));

    //Creates account if a new account and updates the current account if it's being edited
    let apiPath = '';
    if (id === '') {
        console.log("new account");
        apiPath = '/Accounts/new';
        console.log("user id = " + formData.ghgtrujot);
        console.log("user id = " + formData.userID);
        console.log("account name = " + formData.accountName);
        console.log("balance = " + formData.balance);
        console.log("currency = " + formData.currency);
    } else {
        console.log("edit account");
        apiPath = '/Accounts/edit';
    }

    fetch(apiPath, {method: 'post', body: formData}
    ).then(response => response.json()
    ).then(responseData => {

        if (responseData.hasOwnProperty('error')) {
            alert(responseData.error);
        } else {
            document.getElementById("listDiv").style.display = 'block';
            document.getElementById("newButton").style.display = 'block';
            document.getElementById("editDiv").style.display = 'none';
            pageLoad();
        }
    });
}

function cancelChanges(){
    event.preventDefault();

    /*"""""""""""""""""""
        UNCOMMENT THE SECTION BELOW WHEN PUTTING INTO THE COURSEWORK
    """""""""""""""""""*/

/*    document.getElementById("listDiv").style.display = 'block';
    document.getElementById("newButton").style.display = 'block';
    document.getElementById("editDiv").style.display = 'none';*/
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

    //This saves the users names having to be searched up every time the user goes through the navigation links

    if(Cookies.get("Firstname")!=null && Cookies.get("Surname")!=null){
        displayName(Cookies.get("Firstname"),Cookies.get("Surname"));

    } else {
        console.log("Gone to the catch");
        /*This retrieves the details of the user*/
        fetch("/Users/search/"+ userID, {method: 'get'}
        ).then(response => response.json()
        ).then(responseData => {
            if (responseData.hasOwnProperty('error')) {
                alert(responseData.error);
            } else {
                displayName(responseData.Firstname, responseData.Surname);
                Cookies.set("Firstname", (responseData.Firstname).valueOf());
                Cookies.set("Surname", responseData.Surname.valueOf());
            }
        });
    }
}

function displayName(firstname, surname){
    let logInMessage = "You are currently logged in as ";
    logInMessage += "<em>";
    logInMessage += firstname + " ";
    logInMessage += surname;
    logInMessage += "</em>";
    document.getElementById("logInMessage").innerHTML = logInMessage;

}

