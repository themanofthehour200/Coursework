//Associated array of currency types to their signs
let signs = {"EUR": "&#8364;", "GDP": "&#163;", "USD": "&#36;",
    "EUR2":"20AC", "GDP2": "00A3","USD2":"0024"};
let rates = {"GDP":1,"EUR" : 1.18, "USD": 1.31};

function pageLoad() {

    startUp();

    //Choosing which account's transactions to look at
    let select = document.getElementById( 'accountChoose' );

    fetch('/Accounts/viewAll/'+ Cookies.get("UserID"), {method: 'post'}
    ).then(response => response.json()
    ).then(accounts => {
        for (let account of accounts) {
            let option = document.createElement( 'option' );
            option.value = account.AccountID;
            option.text = account.AccountName;
            select.add(option);
        }}
    );


    document.getElementById("reloadButton").addEventListener("click", changeUser);
    document.getElementById("newButton").addEventListener("click", changeUser);

}

function editTransaction(event){

    //The select list of what categories the user can access
    let target = document.getElementById( 'category' );

    //Clears any categories already loaded
    let i;
    for(i = target.options.length - 1 ; i >= 0 ; i--)
    {
        target.remove(i);
    }

    fetch('/Categories/list/'+ Cookies.get("UserID"), {method: 'get'}
    ).then(response => response.json()
    ).then(categories => {
        for (let category of categories) {
            let option = document.createElement( 'option' );
            option.value = category.CategoryID;
            option.text = category.CategoryName;
            target.add(option);
        }}
    );

    const id = event.target.getAttribute("data-id");

    //If no id exists it means that this function is being called by the 'create new transaction' button instead
    if (id === null) {

        document.getElementById("editHeading").innerHTML = 'Create New Transaction:';

        document.getElementById("transactionID").value = '';
        document.getElementById("balanceChange").value = '';
        document.getElementById("description").value = '';
        document.getElementById("date").value = '';


    } else {

        fetch('/Transactions/search/' + id, {method: 'get'}
        ).then(response => response.json()
        ).then(responseData => {

            if (responseData.hasOwnProperty('error')) {
                alert(responseData.error);
            } else {

                document.getElementById("editHeading").innerHTML = 'Editing transaction number ' + responseData.TransactionID + ':';

                document.getElementById("transactionID").value = id;
                document.getElementById("balanceChange").value = String.fromCharCode(parseInt(signs["GDP2"],16)) + (responseData.BalanceChange /100).toFixed(2);
                document.getElementById("category").value = responseData.CategoryID;
                document.getElementById("description").value = responseData.Description;
                document.getElementById("date").value = responseData["Date"];
            }

        });

    }
    document.getElementById("listDiv").style.display = 'none';
    document.getElementById("newButton").style.display = 'none';
    document.getElementById("editDiv").style.display = 'block';
}

function accountCheck(){

    //Checks a user's permission level


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
            Cookies.set("AccessLevel",responseData.AccessLevel);
            console.log("response data " + responseData.AccessLevel);
        }
    });

}


function saveChanges(event) {
    event.preventDefault();

    //Makes sure that all parts have been filled in
    if (document.getElementById("balanceChange").value.trim() === '') {
        alert("Please provide a transaction amount.");
        return;
    }

    if (document.getElementById("date").value.trim() === '') {
        alert("Please provide a date.");
        return;
    }

    const id = document.getElementById("transactionID").value;
    const form = document.getElementById("transactionForm");
    const formData = new FormData(form);

    formData.append("standingOrderID", "0");

    //Creates account if a new transaction or updates the current transaction if it's being edited
    let apiPath = '';
    if (id === '') {
        console.log("new transaction");
        apiPath = '/Transactions/create';

        formData.set("balanceChange",formData.get("balanceChange")*100);

    } else {
        console.log("edit transaction");
        formData.set("balanceChange",(formData.get("balanceChange")).replace(/[^\d.-]/g, '')*100);
        apiPath = '/Transactions/edit';
    }

    formData.append("accountID",Cookies.get("AccountID"));
    formData.set("categoryID",document.getElementById("category").value);

    console.log("category ID is "+formData.get("categoryID"));

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
            changeUser();
        }
    });

}

function cancelChanges(event){
    event.preventDefault();

    pageLoad();
    changeUser();

    document.getElementById("listDiv").style.display = 'block';
    document.getElementById("newButton").style.display = 'block';
    document.getElementById("editDiv").style.display = 'none';
}

function deleteTransaction(event){
    event.preventDefault();

    const ok = confirm("Are you sure that you would like to delete this transaction?");

    if (ok === true) {

        let transactionID = event.target.getAttribute("data-id");
        let formData = new FormData();
        formData.append("transactionID", transactionID);

        fetch('/Transactions/delete', {method: 'post', body: formData}
        ).then(response => response.json()
        ).then(responseData => {
                if (responseData.hasOwnProperty('error')) {
                    alert(responseData.error);
                } else {
                    console.log("Transaction deleted");
                    pageLoad();
                }
            }
        );
        pageLoad();
        changeUser();
    }
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

function changeUser(event) {

    if(event !==undefined){
        event.preventDefault();
    }

    //Displaying account balance at top

    let id = document.getElementById("accountChoose").value;
    Cookies.set("AccountID",id);

    fetch('/Accounts/search/' + id, {method: 'get'}
    ).then(response => response.json()
    ).then(responseData => {

        if (responseData.hasOwnProperty('error')) {
            alert(responseData.error);
        } else {
            let accountAmount= String.fromCharCode(parseInt(signs[responseData.Currency+"2"],16)) + `${Math.floor((responseData.Balance * rates[responseData.Currency] /100)).toFixed(2)}`;
            document.getElementById('chosenAccountBalance').innerText = "  Current Account Balance: "+ accountAmount;

        }
    });


    //Formatting all transactions relating to the account
    let transactionHTML = `<table>` +
        '<tr>' +
        '<th>ID</th>' +
        '<th>Amount</th>' +
        '<th>Category</th>' +
        '<th>Description</th>' +
        '<th>Date</th>' +
        '<th class="last">Options</th>' +
        '</tr>';

    fetch('/Transactions/view/'+ Cookies.get("AccountID"), {method: 'get'}
    ).then(response => response.json()
    ).then(transactions => {

        for (let transaction of transactions) {
            //Shows is transaction is money leaving or entering account
            let posOrNeg = "";
            if (transaction.BalanceChange < 0){
                posOrNeg = "+";
            }else{
                posOrNeg = "-";
            }
            transactionHTML += `<tr>` +
                `<td>${transaction.TransactionID}</td>` +
                `<td>`+posOrNeg+signs["GDP"] + `${Math.abs((transaction.BalanceChange /100).toFixed(2))} </td>` +
                `<td>${transaction.CategoryName}</td>` +
                `<td>${transaction.Description}</td>` +
                `<td>${transaction.Date}</td>` +
                `<td class="last">` +
                `<button class='editButton' data-id='${transaction.TransactionID}'>Edit</button>` +
                `<button class='deleteButton' data-id='${transaction.TransactionID}'>Delete</button>` +
                `</td>` +
                `</tr>`;
        }

        transactionHTML += '</table>';

        document.getElementById("listDiv").innerHTML = transactionHTML;

        //checks if the user has permission to create/delete/edit transactions

        accountCheck();
        let accessLevel = Cookies.get("AccessLevel");
        Cookies.remove("AccessLevel");
        console.log("access " + accessLevel);

        let editButtons = document.getElementsByClassName("editButton");
        let deleteButtons = document.getElementsByClassName("deleteButton");

        if(accessLevel >= 2){
            for (let button of editButtons) {
                button.addEventListener("click", editTransaction);
            }

            for (let button of deleteButtons) {
                button.addEventListener("click", deleteTransaction);
            }

            document.getElementById("newButton").disabled = false;
        }else{
            for (let button of editButtons) {
                button.disabled = true;
            }
            for (let button of deleteButtons) {
                button.disabled = true;
            }

            document.getElementById("newButton").disabled = true;
        }


    });

    document.getElementById("saveButton").addEventListener("click", saveChanges);
    document.getElementById("cancelButton").addEventListener("click", cancelChanges);
}

