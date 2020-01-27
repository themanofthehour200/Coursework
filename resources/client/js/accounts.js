//Associated array of currency types to their signs
let signs = {
    "EUR": "&#8364;", "GDP": "&#163;", "USD": "&#36;",
    "EUR2": "20AC", "GDP2": "00A3", "USD2": "0024"
};
let rates = {"GDP": 1, "EUR": 1.18, "USD": 1.31};

//This function is called when the page is initially loaded
function pageLoad() {
    //The user is verified to have the correct token for their UserID
    verify();

    /*This sets the field names at the top of the table*/
    let accountsHTML = `<table>` +
        '<tr>' +
        '<th>ID</th>' +
        '<th>Account Name</th>' +
        '<th>Balance</th>' +
        '<th>Currency</th>' +
        /*A class of last is used here to that the text aligns to the right*/
        '<th class="last">Options</th>' +
        '</tr>';


    /*This returns the details of all the accounts that the user has any access to*/
    fetch('/Accounts/viewAll/' + Cookies.get("UserID"), {method: 'get'}
    ).then(response => response.json()
    ).then(accounts => {

        //This array stores what permission the user has on each account.
        //This will decide which edit/delete buttons for accounts need to be enabled or disabled
        let permission = [];

        //For every account returned we add another row to the table with the account's details
        //This shows how js allows us to make pages dynamic
        for (let account of accounts) {
            accountsHTML += `<tr>` +
                `<td>${account.AccountID}</td>` +
                `<td>${account.AccountName}</td>` +

                /*Works out the value of the balance with the correct currency*/
                /*The formula explained:
                * The correct currency sign + the converted account balance rounded up to two decimal places*/
                `<td>` + signs[account.Currency] + `${(Math.floor(account.Balance * rates[account.Currency]) / 100).toFixed(2)} </td>` +
                `<td>${account.Currency}</td>` +
                `<td class="last">` +
                `<button class='editButton' data-id='${account.AccountID}'>Edit</button>` +
                `<button class='deleteButton' data-id='${account.AccountID}'>Delete</button>` +
                `</td>` +
                `</tr>`;
                //Stores the accesslevel of the user on the account so edit/delete buttons should be disabled or not
            permission.push(account.AccessLevel);
        }

        accountsHTML += '</table>';

        //Sets the table in the HTML page
        document.getElementById("listDiv").innerHTML = accountsHTML;

        //Goes through the buttons and decides if they should be disabled or not
        let editButtons = document.getElementsByClassName("editButton");
        let deleteButtons = document.getElementsByClassName("deleteButton");

        for (let i = 0; i < editButtons.length; i++) {
            //If the access level corresponding to that account isn't high enough
            if(permission[i] === 1){
                //edit button is disabled so that it can't be used and will also change appearance
                editButtons[i].disabled = true;
            }
            //If the level is high enough
            else{
                //Button is enabled and listener added
                editButtons[i].disabled = false;

                //Listeners means that when the button is clicked it calls the function editAccount, making the page responsive
                editButtons[i].addEventListener("click", editAccount);
            }
        }

        for (let i = 0; i < deleteButtons.length; i++) {
            if(permission[i] === 1){
                //delete button is disabled so that it can't be used and will also change appearance
                deleteButtons[i].disabled = true;
            }else{
                //Button is enabled and listener added
                deleteButtons[i].disabled = false;
                deleteButtons[i].addEventListener("click", deleteAccount);
            }
        }

    });

    //Both the save button and cancel buttons are linked to their corresponding functions
    //So now are responsive to the user.
    document.getElementById("saveButton").addEventListener("click", saveChanges);
    document.getElementById("cancelButton").addEventListener("click", cancelChanges);
}



function editAccount(event) {

    //Verifies the user's session token
    verify();

    //Sets the accountID as a global variable so it can be accesed by other functions as well
    window.accountID = event.target.getAttribute("data-id");

    //If no id exists it means that this function is being called by the 'create new account' button instead
    if (accountID === null) {

        document.getElementById("editHeading").innerHTML = 'Create New Account:';
        //No managers can be added when the account is being created
        document.getElementById("editManagerPortal").style.display = 'none';

        document.getElementById("accountID").value = '';
        document.getElementById("accountName").value = '';
        document.getElementById("balance").value = '';
        document.getElementById("balance").disabled = false;

        //sets up the account to be edited
        document.getElementById("listDiv").style.display = 'none';
        document.getElementById("newButton").style.display = 'none';
        document.getElementById("editDiv").style.display = 'block';

    } else {
        //So that managers can be added or removed
        document.getElementById("editManagerPortal").style.display = 'block';

        //Gets the details of the account
        fetch('/Accounts/search/' + accountID, {method: 'get'}
        ).then(response => response.json()
        ).then(responseData => {

            if (responseData.hasOwnProperty('error')) {
                alert(responseData.error);
            } else {

                document.getElementById("editHeading").innerHTML = 'Editing ' + responseData.AccountName + ':';

                //fills out the input boxes with the existing data, so that the user can see what they need to edit.
                document.getElementById("accountID").value = accountID;
                document.getElementById("accountName").value = responseData.AccountName;
                //Gets the correctly formatted currency sign and then displays the currectly converted balance in said currency
                document.getElementById("balance").value = String.fromCharCode(parseInt(signs[responseData.Currency + "2"], 16)) +
                    (Math.floor(responseData.Balance * rates[responseData.Currency]) / 100).toFixed(2);

                //Is disabled so that the user can't directly change their acount balance, that should only be done via transactions
                document.getElementById("balance").disabled = true;
                document.getElementById("currency").value = responseData.Currency;
                document.getElementById("originalCurrency").value = responseData.Currency;
            }

        });
        //Displays what managers there are of the account
        formatManagers();
    }

}

//This function displays all of the managers of an account
function formatManagers(){

    //Sets the headings of the table
    let managerHTML = `<table>` +
        '<tr>' +
        '<th>User ID</th>' +
        '<th>Name</th>' +
        '<th>Email</th>' +
        '<th>Access Level</th>' +
        '</tr>';

    //Gets all of the data for the managers of the account
    fetch('/AccountManagers/list/' + accountID, {method: 'get'}
    ).then(response => response.json()
    ).then(managers => {

        //Dynamically add a row with the details for every manager of the account
        for (let manager of managers) {

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

        //sets the table in the HTML page
        document.getElementById("managerList").innerHTML = managerHTML;



        //Creates a form and adds the users ID and the account ID
        let formData = new FormData();
        formData.append("accountID", accountID);
        formData.append("userID", Cookies.get("UserID"));


        //Checks what access level the user has on the account
        fetch('/Accounts/accessCheck', {method: 'post', body: formData}
        ).then(response => response.json()
        ).then(responseData => {
            if (responseData.hasOwnProperty('error')) {
                alert(responseData.error);
            } else {
                let accessLevel = responseData.AccessLevel;
                let deleteButtons = document.getElementsByClassName("deleteManagerButton");

                //If the user has the correct permissions
                if (accessLevel === 3) {
                    //enable all of the delete buttons make them call deleteManager when clicked
                    for (let button of deleteButtons) {
                        button.addEventListener("click", deleteManager);
                        button.disabled = false;
                        button.title = "Delete a manager";
                    }

                    //Sets the data-id of the addManager button and makes it call addManager() when clicked
                    document.getElementById("addManager").setAttribute('data-id',accountID);
                    document.getElementById("addManager").disabled = false;
                    document.getElementById("addManager").addEventListener("click",addManager);
                }
                //If the user doesn't have the correct permisson
                else {

                    //disable all of the delete buttons
                    for (let button of deleteButtons) {
                        button.disabled = true;
                    }

                    //disable the addManager button
                    document.getElementById("addManager").disabled = true;
                }
            }
        });

    });

    //Display the edit section and hide the list section
    document.getElementById("listDiv").style.display = 'none';
    document.getElementById("newButton").style.display = 'none';
    document.getElementById("editDiv").style.display = 'block';
}

//This function is used to add a new user as a manager to an account
function addManager(event) {
    //Verifies the user's session token
    verify();

    event.preventDefault();

    //Retrieves the accountID in question
    const accountID = event.target.getAttribute("data-id");


    //Gets the email and access level that the user has input
    const form = document.getElementById("managerForm");
    const formData = new FormData(form);


    //If the user hasn't entered an email then an alert is given
    if(formData.get("email")===null || formData.get("email") ===''){
        alert("Please enter an email")
    }else {

        //This validates that the user being added as a manager exists within the database
        fetch('/Users/emailSearch',{body:formData ,method: 'post'}
        ).then(response => response.json()
        ).then(responseData => {
            //If the email doesn't correspond to an email then give an error
            if (responseData.hasOwnProperty('error')) {
                alert("The entered user couldn't be found,\nis that definitely the correct email address for the user?");

                //If the user has been found then they can be added as a manager on the account
            } else {

                formData.delete("email");
                formData.append("managerID",responseData.UserID);
                formData.append("accountID",accountID);

                fetch('/AccountManagers/new', {method: 'post', body: formData}
                ).then(response => response.json()
                ).then(managerData => {
                    if (managerData.hasOwnProperty('error')) {
                        //If the error can be identified then it is made into a user-friendly alert
                        if(managerData.error === "User is already a manager") alert("User is already a manager for this account");
                        else alert(managerData.error);
                    } else {
                        //lets the user know that a manager has been added
                        alert("Manager added");

                        //re-formats all of the managers to reflect the addition
                        formatManagers();
                    }
                });
            }
        });

        //clears the input boxes for the next addition
        document.getElementById("newEmail").value='';
        document.getElementById("accessLevel").value=1;
    }
}

//This function is used to delete a manager from an account
function deleteManager(event) {
    //Verifies the user's session token
    verify();


    //Creates a form and gets the users ID and the account ID
    let controlID = event.target.getAttribute("data-id");
    let formData = new FormData();
    formData.append("controlID", controlID);

    //Useability: User is made to double check that they definitely want to delete the manager
    const ok = confirm("Are you sure?");

    if (ok === true) {

        //Manager is removed
        fetch('/AccountManagers/delete', {method: 'post', body: formData}
        ).then(response => response.json()
        ).then(responseData => {
                if (responseData.hasOwnProperty('error')) {
                    alert(responseData.error);
                } else {
                    //Lets the user know that the manager has been deleted
                    alert("Manager deleted");
                    //Reloads the manager section of the edit page
                    formatManagers(accountID);
                }
            }
        );
    }
}

//This function is used to delete an account
function deleteAccount(event) {
    //Verifies the user's session token
    verify();

    //Creates a form and gets the users ID and the account ID
    let accountID = event.target.getAttribute("data-id");

    //Useability: User is made to double check that they definitely want to delete the account
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
                    //Lets the user know that the manager has been deleted
                    alert("Account deleted");
                    //Reloads the list of accounts
                    pageLoad();
                }
            }
        );
    }
}

/*This function is used to make the changes to the database
 of either editing or creating the account, based on the
 data that the user has input*/
function saveChanges(event) {
    //Verifies the user's session token
    verify();
    event.preventDefault();

    document.getElementById("balance").disabled = false;

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
        apiPath = '/Accounts/new';
        //Formats balance to be the numerical converted value of the input balance in pence
        formData.set("balance", Math.ceil((formData.get("balance") * 100) / rates[formData.get("currency")]));

    } else {
        //Formats balance to be the numerical converted value of the input balance in pence
        //Also removes the currency symbol, which the user may well have added in as on edit it is put in the input box
        formData.set("balance", Math.ceil((formData.get("balance").replace(/[^\d.-]/g, '')) / rates[formData.get("originalCurrency")] * 100));
        apiPath = '/Accounts/edit';
    }

    //Creates/edits the account based on the apiPath chosen
    fetch(apiPath, {method: 'post', body: formData}
    ).then(response => response.json()
    ).then(responseData => {

        if (responseData.hasOwnProperty('error')) {
            alert(responseData.error);
        } else {
            //Goes back to the list section of the page and reloads the list
            document.getElementById("listDiv").style.display = 'block';
            document.getElementById("newButton").style.display = 'inline';
            document.getElementById("editDiv").style.display = 'none';
            pageLoad();
        }
    });

}


function cancelChanges(event) {
    //Verifies the user's session token
    verify();

    //Stops the function being called by default
    event.preventDefault();

    //returns the user to the list section on the page, no changes made to database
    document.getElementById("listDiv").style.display = 'block';
    document.getElementById("newButton").style.display = 'inline';
    document.getElementById("editDiv").style.display = 'none';
}

//This function returns the user to the welcome page and resets the user's token
function logout() {

    let formData = new FormData();
    formData.append("userID", Cookies.get("UserID"));

    //Restes the toke in the database
    fetch("/Users/logout", {method: 'post', body: formData}
    ).then(response => response.json()
    ).then(responseData => {
        if (responseData.hasOwnProperty('error')) {

            alert(responseData.error);

        } else {
            //Removs the current cookies, though they will be invalid now anyway
            Cookies.remove("UserID");
            Cookies.remove("Token");
            Cookies.remove("FirstName");
            Cookies.remove("Surname");
            //returns the user to the log-on page
            window.location.href = '/client/index.html';

        }
    });
}

//This function verifies that the user has the correct UserID and Token
function verify() {

    //gets what the user has set to their userId and token
    let userID = Cookies.get("UserID");
    let token = Cookies.get("Token");

    let formData = new FormData();
    formData.append("userID", userID);
    formData.append("token", token);


    /*This validates that the client's token corresponds to a logged in user*/
    fetch("/Users/validate", {method: 'post', body: formData}
    ).then(response => response.json()
    ).then(responseData => {
        /*If the user can't be validated they should be returned to the welcome page, either due to an
        * error or a malicious user*/
        if (responseData.hasOwnProperty('error')) {
            alert(responseData.error);
            window.location.href = '/client/index.html';
        } else if (!responseData.found) {
            window.location.href = '/client/index.html';
        }
    });


    //This displays the user's name at the top of the page to indicate they're logged in
    let logInMessage = "You are currently logged in as ";
    logInMessage += "<em>";
    logInMessage += Cookies.get("FirstName").valueOf() + " ";
    logInMessage += Cookies.get("Surname").valueOf();
    logInMessage += "</em>";
    document.getElementById("logInMessage").innerHTML = logInMessage;
}




