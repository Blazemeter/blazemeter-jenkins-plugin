function onChangeSelectHandler() {
    var selectEl = getSelectEl();
    var options = selectEl.options;

    var generatedUlDivEl = document.getElementById("generatedUlDiv");
    var generatedUl = '<ul id="generatedUl">';
    for (i = 0; i < options.length; i++) {
        generatedUl += '<li class="generatedLi" onclick="onClickListElementHandler(this)" data-value="' + options[i].value
                    + '" data-index="' + i
                    + '" >' + options[i].label + '</li>';

        if (options[i].selected) {
            setNameToResult(options[i].label);
        }
    }
    generatedUl += '</ul>';
    generatedUlDivEl.innerHTML = generatedUl;

    var searchInputDivEl = document.getElementById("searchInputDiv");
    searchInputDivEl.innerHTML = '<input type="text" onkeyup="onKeyUpSearch()" id="searchInput" name="searchInput" value="" class="setting-input" placeholder="Search Tests..."/>';
};

var selectEl = getSelectEl();
setNameToResult(selectEl.getAttribute("value"));

function getSelectEl() {
    var testDivEl = document.getElementById("testDiv");
    return testDiv.getElementsByTagName("select")[0];
}

// waiting when init state of original <select> will be changed
var count = 0;
var interval = setInterval(function() {
    var resultDivEl = document.getElementById("result");
    var prevValue = resultDivEl.innerHTML;

    onChangeSelectHandler(); // if <select> element filled it will change 'resultDivEl.innerHTML'
    var curValue = resultDivEl.innerHTML;

    if ((prevValue != curValue) || (count >= 20)) {
        clearInterval(interval);
    }
    count++;
}, 500)

function onKeyUpSearch() {
    var searchInputEl = document.getElementById("searchInput");
    var text = searchInputEl.value.toLowerCase();
    var liList = document.getElementById("generatedUl").getElementsByTagName("li");
    for (i = 0; i < liList.length; i++) {
        if (!liList[i].innerHTML.toLowerCase().includes(text)) {
            liList[i].style.display = "none";
        } else {
            liList[i].style.display = "";
        }
    }
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
    hideHiddenSelect();
};

document.onmousedown = function(event) {
    if (event.target.id != "result"
        && event.target.id != "generatedUl"
        && event.target.getAttribute("class") != "generatedLi"
        && event.target.id != "searchInput") {
            hideHiddenSelect();
    }
};

function hideHiddenSelect() {
    var toggleEl = document.getElementById("hiddenSelect");
    toggleEl.style.display = "none";
}

function setValueToSelect(li) {
    var selectEl = getSelectEl();
    selectEl.setAttribute("value", li.dataset.value);
    selectEl.options[li.dataset.index].selected = true;
};

function setNameToResult(name) {
    var resultDivEl = document.getElementById("result");
    resultDivEl.innerHTML = name;
};