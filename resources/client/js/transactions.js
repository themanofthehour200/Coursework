//Associated array of currency types to their signs
let signs = {"EUR": "&#8364;", "GDP": "&#163;", "USD": "&#36;",
    "EUR2":"20AC", "GDP2": "00A3","USD2":"0024"};
let rates = {"GDP":1,"EUR" : 1.18, "USD": 1.31};

function pageLoad() {

    verify();

    //Choosing which account's transactions to look at
    let select = document.getElementById( 'accountChoose' );

    fetch('/Accounts/viewAll/'+ Cookies.get("UserID"), {method: 'get'}
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
    document.getElementById("newButton").addEventListener("click", editTransaction);

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
        document.getElementById("currency").value = '';
        document.getElementById("currency").disabled = false;
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
                document.getElementById("balanceChange").value = String.fromCharCode(parseInt(signs[responseData.Currency+"2"],16)) +
                    (Math.floor(responseData.BalanceChange * rates[responseData.Currency])/100).toFixed(2);
                document.getElementById("currency").value = responseData.Currency;
                document.getElementById("currency").disabled = true;
                document.getElementById("category").selected = responseData.CategoryID;
                document.getElementById("description").value = responseData.Description;
                document.getElementById("date").value = responseData["Date"];
            }

        });

    }
    document.getElementById("listDiv").style.display = 'none';
    document.getElementById("editDiv").style.display = 'block';
}

function saveChanges(event) {
    event.preventDefault();

    //Makes sure that all parts have been filled in
    if (document.getElementById("balanceChange").value.trim() === '' || document.getElementById("balanceChange").value.trim() === 0) {
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

    if(parseInt(formData.get("balanceChange")) === 0){
        alert("Please provide a transaction amount.");
        return;
    }

    formData.append("standingOrderID", "0");
    formData.append("currency",document.getElementById("currency").value);


    //Creates account if a new transaction or updates the current transaction if it's being edited

    let apiPath = '';
    let convertedAmount;
    if (id === '') {
        apiPath = '/Transactions/create';
        convertedAmount = Math.sign(formData.get("balanceChange")) * Math.ceil(Math.abs((formData.get("balanceChange")*100)/rates[formData.get("currency")]));

    } else {
        apiPath = '/Transactions/edit';
        convertedAmount =  Math.sign(formData.get("balanceChange")) * Math.ceil(Math.abs((formData.get("balanceChange").replace(/[^\d.-]/g, '')*100)/rates[formData.get("currency")]));
    }

    formData.set("balanceChange",convertedAmount);
    formData.append("accountID",Cookies.get("AccountID"));



    fetch(apiPath, {method: 'post', body: formData}
    ).then(response => response.json()
    ).then(responseData => {

        if (responseData.hasOwnProperty('error')) {
            alert(responseData.error);
        } else {
            document.getElementById("listDiv").style.display = 'block';
            document.getElementById("editDiv").style.display = 'none';
            pageLoad();
            changeUser();
        }
    });

}

function cancelChanges(event){
    event.preventDefault();

    changeUser();

    document.getElementById("listDiv").style.display = 'block';
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
            let accountAmount= String.fromCharCode(parseInt(signs[responseData.Currency+"2"],16)) + `${(Math.floor(responseData.Balance * rates[responseData.Currency])/100).toFixed(2)}`;
            document.getElementById('chosenAccountBalance').innerText = "  Current Account Balance: "+ accountAmount;

        }
    });


    //Formatting all transactions relating to the account
    let transactionHTML = `<table id="transactionTable">` +
        '<tr>' +
        '<th onclick="sortTable(0)">ID</th>' +
        '<th onclick="sortTable(1)">Amount</th>' +
        '<th onclick="sortTable(2)">Category</th>' +
        '<th>Description</th>' +
        '<th onclick="sortTable(4)">Date</th>' +
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
                let editButtons = document.getElementsByClassName("editButton");
                let deleteButtons = document.getElementsByClassName("deleteButton");

                if(accessLevel >= 2){
                    document.getElementById("accessDisplay").innerText = "Account permission sufficient to create/delete/edit transactions";
                    for (let button of editButtons) {
                        button.addEventListener("click", editTransaction);
                        button.disabled = false;
                        button.title = "Edit transaction";
                    }

                    for (let button of deleteButtons) {
                        button.addEventListener("click", deleteTransaction);
                        button.disabled = false;
                        button.title = "Delete transaction";
                    }

                    document.getElementById("newButton").disabled = false;
                }else{
                    document.getElementById("accessDisplay").innerText = "Account permission insufficient to create/delete/edit transactions";
                    for (let button of editButtons) {
                        button.disabled = true;
                        button.title = "Insufficient permission to edit transaction";
                    }
                    for (let button of deleteButtons) {
                        button.disabled = true;
                        button.title = "Insufficient permission to delete transaction";
                    }

                    document.getElementById("newButton").disabled = true;
                }
            }
        });

    });

    document.getElementById("saveButton").addEventListener("click", saveChanges);
    document.getElementById("cancelButton").addEventListener("click", cancelChanges);
}

//This function allows the table of transactions to be sorted by its headers
function sortTable(n) {
    let table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;

    table = document.getElementById("transactionTable");
    switching = true;

    // Set the sorting direction to ascending:
    dir = "asc";

    /* Make a loop that will continue until
    no switching has been done: */
    while (switching) {


        // Start by saying: no switching is done:
        switching = false;
        rows = table.rows;
        /* Loop through all table rows (except the
        first, which contains table headers): */

        for (i = 1; i < (rows.length - 1); i++) {
            // Start by saying there should be no switching:
            shouldSwitch = false;

            /* Get the two elements you want to compare,
            one from current row and one from the next: */
            x = rows[i].getElementsByTagName("TD")[n];
            y = rows[i + 1].getElementsByTagName("TD")[n];

            //Set the way in which you sort depending on what depending header you are sorting by
            let ascCheck, descCheck;

            switch(n){
                case 0:
                    ascCheck = Number(x.innerHTML) > Number(y.innerHTML);
                    descCheck = Number(x.innerHTML) < Number(y.innerHTML);
                    break;
                case 1:
                    ascCheck = Number(x.innerHTML.replace(/[^\d.-]/g, '')) > Number(y.innerHTML.replace(/[^\d.-]/g, ''));
                    descCheck = Number(x.innerHTML.replace(/[^\d.-]/g, '')) < Number(y.innerHTML.replace(/[^\d.-]/g, ''));
                    break;
                case 2:
                    ascCheck = x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase();
                    descCheck = x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase();
                    break;
                case 4:
                    let date1 = new Date(x.innerHTML);
                    let date2 = new Date(y.innerHTML);
                    ascCheck = date1 > date2;
                    descCheck = date1 < date2;
                    break;
            }

            /* Check if the two rows should switch place,
            based on the direction, asc or desc: */
            if (dir === "asc") {
                if (ascCheck) {
                    // If so, mark as a switch and break the loop:
                    shouldSwitch = true;
                    break;
                }
            } else if (dir === "desc") {
                if (descCheck) {
                    // If so, mark as a switch and break the loop:
                    shouldSwitch = true;
                    break;
                }
            }
        }
        if (shouldSwitch) {
            /* If a switch has been marked, make the switch
            and mark that a switch has been done: */
            rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
            switching = true;

            // Each time a switch is done, increase this count by 1:
            switchcount ++;
        } else {
            /* If no switching has been done AND the direction is "asc",
            set the direction to "desc" and run the while loop again. */
            if (switchcount === 0 && dir === "asc") {
                dir = "desc";
                switching = true;
            }
        }
    }
}

