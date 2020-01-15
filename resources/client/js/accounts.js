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
                `<td>`+signs[account.Currency] + `${(Math.floor(account.Balance * rates[account.Currency])/100).toFixed(2)} </td>` +
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
        document.getElementById("editManagerPortal").style.display = 'none';

        document.getElementById("accountID").value = '';
        document.getElementById("accountName").value = '';
        document.getElementById("balance").value = '';
        document.getElementById("balance").disabled = false;

    } else {
        document.getElementById("editManagerPortal").style.display = 'block';

        fetch('/Accounts/search/' + id, {method: 'get'}
        ).then(response => response.json()
        ).then(responseData => {

            if (responseData.hasOwnProperty('error')) {
                alert(responseData.error);
            } else {

                document.getElementById("editHeading").innerHTML = 'Editing ' + responseData.AccountName + ':';

                document.getElementById("accountID").value = id;
                document.getElementById("accountName").value = responseData.AccountName;
                document.getElementById("balance").value = String.fromCharCode(parseInt(signs[responseData.Currency+"2"],16)) +
                    (Math.floor(responseData.Balance * rates[responseData.Currency])/100).toFixed(2);
                document.getElementById("balance").disabled = true;
                document.getElementById("currency").value = responseData.Currency;
                document.getElementById("originalCurrency").value = responseData.Currency;


            }

        });
    }
    //Formatting all managers relating to the account
    let managerHTML = `<table>` +
        '<tr>' +
        '<th>User ID</th>' +
        '<th>Name</th>' +
        '<th>Email</th>' +
        '<th>Access Level</th>' +
        '</tr>';

    fetch('/AccountManagers/list/'+ Cookies.get("AccountID"), {method: 'get'}
    ).then(response => response.json()
    ).then(managers => {

        for (let manager of managers) {

/*            console.log("userid: " + manager.UserID);
            console.log("firstname: " + manager.FirstName);
            console.log("surname: " + manager.Surname);
            console.log("email: " + manager.Email);
            console.log("uaccess levelserid: " + manager.AccessLevel);*/

            managerHTML += `<tr>` +
                `<td>${manager.UserID}</td>` +
                `<td>${manager.FirstName + " " + manager.Surname}</td>` +
                `<td>${manager.Email}</td>` +
                `<td>${manager.AccessLevel}</tdclass>` +
                `<td class="last">` +
                `<button class='deleteManagerButton' data-id='${manager.ControlID}'>Delete</button>` +
                `</td>` +
                `</tr>`;
        }

        managerHTML += '</table>';

        document.getElementById("managerList").innerHTML = managerHTML;

        //checks if the user has permission to create/delete/edit transactions


        //Creates a form and gets the users ID and the account ID
        let formData = new FormData();
        formData.append("accountID", Cookies.get("AccountID"));
        formData.append("userID", Cookies.get("UserID"));


        fetch('/Accounts/accessCheck', {method: 'post', body: formData}
        ).then(response => response.json()
        ).then(responseData => {
            if (responseData.hasOwnProperty('error')) {
                alert(responseData.error);
            } else{
                let accessLevel = responseData.AccessLevel;
                let deleteButtons = document.getElementsByClassName("deleteManagerButton");

                if(accessLevel === 3){
                    for (let button of deleteButtons) {
                        button.addEventListener("click", deleteManager);
                        button.disabled = false;
                    }

                    document.getElementById("addManager").disabled = false;
                }else{

                    for (let button of deleteButtons) {
                        button.disabled = true;
                    }

                    document.getElementById("addManager").disabled = true;
                }
            }
        });

    });

    /*document.getElementById("saveButton").addEventListener("click", saveChanges);
    document.getElementById("cancelButton").addEventListener("click", cancelChanges);*/


    document.getElementById("listDiv").style.display = 'none';
    document.getElementById("newButton").style.display = 'none';
    document.getElementById("editDiv").style.display = 'block';
}

function deleteManager(){


    //Creates a form and gets the users ID and the account ID
    let controlID = event.target.getAttribute("data-id");
    let formData = new FormData();
    formData.append("controlID", controlID);

    for(let pair of formData.entries()) {
        console.log(pair[0]+ ', '+ pair[1]);
    }


    const ok = confirm("Are you sure?");

    if (ok === true) {

        fetch('/AccountManagers/delete', {method: 'post', body: formData}
        ).then(response => response.json()
        ).then(responseData => {
                if (responseData.hasOwnProperty('error')) {
                    alert(responseData.error);
                } else {
                    console.log("Manager deleted");
                    editAccount();
                }
            }
        );
    }
}

function deleteAccount(event){


    //Creates a form and gets the users ID and the account ID
    let accountID = event.target.getAttribute("data-id");
    let formData = new FormData();
    formData.append("accountID", accountID);
    formData.append("userID", Cookies.get("UserID"));


    fetch('/Accounts/accessCheck', {method: 'post', body: formData}
    ).then(response => response.json()
    ).then(responseData => {
        if (responseData.hasOwnProperty('error')) {
            alert(responseData.error);
        } else if (responseData.AccessLevel !== 3) {
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


function saveChanges() {
    event.preventDefault();

    document.getElementById("balance").disabled=false;

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
        console.log(Math.floor((formData.get("balance")*100)/rates[formData.get("currency")]));
        // noinspection JSCheckFunctionSignatures
        formData.set("balance",Math.ceil((formData.get("balance")*100)/rates[formData.get("currency")]));

    } else {
        console.log("edit account");
        console.log(formData.get("currency"));
        console.log(formData.get("balance").replace(/[^\d.-]/g, '')/*/rates[formData.get("currency"));*/);
        formData.set("balance",Math.ceil((formData.get("balance").replace(/[^\d.-]/g, ''))/rates[formData.get("originalCurrency")]*100));
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


function showVals(value){
    console.log(value);
}

function cancelChanges(){
    event.preventDefault();

    pageLoad();

    /*"""""""""""""""""""
        UNCOMMENT THE SECTION BELOW WHEN PUTTING INTO THE COURSEWORK
    """""""""""""""""""*/

    document.getElementById("listDiv").style.display = 'block';
    document.getElementById("newButton").style.display = 'block';
    document.getElementById("editDiv").style.display = 'none';
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
            Cookies.remove("UserID");
            Cookies.remove("Token");
            Cookies.remove("FirstName");
            Cookies.remove("Surname");
            window.location.href = '/client/index.html';

        }
    });
}

function startUp(){
    let userID = Cookies.get("UserID");
    let token = Cookies.get("Token");

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
        }
    });

    //This saves the users names having to be searched up every time the user goes through the navigation links


    displayName(Cookies.get("FirstName"),Cookies.get("Surname"));
}

function displayName(firstname, surname){
    let logInMessage = "You are currently logged in as ";
    logInMessage += "<em>";
    logInMessage += firstname + " ";
    logInMessage += surname;
    logInMessage += "</em>";
    document.getElementById("logInMessage").innerHTML = logInMessage;

}


