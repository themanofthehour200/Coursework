//Associated array of currency types to their signs
let signs = {"EUR": "&#8364;", "GDP": "&#163;", "USD": "&#36;",
    "EUR2":"20AC", "GDP2": "00A3","USD2":"0024"};
let rates = {"GDP":1,"EUR" : 1.18, "USD": 1.31};

function pageLoad() {


    startUp();

    let categoryHTML = `<table>` +
        '<tr>' +
        '<th>ID</th>' +
        '<th>Category Name</th>' +
        '</tr>';

    fetch('/Categories/list/'+ Cookies.get("UserID"), {method: 'get'}
    ).then(response => response.json()
    ).then(categories => {

        for (let category of categories) {

            categoryHTML += `<tr>` +
                `<td>${category.CategoryID}</td>` +
                `<td>${category.CategoryName}</td>` +
                `<td class="last">` +
                `<button class='editButton' id = 'category${category.AccessID}' data-id='${category.CategoryID}'>Edit</button>` +
                `<button class='deleteButton' id = 'category${category.AccessID}' data-id='[${category.CategoryID},${category.AccessID}]'>Delete</button>` +
                `</td>` +
                `</tr>`;
        }

        categoryHTML += '</table>';

        document.getElementById("listDiv").innerHTML = categoryHTML;

        let editButtons = document.getElementsByClassName("editButton");
        for (let button of editButtons) {
            let accessLevel = parseInt((button.id).replace("category",""));
            console.log(accessLevel);

            if(accessLevel !== 0 && !isNaN(accessLevel)) {
                button.addEventListener("click", editAccount);
            }else{
                button.disabled = true;
            }
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

        document.getElementById("categoryID").value = '';
        document.getElementById("categoryName").value = '';

    } else {

        fetch('/Categories/search/' + id, {method: 'get'}
        ).then(response => response.json()
        ).then(responseData => {

            if (responseData.hasOwnProperty('error')) {
                alert(responseData.error);
            } else {

                document.getElementById("editHeading").innerHTML = 'Editing Category Number ' + responseData.CategoryID + ':';
                document.getElementById("categoryID").value = id;
                document.getElementById("categoryName").value = responseData.CategoryName;

            }

        });

    }
    document.getElementById("listDiv").style.display = 'none';
    document.getElementById("newButton").style.display = 'none';
    document.getElementById("editDiv").style.display = 'block';
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
        formData.set("balance",Math.ceil((formData.get("balance")*100)/rates[formData.get("currency")]));

    } else {
        console.log("edit account");
        formData.set("balance",Math.ceil((formData.get("balance").replace(/[^\d.-]/g, ''))*100/rates[formData.get("currency")]));
        apiPath = '/Accounts/edit';
    }

    for (let value of formData.values()) {
        console.log(value);
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

