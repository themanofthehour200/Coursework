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
                `<button class='deleteButton' id = 'category${category.AccessID}' data-id='${category.CategoryID}'>Delete</button>` +
                `</td>` +
                `</tr>`;
        }

        categoryHTML += '</table>';

        document.getElementById("listDiv").innerHTML = categoryHTML;

        let editButtons = document.getElementsByClassName("editButton");
        for (let button of editButtons) {
            let accessLevel = parseInt((button.id).replace("category",""));

            if(accessLevel !== 0 && !isNaN(accessLevel)) {
                button.addEventListener("click", editCategory);
            }else{
                button.title = "Cannot edit category";
                button.disabled = true;
            }
        }

        let deleteButtons = document.getElementsByClassName("deleteButton");
        for (let button of deleteButtons) {
            let accessLevel = parseInt((button.id).replace("category",""));

            if(accessLevel !== 0 && !isNaN(accessLevel)) {
                button.addEventListener("click", deleteCategory);
            }else{
                button.title = "Cannot delete category";
                button.disabled = true;
            }
        }

    });

    document.getElementById("newButton").addEventListener("click",editCategory);
    document.getElementById("saveButton").addEventListener("click", saveChanges);
    document.getElementById("cancelButton").addEventListener("click", cancelChanges);


}

function editCategory(event){

    const id = event.target.getAttribute("data-id");

    document.getElementById("accessID").value = Cookies.get("UserID");

    //If no id exists it means that tis function is being called by the 'create new account' button instead
    if (id === null) {

        document.getElementById("editHeading").innerHTML = 'Create New Category:';

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

function deleteCategory(event){

    //Creates a form and gets the users ID and the account ID
    let categoryID = event.target.getAttribute("data-id");
    let formData = new FormData();
    formData.append("categoryID", categoryID);
    console.log(categoryID);

    const ok = confirm("Are you sure?");
    if(ok) {

        fetch('/Categories/delete', {method: 'post', body: formData}
        ).then(response => response.json()
        ).then(responseData => {
            if (responseData.hasOwnProperty('error')) {
                alert(responseData.error);
            } else {
                pageLoad();
            }
        });
    }
}


function saveChanges() {
    event.preventDefault();

    //Makes sure that all parts have been filled in
    if (document.getElementById("categoryName").value.trim() === '') {
        alert("Please provide an account name.");
        return;
    }

    const id = document.getElementById("categoryID").value;
    const form = document.getElementById("categoryForm");
    const formData = new FormData(form);

    console.log(formData.get("accessID"));

    //Creates category if a new category and updates the current category if it's being edited
    let apiPath = '';
    if (id === '') {
        apiPath = '/Categories/new';
    } else {
        apiPath = '/Categories/edit';
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

