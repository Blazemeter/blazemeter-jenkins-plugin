function onChangeSelectHandler() {
    var testDivEl = document.getElementById("testDiv");
    var selectEl = testDiv.getElementsByTagName("select")[0];

    var options = selectEl.options;
    console.log("selectEl", selectEl);
    console.log("selectEl.value= ", selectEl.getAttribute("value"));

    var generatedUlDivEl = document.getElementById("generatedUlDiv");
    var generatedUl = '<ul id="generatedUl">';
    for (i = 0; i < options.length; i++) {
        generatedUl += '<li onclick="onClickListElementHandler(this)" data-value="' + options[i].value
                    + '" data-index="' + i
                    + '" >' + options[i].label + '</li>';
    }
    generatedUl += '</ul>';
    generatedUlDivEl.innerHTML = generatedUl;

    var searchInputEl = document.getElementById("searchInputDiv");
    searchInputEl.innerHTML = '<input type="text" id="searchInput" name="searchInput" value="" class="setting-input" placeholder="Search Tests..."/>';
};

var testDivEl = document.getElementById("testDiv");
var selectEl = testDiv.getElementsByTagName("select")[0];
setNameToResult(selectEl.getAttribute("value"));
selectEl.ondataavailable = function onLoad() {
    console.log("aaaa Load!!");
};



function onClickResultHandler() {
    var toggleEl = document.getElementById("hiddenSelect");
    if (toggleEl.style.display === "block") {
        toggleEl.style.display = "none";
    } else {
        onChangeSelectHandler();
        toggleEl.style.display = "block";
    }
};

function onClickListElementHandler(li) {
    setNameToResult(li.innerHTML);
    setValueToSelect(li);
    console.log("click li ", li.dataset.value);
    console.log("click li ", li.innerHTML);
}

document.onclick = function(event) {
    if (event.target.id != "result" && event.target.id != "searchInput") {
        var toggleEl = document.getElementById("hiddenSelect");
        toggleEl.style.display = "none";
    }
}

function setValueToSelect(li) {
    var testDivEl = document.getElementById("testDiv");
    var selectEl = testDiv.getElementsByTagName("select")[0];
    selectEl.setAttribute("value", li.dataset.value);
    selectEl.options[li.dataset.index].selected = true;
}

function setNameToResult(name) {
    var resultDivEl = document.getElementById("result");
    resultDivEl.innerHTML = name;
}