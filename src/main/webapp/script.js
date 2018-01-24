function onChangeSelectHandler() {
    console.log("works really fine");
    var testDivEl = document.getElementById("testDiv");
    var selectEl = testDiv.getElementsByTagName("select")[0];

    var options = selectEl.options;
    console.log("selectEl", selectEl);
    console.log("selectEl.value= ", selectEl.getAttribute("value"));

    var generatedUlDivEl = document.getElementById("generatedUlDiv");
    var generatedUl = '<ul id="generatedUl">';
    for (i = 0; i < options.length; i++) {
        generatedUl += '<li onclick="onClickListElementHandler(this)" data-value="' + options[i].value + '" >' + options[i].label + '</li>';
    }
    generatedUl += '</ul>';
    generatedUlDivEl.innerHTML = generatedUl;

    var searchInputEl = document.getElementById("searchInputDiv");
    searchInputEl.innerHTML = '<input type="text" id="searchInput" name="searchInput" value="" class="setting-input" placeholder="Search Tests..."/>';
};

var testDivEl = document.getElementById("testDiv");
var selectEl = testDiv.getElementsByTagName("select")[0];
setNameToResult(selectEl.getAttribute("value"));
selectEl.onmessage = function onLoad() {
    console.log("aaaa Load!!");
};

function onLoad123() {
    console.log("aaaa Load!!");
};



function onClickResultHandler() {
    var toggleEl = document.getElementById("hiddenSelect");
    //console.log("click!", toggleEl.style.display);
    if (toggleEl.style.display === "block") {
        toggleEl.style.display = "none";
    } else {
        onChangeSelectHandler();
        toggleEl.style.display = "block";
    }
};

function onClickListElementHandler(li) {
    console.log("click li ", li.dataset.value);
    console.log("click li ", li.innerHTML);
}

document.onclick = function(event) {
    if (event.target.id != "result" && event.target.id != "searchInput") {
        var toggleEl = document.getElementById("hiddenSelect");
        toggleEl.style.display = "none";
    }
}

function setNameToResult(name) {
    var resultDivEl = document.getElementById("result");
    resultDivEl.innerHTML = name;
}